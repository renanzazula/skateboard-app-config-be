package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.BrandingAsset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandingAssetRepositoryPort {
    BrandingAsset save(BrandingAsset asset);
    Optional<BrandingAsset> findById(UUID id);
    Optional<BrandingAsset> findByName(String name);
    List<BrandingAsset> findAll();
    void deleteById(UUID id);
    boolean existsByName(String name);
}
