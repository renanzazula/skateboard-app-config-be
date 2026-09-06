package com.skateboard.appconfig.infrastructure.web;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Bounds writes on the one anonymous, unauthenticated write endpoint,
 * {@code POST /api/campaigns/{id}/events}. Over the limit the request is
 * dropped with a {@code 204} — the same status a recorded event returns — so
 * the contract "never blocks or fails the app" (spec §12) still holds; the
 * client can't tell its event was shed.
 *
 * A per-client sliding window in memory. `X-Forwarded-For`'s first hop is the
 * client key when present (requests arrive via skateboard-ui-backend), else
 * the socket address. The key map is size-capped and cleared wholesale on
 * overflow — crude but bounded and self-healing, and this is best-effort
 * abuse protection, not accounting.
 *
 * The {@code campaign.events.ratelimit} counter (tag {@code outcome} =
 * {@code accepted} | {@code shed}) makes it visible whether the limit is
 * tuned right — a rising {@code shed} share means legitimate traffic is being
 * dropped and the window needs widening. New Relic picks up Micrometer meters.
 */
@Component
public class CampaignEventRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CampaignEventRateLimitFilter.class);
    private static final Pattern EVENTS_PATH =
            Pattern.compile("^/api/campaigns/[^/]+/events/?$");
    private static final int MAX_KEYS = 20_000;

    private final int maxRequests;
    private final long windowMs;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();
    private final Counter acceptedCounter;
    private final Counter shedCounter;

    public CampaignEventRateLimitFilter(
            @Value("${app.campaign.events.rate-limit.max-requests:40}") int maxRequests,
            @Value("${app.campaign.events.rate-limit.window-seconds:10}") long windowSeconds,
            MeterRegistry meterRegistry) {
        this.maxRequests = maxRequests;
        this.windowMs = windowSeconds * 1000;
        this.acceptedCounter = Counter.builder("campaign.events.ratelimit")
                .description("Campaign analytics events by rate-limit outcome")
                .tag("outcome", "accepted").register(meterRegistry);
        this.shedCounter = Counter.builder("campaign.events.ratelimit")
                .description("Campaign analytics events by rate-limit outcome")
                .tag("outcome", "shed").register(meterRegistry);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod())
                || !EVENTS_PATH.matcher(request.getRequestURI()).matches();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (allow(clientKey(request))) {
            acceptedCounter.increment();
            filterChain.doFilter(request, response);
        } else {
            shedCounter.increment();
            log.debug("Rate-limited campaign event from {}", clientKey(request));
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private boolean allow(String key) {
        if (hits.size() > MAX_KEYS) {
            hits.clear();
        }
        Deque<Long> window = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > windowMs) {
                window.pollFirst();
            }
            if (window.size() >= maxRequests) {
                return false;
            }
            window.addLast(now);
            return true;
        }
    }

    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }
}
