package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UploadBrandingAssetUseCase;
import com.skateboard.appconfig.application.port.out.BrandingAssetRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.BrandingAssetNameConflictException;
import com.skateboard.appconfig.domain.model.BrandingAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UploadBrandingAssetService implements UploadBrandingAssetUseCase {

    private final BrandingAssetRepositoryPort brandingAssetRepositoryPort;
    private final ObjectStoragePort objectStoragePort;

    public UploadBrandingAssetService(BrandingAssetRepositoryPort brandingAssetRepositoryPort,
                                       ObjectStoragePort objectStoragePort) {
        this.brandingAssetRepositoryPort = brandingAssetRepositoryPort;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @Transactional
    public BrandingAsset execute(Command command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Asset name is required.");
        }
        if (brandingAssetRepositoryPort.existsByName(command.name())) {
            throw new BrandingAssetNameConflictException(command.name());
        }
        String extension = ImageUploadValidator.extensionFor(command.data(), command.mimeType());

        UUID id = UUID.randomUUID();
        String key = "assets/" + id + extension;
        objectStoragePort.put(key, command.data(), command.mimeType());

        BrandingAsset asset = BrandingAsset.create(id, command.name(), key, command.mimeType(), command.adminId());
        return brandingAssetRepositoryPort.save(asset);
    }
}
