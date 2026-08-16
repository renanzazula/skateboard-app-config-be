package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ReplaceBrandingAssetUseCase;
import com.skateboard.appconfig.application.port.out.BrandingAssetRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.BrandingAssetNotFoundException;
import com.skateboard.appconfig.domain.model.BrandingAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReplaceBrandingAssetService implements ReplaceBrandingAssetUseCase {

    private final BrandingAssetRepositoryPort brandingAssetRepositoryPort;
    private final ObjectStoragePort objectStoragePort;

    public ReplaceBrandingAssetService(BrandingAssetRepositoryPort brandingAssetRepositoryPort,
                                        ObjectStoragePort objectStoragePort) {
        this.brandingAssetRepositoryPort = brandingAssetRepositoryPort;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @Transactional
    public BrandingAsset execute(Command command) {
        BrandingAsset asset = brandingAssetRepositoryPort.findById(command.assetId())
                .orElseThrow(() -> new BrandingAssetNotFoundException(command.assetId().toString()));

        String extension = ImageUploadValidator.extensionFor(command.data(), command.mimeType());
        String previousKey = asset.getObjectKey();
        String key = "assets/" + asset.getId() + extension;

        objectStoragePort.put(key, command.data(), command.mimeType());
        asset.replace(key, command.mimeType(), command.adminId());
        BrandingAsset saved = brandingAssetRepositoryPort.save(asset);

        if (previousKey != null && !previousKey.equals(key)) {
            objectStoragePort.delete(previousKey);
        }
        return saved;
    }
}
