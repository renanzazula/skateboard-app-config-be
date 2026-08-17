package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.LoadHomeVideoCategoryConfigPort;
import com.skateboard.appconfig.application.port.out.SaveHomeVideoCategoryConfigPort;
import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashSet;

@Component
public class HomeVideoCategoryConfigPersistenceAdapter implements LoadHomeVideoCategoryConfigPort, SaveHomeVideoCategoryConfigPort {

    private final SpringHomeVideoCategoryConfigRepository jpaRepository;

    public HomeVideoCategoryConfigPersistenceAdapter(SpringHomeVideoCategoryConfigRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public HomeVideoCategoryConfig getOrCreate() {
        HomeVideoCategoryConfigJpaEntity existing = jpaRepository.findSingleton();
        if (existing != null) {
            return toDomain(existing);
        }
        return save(HomeVideoCategoryConfig.createDefaults());
    }

    @Override
    public HomeVideoCategoryConfig save(HomeVideoCategoryConfig config) {
        HomeVideoCategoryConfigJpaEntity entity = toEntity(config);
        return toDomain(jpaRepository.save(entity));
    }

    private HomeVideoCategoryConfig toDomain(HomeVideoCategoryConfigJpaEntity e) {
        return HomeVideoCategoryConfig.reconstitute(e.getId(), e.getMode(), e.getEnabledCategoryIds(),
                e.getUpdatedAt(), e.getUpdatedBy());
    }

    private HomeVideoCategoryConfigJpaEntity toEntity(HomeVideoCategoryConfig config) {
        HomeVideoCategoryConfigJpaEntity existing = jpaRepository.findById(config.getId()).orElse(null);
        HomeVideoCategoryConfigJpaEntity e = existing != null ? existing : new HomeVideoCategoryConfigJpaEntity();
        Instant now = Instant.now();
        if (existing == null) {
            e.setCreatedAt(now);
        }
        e.setId(config.getId());
        e.setMode(config.getMode());
        e.setEnabledCategoryIds(new LinkedHashSet<>(config.getEnabledCategoryIds()));
        e.setUpdatedBy(config.getUpdatedBy());
        e.setUpdatedAt(now);
        return e;
    }
}
