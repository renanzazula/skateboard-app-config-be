package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.domain.model.EmailTemplateType;

import java.util.Optional;
import java.util.UUID;

public interface EmailTemplateRepositoryPort {
    Optional<EmailTemplate> findByTypeAndLanguage(EmailTemplateType type, String language);
    Optional<EmailTemplate> findById(UUID id);
    EmailTemplate save(EmailTemplate template);
}
