package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.LoadLicensesPort;
import com.skateboard.appconfig.application.port.out.SaveLicensesPort;
import com.skateboard.appconfig.domain.model.Licenses;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class LicensesPersistenceAdapter implements LoadLicensesPort, SaveLicensesPort {

    private final SpringLicensesRepository jpaRepository;

    public LicensesPersistenceAdapter(SpringLicensesRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Licenses> find() {
        return Optional.ofNullable(jpaRepository.findSingleton()).map(this::toDomain);
    }

    @Override
    public Licenses save(Licenses page) {
        LicensesJpaEntity existing = jpaRepository.findById(page.getId()).orElse(null);
        LicensesJpaEntity entity = existing != null ? existing : new LicensesJpaEntity();
        if (existing == null) {
            entity.setCreatedAt(page.getCreatedAt() != null ? page.getCreatedAt() : Instant.now());
        }
        entity.setId(page.getId());
        entity.setTitle(page.getTitle());
        entity.setBody(page.getBody());
        entity.setStatus(page.getStatus().name().toLowerCase());
        entity.setUpdatedBy(page.getUpdatedBy());
        entity.setUpdatedAt(page.getUpdatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    private Licenses toDomain(LicensesJpaEntity e) {
        return Licenses.reconstitute(
                e.getId(),
                e.getTitle(),
                e.getBody(),
                Licenses.Status.valueOf(e.getStatus().toUpperCase()),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getUpdatedBy());
    }
}
