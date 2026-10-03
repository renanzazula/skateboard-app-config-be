package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.LoadPrivacyPolicyPort;
import com.skateboard.appconfig.application.port.out.SavePrivacyPolicyPort;
import com.skateboard.appconfig.domain.model.PrivacyPolicy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PrivacyPolicyPersistenceAdapter implements LoadPrivacyPolicyPort, SavePrivacyPolicyPort {

    private final SpringPrivacyPolicyRepository jpaRepository;

    public PrivacyPolicyPersistenceAdapter(SpringPrivacyPolicyRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PrivacyPolicy> find() {
        return Optional.ofNullable(jpaRepository.findSingleton()).map(this::toDomain);
    }

    @Override
    public PrivacyPolicy save(PrivacyPolicy page) {
        PrivacyPolicyJpaEntity existing = jpaRepository.findById(page.getId()).orElse(null);
        PrivacyPolicyJpaEntity entity = existing != null ? existing : new PrivacyPolicyJpaEntity();
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

    private PrivacyPolicy toDomain(PrivacyPolicyJpaEntity e) {
        return PrivacyPolicy.reconstitute(
                e.getId(),
                e.getTitle(),
                e.getBody(),
                PrivacyPolicy.Status.valueOf(e.getStatus().toUpperCase()),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getUpdatedBy());
    }
}
