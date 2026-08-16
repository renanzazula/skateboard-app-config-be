package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.RemoveBrandingAssetUseCase;
import com.skateboard.appconfig.application.port.out.BrandingAssetRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.BrandingAssetNotFoundException;
import com.skateboard.appconfig.domain.model.BrandingAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveBrandingAssetService implements RemoveBrandingAssetUseCase {

    private final BrandingAssetRepositoryPort brandingAssetRepositoryPort;
    private final ObjectStoragePort objectStoragePort;

    public RemoveBrandingAssetService(BrandingAssetRepositoryPort brandingAssetRepositoryPort,
                                       ObjectStoragePort objectStoragePort) {
        this.brandingAssetRepositoryPort = brandingAssetRepositoryPort;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @Transactional
    public void execute(Command command) {
        BrandingAsset asset = brandingAssetRepositoryPort.findById(command.assetId())
                .orElseThrow(() -> new BrandingAssetNotFoundException(command.assetId().toString()));

        brandingAssetRepositoryPort.deleteById(asset.getId());
        objectStoragePort.delete(asset.getObjectKey());
    }
}
