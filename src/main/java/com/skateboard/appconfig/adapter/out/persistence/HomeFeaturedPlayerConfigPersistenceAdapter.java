package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.LoadHomeFeaturedPlayerConfigPort;
import com.skateboard.appconfig.application.port.out.SaveHomeFeaturedPlayerConfigPort;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class HomeFeaturedPlayerConfigPersistenceAdapter implements LoadHomeFeaturedPlayerConfigPort, SaveHomeFeaturedPlayerConfigPort {

    private final SpringHomeFeaturedPlayerConfigRepository jpaRepository;

    public HomeFeaturedPlayerConfigPersistenceAdapter(SpringHomeFeaturedPlayerConfigRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public HomeFeaturedPlayerConfig getOrCreate() {
        HomeFeaturedPlayerConfigJpaEntity existing = jpaRepository.findSingleton();
        if (existing != null) {
            return toDomain(existing);
        }
        return save(HomeFeaturedPlayerConfig.createDefaults());
    }

    @Override
    public HomeFeaturedPlayerConfig save(HomeFeaturedPlayerConfig config) {
        HomeFeaturedPlayerConfigJpaEntity entity = toEntity(config);
        return toDomain(jpaRepository.save(entity));
    }

    private HomeFeaturedPlayerConfig toDomain(HomeFeaturedPlayerConfigJpaEntity e) {
        return HomeFeaturedPlayerConfig.reconstitute(e.getId(), e.isEnabled(), e.getContentSource(), e.getContentId(),
                e.getPlayerType(), e.getPosition(), e.getPreferredPlatform(), e.getUpdatedAt(), e.getUpdatedBy());
    }

    private HomeFeaturedPlayerConfigJpaEntity toEntity(HomeFeaturedPlayerConfig config) {
        HomeFeaturedPlayerConfigJpaEntity existing = jpaRepository.findById(config.getId()).orElse(null);
        HomeFeaturedPlayerConfigJpaEntity e = existing != null ? existing : new HomeFeaturedPlayerConfigJpaEntity();
        if (existing == null) {
            e.setCreatedAt(Instant.now());
        }
        e.setId(config.getId());
        e.setEnabled(config.isEnabled());
        e.setContentSource(config.getContentSource());
        e.setContentId(config.getContentId());
        e.setPlayerType(config.getPlayerType());
        e.setPosition(config.getPosition());
        e.setPreferredPlatform(config.getPreferredPlatform());
        e.setUpdatedBy(config.getUpdatedBy());
        // Copies the domain's own updatedAt (null until update() runs)
        // rather than stamping now() — the default-creation save on first
        // GET must not look like an admin change.
        e.setUpdatedAt(config.getUpdatedAt());
        return e;
    }
}
