package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.EmailTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringEmailTemplateRepository extends JpaRepository<EmailTemplateJpaEntity, UUID> {
    Optional<EmailTemplateJpaEntity> findByTypeAndLanguage(EmailTemplateType type, String language);
}
