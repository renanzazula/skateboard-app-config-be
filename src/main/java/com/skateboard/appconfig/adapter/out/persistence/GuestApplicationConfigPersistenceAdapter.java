package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.LoadGuestApplicationConfigPort;
import com.skateboard.appconfig.application.port.out.SaveGuestApplicationConfigPort;
import com.skateboard.appconfig.domain.model.GuestApplicationConfig;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashSet;

@Component
public class GuestApplicationConfigPersistenceAdapter implements LoadGuestApplicationConfigPort, SaveGuestApplicationConfigPort {

    private final SpringGuestApplicationConfigRepository jpaRepository;

    public GuestApplicationConfigPersistenceAdapter(SpringGuestApplicationConfigRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GuestApplicationConfig getOrCreate() {
        GuestApplicationConfigJpaEntity existing = jpaRepository.findSingleton();
        if (existing != null) {
            return toDomain(existing);
        }
        return save(GuestApplicationConfig.createDefaults());
    }

    @Override
    public GuestApplicationConfig save(GuestApplicationConfig config) {
        GuestApplicationConfigJpaEntity entity = toEntity(config);
        return toDomain(jpaRepository.save(entity));
    }

    private GuestApplicationConfig toDomain(GuestApplicationConfigJpaEntity e) {
        return GuestApplicationConfig.reconstitute(e.getId(), e.isEnabled(), e.getRecipientIds(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getUpdatedBy());
    }

    private GuestApplicationConfigJpaEntity toEntity(GuestApplicationConfig config) {
        GuestApplicationConfigJpaEntity existing = jpaRepository.findById(config.getId()).orElse(null);
        GuestApplicationConfigJpaEntity e = existing != null ? existing : new GuestApplicationConfigJpaEntity();
        if (existing == null) {
            e.setCreatedAt(config.getCreatedAt() != null ? config.getCreatedAt() : Instant.now());
        }
        e.setId(config.getId());
        e.setEnabled(config.isEnabled());
        e.setRecipientIds(new LinkedHashSet<>(config.getRecipientIds()));
        e.setUpdatedBy(config.getUpdatedBy());
        // Copies the domain's own updatedAt (null until update() runs)
        // rather than stamping now() — the default-creation save on first
        // GET must not look like an admin change.
        e.setUpdatedAt(config.getUpdatedAt());
        return e;
    }
}
