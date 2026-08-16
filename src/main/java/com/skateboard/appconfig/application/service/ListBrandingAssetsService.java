package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ListBrandingAssetsUseCase;
import com.skateboard.appconfig.application.port.out.BrandingAssetRepositoryPort;
import com.skateboard.appconfig.domain.model.BrandingAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListBrandingAssetsService implements ListBrandingAssetsUseCase {

    private final BrandingAssetRepositoryPort brandingAssetRepositoryPort;

    public ListBrandingAssetsService(BrandingAssetRepositoryPort brandingAssetRepositoryPort) {
        this.brandingAssetRepositoryPort = brandingAssetRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandingAsset> execute() {
        return brandingAssetRepositoryPort.findAll();
    }
}
