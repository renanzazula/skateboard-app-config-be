package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetAboutPageUseCase;
import com.skateboard.appconfig.application.port.in.SaveAboutPageUseCase;
import com.skateboard.appconfig.application.port.in.UploadAboutImageUseCase;
import com.skateboard.appconfig.domain.model.AboutPage;
import com.skateboard.appconfig.infrastructure.web.api.AboutUsApi;
import com.skateboard.appconfig.infrastructure.web.dto.AboutImageResponse;
import com.skateboard.appconfig.infrastructure.web.dto.AboutPageResponse;
import com.skateboard.appconfig.infrastructure.web.dto.AboutPageStatus;
import com.skateboard.appconfig.infrastructure.web.dto.UpdateAboutPageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * The About Us page. {@code GET /api/about-us} is open to any authenticated
 * caller (it backs the standard-user viewer) and returns 204 until a page is
 * published; the draft read, the save and the image upload require
 * FUNC_ABOUT_US_MANAGE. Mirrors {@link HomeVideoCategoryConfigController}
 * (open GET / gated write) and {@link BrandingAdminController} (image upload).
 */
@RestController
public class AboutUsController implements AboutUsApi {

    private final GetAboutPageUseCase getAboutPageUseCase;
    private final SaveAboutPageUseCase saveAboutPageUseCase;
    private final UploadAboutImageUseCase uploadAboutImageUseCase;
    private final AboutBlockImageResolver imageResolver;

    public AboutUsController(GetAboutPageUseCase getAboutPageUseCase,
                            SaveAboutPageUseCase saveAboutPageUseCase,
                            UploadAboutImageUseCase uploadAboutImageUseCase,
                            AboutBlockImageResolver imageResolver) {
        this.getAboutPageUseCase = getAboutPageUseCase;
        this.saveAboutPageUseCase = saveAboutPageUseCase;
        this.uploadAboutImageUseCase = uploadAboutImageUseCase;
        this.imageResolver = imageResolver;
    }

    @Override
    public ResponseEntity<AboutPageResponse> getAboutUs() {
        return getAboutPageUseCase.execute(false)
                .map(page -> ResponseEntity.ok(toResponse(page)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_ABOUT_US_MANAGE')")
    public ResponseEntity<AboutPageResponse> getAboutUsAdmin() {
        return getAboutPageUseCase.execute(true)
                .map(page -> ResponseEntity.ok(toResponse(page)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_ABOUT_US_MANAGE')")
    public ResponseEntity<AboutPageResponse> updateAboutUs(UpdateAboutPageRequest request) {
        List<Map<String, Object>> blocks = imageResolver.toStorageKeys(request.getBlocks());
        AboutPage saved = saveAboutPageUseCase.execute(new SaveAboutPageUseCase.Command(
                currentAdminId(), request.getTitle(), request.getSubtitle(),
                toDomainStatus(request.getStatus()), blocks));
        return ResponseEntity.ok(toResponse(saved));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_ABOUT_US_MANAGE')")
    public ResponseEntity<AboutImageResponse> uploadAboutUsImage(MultipartFile file) {
        String url = uploadAboutImageUseCase.execute(
                new UploadAboutImageUseCase.Command(readBytes(file), file.getContentType()));
        return ResponseEntity.status(201).body(new AboutImageResponse().url(url));
    }

    private AboutPageResponse toResponse(AboutPage page) {
        return new AboutPageResponse()
                .title(page.getTitle())
                .subtitle(page.getSubtitle())
                .status(AboutPageStatus.fromValue(page.getStatus().name().toLowerCase()))
                .blocks(imageResolver.toSignedUrls(page.getBlocks()))
                .updatedAt(toOffsetDateTime(page.getUpdatedAt()))
                .updatedBy(page.getUpdatedBy());
    }

    private AboutPage.Status toDomainStatus(AboutPageStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("A status is required.");
        }
        return AboutPage.Status.valueOf(status.getValue().toUpperCase());
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant != null ? OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }
    }

    private String currentAdminId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
