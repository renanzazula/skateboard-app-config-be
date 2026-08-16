package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.BrandingAssetRepositoryPort;
import com.skateboard.appconfig.domain.model.BrandingAsset;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BrandingAssetPersistenceAdapter implements BrandingAssetRepositoryPort {

    private final SpringBrandingAssetRepository jpaRepository;

    public BrandingAssetPersistenceAdapter(SpringBrandingAssetRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BrandingAsset save(BrandingAsset asset) {
        BrandingAssetJpaEntity entity = toEntity(asset);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<BrandingAsset> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<BrandingAsset> findByName(String name) {
        return jpaRepository.findByName(name).map(this::toDomain);
    }

    @Override
    public List<BrandingAsset> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    private BrandingAsset toDomain(BrandingAssetJpaEntity e) {
        return BrandingAsset.reconstitute(e.getId(), e.getName(), e.getObjectKey(), e.getContentType(),
                e.getVersion(), e.getCreatedAt(), e.getUpdatedAt(), e.getUpdatedBy());
    }

    private BrandingAssetJpaEntity toEntity(BrandingAsset asset) {
        BrandingAssetJpaEntity e = new BrandingAssetJpaEntity();
        e.setId(asset.getId());
        e.setName(asset.getName());
        e.setObjectKey(asset.getObjectKey());
        e.setContentType(asset.getContentType());
        e.setVersion(asset.getVersion());
        e.setUpdatedBy(asset.getUpdatedBy());
        e.setCreatedAt(asset.getCreatedAt());
        e.setUpdatedAt(asset.getUpdatedAt());
        return e;
    }
}
