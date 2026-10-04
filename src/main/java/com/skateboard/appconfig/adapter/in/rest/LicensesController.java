package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetLicensesUseCase;
import com.skateboard.appconfig.application.port.in.SaveLicensesUseCase;
import com.skateboard.appconfig.domain.model.Licenses;
import com.skateboard.appconfig.infrastructure.web.api.LicensesApi;
import com.skateboard.appconfig.infrastructure.web.dto.LicensesResponse;
import com.skateboard.appconfig.infrastructure.web.dto.LicensesStatus;
import com.skateboard.appconfig.infrastructure.web.dto.UpdateLicensesRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * The Open-source Licenses page. {@code GET /api/licenses} is open to any
 * authenticated caller (it backs the standard-user viewer), mirroring {@link
 * AboutUsController}. The draft read and the save require
 * FUNC_LICENSES_MANAGE.
 */
@RestController
public class LicensesController implements LicensesApi {

    private final GetLicensesUseCase getLicensesUseCase;
    private final SaveLicensesUseCase saveLicensesUseCase;

    public LicensesController(GetLicensesUseCase getLicensesUseCase, SaveLicensesUseCase saveLicensesUseCase) {
        this.getLicensesUseCase = getLicensesUseCase;
        this.saveLicensesUseCase = saveLicensesUseCase;
    }

    @Override
    public ResponseEntity<LicensesResponse> getLicenses() {
        return getLicensesUseCase.execute(false)
                .map(page -> ResponseEntity.ok(toResponse(page)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_LICENSES_MANAGE')")
    public ResponseEntity<LicensesResponse> getLicensesAdmin() {
        return getLicensesUseCase.execute(true)
                .map(page -> ResponseEntity.ok(toResponse(page)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_LICENSES_MANAGE')")
    public ResponseEntity<LicensesResponse> updateLicenses(UpdateLicensesRequest request) {
        Licenses saved = saveLicensesUseCase.execute(new SaveLicensesUseCase.Command(
                currentAdminId(), request.getTitle(), request.getBody(), toDomainStatus(request.getStatus())));
        return ResponseEntity.ok(toResponse(saved));
    }

    private LicensesResponse toResponse(Licenses page) {
        return new LicensesResponse()
                .title(page.getTitle())
                .body(page.getBody())
                .status(LicensesStatus.fromValue(page.getStatus().name().toLowerCase()))
                .updatedAt(toOffsetDateTime(page.getUpdatedAt()))
                .updatedBy(page.getUpdatedBy());
    }

    private Licenses.Status toDomainStatus(LicensesStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("A status is required.");
        }
        return Licenses.Status.valueOf(status.getValue().toUpperCase());
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
