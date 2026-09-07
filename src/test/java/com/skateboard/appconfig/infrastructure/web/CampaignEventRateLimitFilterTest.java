package com.skateboard.appconfig.infrastructure.web;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CampaignEventRateLimitFilterTest {

    private CampaignEventRateLimitFilter filter;
    private FilterChain chain;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        filter = new CampaignEventRateLimitFilter(3, 10, meterRegistry);
        chain = mock(FilterChain.class);
    }

    private double count(String outcome) {
        return meterRegistry.get("campaign.events.ratelimit").tag("outcome", outcome).counter().count();
    }

    private static MockHttpServletRequest eventPost(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
                "/api/campaigns/" + java.util.UUID.randomUUID() + "/events");
        request.setRemoteAddr(ip);
        return request;
    }

    @Test
    void passesThroughUpToTheLimitThenShedsWith204() throws Exception {
        for (int i = 0; i < 3; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(eventPost("10.0.0.1"), response, chain);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        MockHttpServletResponse fourth = new MockHttpServletResponse();
        filter.doFilter(eventPost("10.0.0.1"), fourth, chain);

        assertThat(fourth.getStatus()).isEqualTo(204);
        verify(chain, times(3)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void countsAcceptedAndShedEvents() throws Exception {
        for (int i = 0; i < 5; i++) {
            filter.doFilter(eventPost("10.0.0.1"), new MockHttpServletResponse(), chain);
        }

        assertThat(count("accepted")).isEqualTo(3);
        assertThat(count("shed")).isEqualTo(2);
    }

    @Test
    void limitsPerClientKey() throws Exception {
        for (int i = 0; i < 3; i++) {
            filter.doFilter(eventPost("10.0.0.1"), new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse otherClient = new MockHttpServletResponse();
        filter.doFilter(eventPost("10.0.0.2"), otherClient, chain);

        assertThat(otherClient.getStatus()).isEqualTo(200);
    }

    @Test
    void prefersTheForwardedForFirstHop() throws Exception {
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = eventPost("10.0.0.9");
            req.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.9");
            filter.doFilter(req, new MockHttpServletResponse(), chain);
        }
        MockHttpServletRequest sameRealClient = eventPost("10.0.0.9");
        sameRealClient.addHeader("X-Forwarded-For", "203.0.113.5");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(sameRealClient, response, chain);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    void ignoresNonEventRequests() throws Exception {
        MockHttpServletRequest getActive = new MockHttpServletRequest("GET", "/api/campaigns/active");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(getActive, response, chain);

        verify(chain, times(1)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
