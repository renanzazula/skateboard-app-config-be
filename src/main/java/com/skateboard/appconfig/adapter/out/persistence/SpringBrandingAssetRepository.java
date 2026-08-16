package com.skateboard.appconfig.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringBrandingAssetRepository extends JpaRepository<BrandingAssetJpaEntity, UUID> {
    Optional<BrandingAssetJpaEntity> findByName(String name);
    boolean existsByName(String name);
}
