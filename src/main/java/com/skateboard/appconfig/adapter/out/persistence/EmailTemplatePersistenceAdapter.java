package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.EmailTemplateRepositoryPort;
import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.domain.model.EmailTemplateType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmailTemplatePersistenceAdapter implements EmailTemplateRepositoryPort {

    private final SpringEmailTemplateRepository jpaRepository;

    public EmailTemplatePersistenceAdapter(SpringEmailTemplateRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<EmailTemplate> findByTypeAndLanguage(EmailTemplateType type, String language) {
        return jpaRepository.findByTypeAndLanguage(type, language).map(this::toDomain);
    }

    @Override
    public Optional<EmailTemplate> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public EmailTemplate save(EmailTemplate template) {
        EmailTemplateJpaEntity existing = jpaRepository.findById(template.getId()).orElse(null);
        EmailTemplateJpaEntity entity = existing != null ? existing : new EmailTemplateJpaEntity();
        if (existing == null) {
            entity.setCreatedAt(template.getCreatedAt() != null ? template.getCreatedAt() : Instant.now());
        }
        entity.setId(template.getId());
        entity.setType(template.getType());
        entity.setLanguage(template.getLanguage());
        entity.setSubject(template.getSubject());
        entity.setBody(template.getBody());
        entity.setEnabled(template.isEnabled());
        entity.setUpdatedBy(template.getUpdatedBy());
        // Copies the domain's own updatedAt (null until update() runs)
        // rather than stamping now() — mirrors GuestApplicationConfigPersistenceAdapter:
        // the default-materialization save on first GET must not look like
        // an admin change.
        entity.setUpdatedAt(template.getUpdatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    private EmailTemplate toDomain(EmailTemplateJpaEntity e) {
        return EmailTemplate.reconstitute(e.getId(), e.getType(), e.getLanguage(), e.getSubject(), e.getBody(),
                e.isEnabled(), e.getCreatedAt(), e.getUpdatedAt(), e.getUpdatedBy());
    }
}
