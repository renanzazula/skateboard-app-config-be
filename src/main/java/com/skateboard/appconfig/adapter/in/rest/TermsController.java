package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetTermsUseCase;
import com.skateboard.appconfig.application.port.in.SaveTermsUseCase;
import com.skateboard.appconfig.domain.model.Terms;
import com.skateboard.appconfig.infrastructure.web.api.TermsApi;
import com.skateboard.appconfig.infrastructure.web.dto.TermsResponse;
import com.skateboard.appconfig.infrastructure.web.dto.TermsStatus;
import com.skateboard.appconfig.infrastructure.web.dto.UpdateTermsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * The Terms &amp; Conditions page. {@code GET /api/terms} is open to any
 * authenticated caller (it backs the standard-user viewer), mirroring {@link
 * AboutUsController}. The draft read and the save require FUNC_TERMS_MANAGE.
 */
@RestController
public class TermsController implements TermsApi {

    private final GetTermsUseCase getTermsUseCase;
    private final SaveTermsUseCase saveTermsUseCase;

    public TermsController(GetTermsUseCase getTermsUseCase, SaveTermsUseCase saveTermsUseCase) {
        this.getTermsUseCase = getTermsUseCase;
        this.saveTermsUseCase = saveTermsUseCase;
    }

    @Override
    public ResponseEntity<TermsResponse> getTerms() {
        return getTermsUseCase.execute(false)
                .map(page -> ResponseEntity.ok(toResponse(page)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TERMS_MANAGE')")
    public ResponseEntity<TermsResponse> getTermsAdmin() {
        return getTermsUseCase.execute(true)
                .map(page -> ResponseEntity.ok(toResponse(page)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TERMS_MANAGE')")
    public ResponseEntity<TermsResponse> updateTerms(UpdateTermsRequest request) {
        Terms saved = saveTermsUseCase.execute(new SaveTermsUseCase.Command(
                currentAdminId(), request.getTitle(), request.getBody(), toDomainStatus(request.getStatus())));
        return ResponseEntity.ok(toResponse(saved));
    }

    private TermsResponse toResponse(Terms page) {
        return new TermsResponse()
                .title(page.getTitle())
                .body(page.getBody())
                .status(TermsStatus.fromValue(page.getStatus().name().toLowerCase()))
                .updatedAt(toOffsetDateTime(page.getUpdatedAt()))
                .updatedBy(page.getUpdatedBy());
    }

    private Terms.Status toDomainStatus(TermsStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("A status is required.");
        }
        return Terms.Status.valueOf(status.getValue().toUpperCase());
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
