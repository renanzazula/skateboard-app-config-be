package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetPrivacyPolicyUseCase;
import com.skateboard.appconfig.application.port.in.SavePrivacyPolicyUseCase;
import com.skateboard.appconfig.domain.model.PrivacyPolicy;
import com.skateboard.appconfig.infrastructure.web.api.PrivacyPolicyApi;
import com.skateboard.appconfig.infrastructure.web.dto.PrivacyPolicyResponse;
import com.skateboard.appconfig.infrastructure.web.dto.PrivacyPolicyStatus;
import com.skateboard.appconfig.infrastructure.web.dto.UpdatePrivacyPolicyRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * The Privacy Policy page. {@code GET /api/privacy-policy} is fully
 * anonymous — no {@code @PreAuthorize} here, and {@code SecurityConfig}
 * grants it {@code permitAll()} — unlike {@link AboutUsController}'s GET,
 * which still requires an authenticated caller. The draft read and the save
 * require FUNC_PRIVACY_POLICY_MANAGE.
 */
@RestController
public class PrivacyPolicyController implements PrivacyPolicyApi {

    private final GetPrivacyPolicyUseCase getPrivacyPolicyUseCase;
    private final SavePrivacyPolicyUseCase savePrivacyPolicyUseCase;

    public PrivacyPolicyController(GetPrivacyPolicyUseCase getPrivacyPolicyUseCase,
                                   SavePrivacyPolicyUseCase savePrivacyPolicyUseCase) {
        this.getPrivacyPolicyUseCase = getPrivacyPolicyUseCase;
        this.savePrivacyPolicyUseCase = savePrivacyPolicyUseCase;
    }

    @Override
    public ResponseEntity<PrivacyPolicyResponse> getPrivacyPolicy() {
        return getPrivacyPolicyUseCase.execute(false)
                .map(page -> ResponseEntity.ok(toResponse(page)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_PRIVACY_POLICY_MANAGE')")
    public ResponseEntity<PrivacyPolicyResponse> getPrivacyPolicyAdmin() {
        return getPrivacyPolicyUseCase.execute(true)
                .map(page -> ResponseEntity.ok(toResponse(page)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_PRIVACY_POLICY_MANAGE')")
    public ResponseEntity<PrivacyPolicyResponse> updatePrivacyPolicy(UpdatePrivacyPolicyRequest request) {
        PrivacyPolicy saved = savePrivacyPolicyUseCase.execute(new SavePrivacyPolicyUseCase.Command(
                currentAdminId(), request.getTitle(), request.getBody(), toDomainStatus(request.getStatus())));
        return ResponseEntity.ok(toResponse(saved));
    }

    private PrivacyPolicyResponse toResponse(PrivacyPolicy page) {
        return new PrivacyPolicyResponse()
                .title(page.getTitle())
                .body(page.getBody())
                .status(PrivacyPolicyStatus.fromValue(page.getStatus().name().toLowerCase()))
                .updatedAt(toOffsetDateTime(page.getUpdatedAt()))
                .updatedBy(page.getUpdatedBy());
    }

    private PrivacyPolicy.Status toDomainStatus(PrivacyPolicyStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("A status is required.");
        }
        return PrivacyPolicy.Status.valueOf(status.getValue().toUpperCase());
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
