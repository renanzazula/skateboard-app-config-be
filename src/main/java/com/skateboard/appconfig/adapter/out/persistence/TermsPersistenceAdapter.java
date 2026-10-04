package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.LoadTermsPort;
import com.skateboard.appconfig.application.port.out.SaveTermsPort;
import com.skateboard.appconfig.domain.model.Terms;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class TermsPersistenceAdapter implements LoadTermsPort, SaveTermsPort {

    private final SpringTermsRepository jpaRepository;

    public TermsPersistenceAdapter(SpringTermsRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Terms> find() {
        return Optional.ofNullable(jpaRepository.findSingleton()).map(this::toDomain);
    }

    @Override
    public Terms save(Terms page) {
        TermsJpaEntity existing = jpaRepository.findById(page.getId()).orElse(null);
        TermsJpaEntity entity = existing != null ? existing : new TermsJpaEntity();
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

    private Terms toDomain(TermsJpaEntity e) {
        return Terms.reconstitute(
                e.getId(),
                e.getTitle(),
                e.getBody(),
                Terms.Status.valueOf(e.getStatus().toUpperCase()),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getUpdatedBy());
    }
}
