package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetBrandingConfigUseCase;
import com.skateboard.appconfig.application.port.in.ListBrandingAssetsUseCase;
import com.skateboard.appconfig.application.port.in.RemoveAppLogoUseCase;
import com.skateboard.appconfig.application.port.in.RemoveBrandingAssetUseCase;
import com.skateboard.appconfig.application.port.in.RemoveLoginBackgroundUseCase;
import com.skateboard.appconfig.application.port.in.ReplaceBrandingAssetUseCase;
import com.skateboard.appconfig.application.port.in.UploadAppLogoUseCase;
import com.skateboard.appconfig.application.port.in.UploadBrandingAssetUseCase;
import com.skateboard.appconfig.application.port.in.UploadLoginBackgroundUseCase;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.model.AppConfig;
import com.skateboard.appconfig.domain.model.BrandingAsset;
import com.skateboard.appconfig.infrastructure.web.api.AdminApi;
import com.skateboard.appconfig.infrastructure.web.dto.BrandingAssetResponse;
import com.skateboard.appconfig.infrastructure.web.dto.BrandingConfigResponse;
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
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class BrandingAdminController implements AdminApi {

    private final GetBrandingConfigUseCase getBrandingConfigUseCase;
    private final ListBrandingAssetsUseCase listBrandingAssetsUseCase;
    private final UploadLoginBackgroundUseCase uploadLoginBackgroundUseCase;
    private final RemoveLoginBackgroundUseCase removeLoginBackgroundUseCase;
    private final UploadAppLogoUseCase uploadAppLogoUseCase;
    private final RemoveAppLogoUseCase removeAppLogoUseCase;
    private final UploadBrandingAssetUseCase uploadBrandingAssetUseCase;
    private final ReplaceBrandingAssetUseCase replaceBrandingAssetUseCase;
    private final RemoveBrandingAssetUseCase removeBrandingAssetUseCase;
    private final ObjectStoragePort objectStoragePort;

    public BrandingAdminController(GetBrandingConfigUseCase getBrandingConfigUseCase,
                                    ListBrandingAssetsUseCase listBrandingAssetsUseCase,
                                    UploadLoginBackgroundUseCase uploadLoginBackgroundUseCase,
                                    RemoveLoginBackgroundUseCase removeLoginBackgroundUseCase,
                                    UploadAppLogoUseCase uploadAppLogoUseCase,
                                    RemoveAppLogoUseCase removeAppLogoUseCase,
                                    UploadBrandingAssetUseCase uploadBrandingAssetUseCase,
                                    ReplaceBrandingAssetUseCase replaceBrandingAssetUseCase,
                                    RemoveBrandingAssetUseCase removeBrandingAssetUseCase,
                                    ObjectStoragePort objectStoragePort) {
        this.getBrandingConfigUseCase = getBrandingConfigUseCase;
        this.listBrandingAssetsUseCase = listBrandingAssetsUseCase;
        this.uploadLoginBackgroundUseCase = uploadLoginBackgroundUseCase;
        this.removeLoginBackgroundUseCase = removeLoginBackgroundUseCase;
        this.uploadAppLogoUseCase = uploadAppLogoUseCase;
        this.removeAppLogoUseCase = removeAppLogoUseCase;
        this.uploadBrandingAssetUseCase = uploadBrandingAssetUseCase;
        this.replaceBrandingAssetUseCase = replaceBrandingAssetUseCase;
        this.removeBrandingAssetUseCase = removeBrandingAssetUseCase;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TAB_SETTINGS_BRANDING')")
    public ResponseEntity<BrandingConfigResponse> getBrandingConfig() {
        AppConfig config = getBrandingConfigUseCase.execute();
        List<BrandingAsset> assets = listBrandingAssetsUseCase.execute();
        return ResponseEntity.ok(toConfigResponse(config, assets));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TAB_SETTINGS_BRANDING')")
    public ResponseEntity<BrandingConfigResponse> uploadLoginBackground(MultipartFile file) {
        AppConfig updated = uploadLoginBackgroundUseCase.execute(
                new UploadLoginBackgroundUseCase.Command(currentAdminId(), readBytes(file), file.getContentType()));
        return ResponseEntity.ok(toConfigResponse(updated, listBrandingAssetsUseCase.execute()));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TAB_SETTINGS_BRANDING')")
    public ResponseEntity<BrandingConfigResponse> removeLoginBackground() {
        AppConfig updated = removeLoginBackgroundUseCase.execute(
                new RemoveLoginBackgroundUseCase.Command(currentAdminId()));
        return ResponseEntity.ok(toConfigResponse(updated, listBrandingAssetsUseCase.execute()));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TAB_SETTINGS_BRANDING')")
    public ResponseEntity<BrandingConfigResponse> uploadAppLogo(MultipartFile file) {
        AppConfig updated = uploadAppLogoUseCase.execute(
                new UploadAppLogoUseCase.Command(currentAdminId(), readBytes(file), file.getContentType()));
        return ResponseEntity.ok(toConfigResponse(updated, listBrandingAssetsUseCase.execute()));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TAB_SETTINGS_BRANDING')")
    public ResponseEntity<BrandingConfigResponse> removeAppLogo() {
        AppConfig updated = removeAppLogoUseCase.execute(
                new RemoveAppLogoUseCase.Command(currentAdminId()));
        return ResponseEntity.ok(toConfigResponse(updated, listBrandingAssetsUseCase.execute()));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TAB_SETTINGS_BRANDING')")
    public ResponseEntity<List<BrandingAssetResponse>> listBrandingAssets() {
        List<BrandingAssetResponse> response = listBrandingAssetsUseCase.execute().stream()
                .map(this::toAssetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TAB_SETTINGS_BRANDING')")
    public ResponseEntity<BrandingAssetResponse> uploadBrandingAsset(String name, MultipartFile file) {
        BrandingAsset asset = uploadBrandingAssetUseCase.execute(
                new UploadBrandingAssetUseCase.Command(currentAdminId(), name, readBytes(file), file.getContentType()));
        return ResponseEntity.status(201).body(toAssetResponse(asset));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TAB_SETTINGS_BRANDING')")
    public ResponseEntity<BrandingAssetResponse> replaceBrandingAsset(UUID assetId, MultipartFile file) {
        BrandingAsset asset = replaceBrandingAssetUseCase.execute(
                new ReplaceBrandingAssetUseCase.Command(currentAdminId(), assetId, readBytes(file), file.getContentType()));
        return ResponseEntity.ok(toAssetResponse(asset));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_TAB_SETTINGS_BRANDING')")
    public ResponseEntity<Void> removeBrandingAsset(UUID assetId) {
        removeBrandingAssetUseCase.execute(new RemoveBrandingAssetUseCase.Command(assetId));
        return ResponseEntity.noContent().build();
    }

    private BrandingConfigResponse toConfigResponse(AppConfig config, List<BrandingAsset> assets) {
        return new BrandingConfigResponse()
                .loginBackgroundUrl(objectStoragePort.presignGetUrl(config.getLoginBackgroundKey()))
                .loginBackgroundVersion(config.getLoginBackgroundVersion())
                .loginBackgroundUpdatedAt(toOffsetDateTime(config.getLoginBackgroundUpdatedAt()))
                .appLogoUrl(objectStoragePort.presignGetUrl(config.getAppLogoKey()))
                .appLogoVersion(config.getAppLogoVersion())
                .appLogoUpdatedAt(toOffsetDateTime(config.getAppLogoUpdatedAt()))
                .assets(assets.stream().map(this::toAssetResponse).collect(Collectors.toList()));
    }

    private BrandingAssetResponse toAssetResponse(BrandingAsset asset) {
        return new BrandingAssetResponse()
                .id(asset.getId())
                .name(asset.getName())
                .url(objectStoragePort.presignGetUrl(asset.getObjectKey()))
                .contentType(asset.getContentType())
                .version(asset.getVersion())
                .updatedAt(toOffsetDateTime(asset.getUpdatedAt()));
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
