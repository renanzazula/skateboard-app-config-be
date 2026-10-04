package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetGuestApplicationConfigUseCase;
import com.skateboard.appconfig.application.port.in.UpdateGuestApplicationConfigUseCase;
import com.skateboard.appconfig.domain.model.GuestApplicationConfig;
import com.skateboard.appconfig.infrastructure.web.api.GuestApplicationSettingsApi;
import com.skateboard.appconfig.infrastructure.web.dto.GuestApplicationSettingsResponse;
import com.skateboard.appconfig.infrastructure.web.dto.PublicGuestApplicationSettingsResponse;
import com.skateboard.appconfig.infrastructure.web.dto.UpdateGuestApplicationSettingsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * GET /api/guest-application-settings is intentionally open to any
 * authenticated caller (no {@code @PreAuthorize}) — it backs the mobile
 * Settings → Community entry point's "should I show this feature" check and
 * returns only {@code enabled}, never the recipient list or email template.
 * Mirrors HomeFeaturedPlayerConfigController's unrestricted GET. The
 * admin-only surface returns and accepts the full settings.
 */
@RestController
public class GuestApplicationSettingsController implements GuestApplicationSettingsApi {

    private final GetGuestApplicationConfigUseCase getGuestApplicationConfigUseCase;
    private final UpdateGuestApplicationConfigUseCase updateGuestApplicationConfigUseCase;

    public GuestApplicationSettingsController(GetGuestApplicationConfigUseCase getGuestApplicationConfigUseCase,
                                               UpdateGuestApplicationConfigUseCase updateGuestApplicationConfigUseCase) {
        this.getGuestApplicationConfigUseCase = getGuestApplicationConfigUseCase;
        this.updateGuestApplicationConfigUseCase = updateGuestApplicationConfigUseCase;
    }

    @Override
    public ResponseEntity<PublicGuestApplicationSettingsResponse> getGuestApplicationSettingsPublic() {
        GuestApplicationConfig config = getGuestApplicationConfigUseCase.execute();
        return ResponseEntity.ok(new PublicGuestApplicationSettingsResponse().enabled(config.isEnabled()));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_GUEST_APPLICATION_CONFIGURE')")
    public ResponseEntity<GuestApplicationSettingsResponse> getGuestApplicationSettingsAdmin() {
        return ResponseEntity.ok(toResponse(getGuestApplicationConfigUseCase.execute()));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_GUEST_APPLICATION_CONFIGURE')")
    public ResponseEntity<GuestApplicationSettingsResponse> updateGuestApplicationSettings(
            UpdateGuestApplicationSettingsRequest request) {
        GuestApplicationConfig updated = updateGuestApplicationConfigUseCase.execute(
                new UpdateGuestApplicationConfigUseCase.Command(
                        currentAdminId(),
                        Boolean.TRUE.equals(request.getEnabled()),
                        toRecipientIds(request.getRecipientIds()),
                        request.getConfirmationSubject(),
                        request.getConfirmationBody()));
        return ResponseEntity.ok(toResponse(updated));
    }

    private GuestApplicationSettingsResponse toResponse(GuestApplicationConfig config) {
        return new GuestApplicationSettingsResponse()
                .enabled(config.isEnabled())
                .recipientIds(List.copyOf(config.getRecipientIds()))
                .confirmationSubject(config.getConfirmationSubject())
                .confirmationBody(config.getConfirmationBody())
                .updatedAt(toOffsetDateTime(config.getUpdatedAt()))
                .updatedBy(config.getUpdatedBy());
    }

    private Set<UUID> toRecipientIds(List<UUID> recipientIds) {
        return recipientIds != null ? new LinkedHashSet<>(recipientIds) : Set.of();
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant != null ? OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    private String currentAdminId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
