package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.application.port.out.SaveAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AppConfigPersistenceAdapter implements LoadAppConfigPort, SaveAppConfigPort {

    private final SpringAppConfigRepository jpaRepository;

    public AppConfigPersistenceAdapter(SpringAppConfigRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AppConfig getOrCreate() {
        AppConfigJpaEntity existing = jpaRepository.findSingleton();
        if (existing != null) {
            return toDomain(existing);
        }
        return save(AppConfig.createDefaults());
    }

    @Override
    public AppConfig save(AppConfig config) {
        AppConfigJpaEntity entity = toEntity(config);
        return toDomain(jpaRepository.save(entity));
    }

    private AppConfig toDomain(AppConfigJpaEntity e) {
        return AppConfig.reconstitute(e.getId(), e.getLoginBackgroundKey(), e.getLoginBackgroundVersion(),
                e.getLoginBackgroundUpdatedAt(), e.getAppLogoKey(), e.getAppLogoVersion(),
                e.getAppLogoUpdatedAt(), e.getLoginTitle(), e.getLoginMessage(), e.getUpdatedBy());
    }

    private AppConfigJpaEntity toEntity(AppConfig config) {
        AppConfigJpaEntity existing = jpaRepository.findById(config.getId()).orElse(null);
        AppConfigJpaEntity e = existing != null ? existing : new AppConfigJpaEntity();
        Instant now = Instant.now();
        if (existing == null) {
            e.setCreatedAt(now);
        }
        e.setId(config.getId());
        e.setLoginBackgroundKey(config.getLoginBackgroundKey());
        e.setLoginBackgroundVersion(config.getLoginBackgroundVersion());
        e.setLoginBackgroundUpdatedAt(config.getLoginBackgroundUpdatedAt());
        e.setAppLogoKey(config.getAppLogoKey());
        e.setAppLogoVersion(config.getAppLogoVersion());
        e.setAppLogoUpdatedAt(config.getAppLogoUpdatedAt());
        e.setLoginTitle(config.getLoginTitle());
        e.setLoginMessage(config.getLoginMessage());
        e.setUpdatedBy(config.getUpdatedBy());
        e.setUpdatedAt(now);
        return e;
    }
}
