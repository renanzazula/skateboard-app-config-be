package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetEmailTemplateUseCase;
import com.skateboard.appconfig.application.port.out.EmailTemplateRepositoryPort;
import com.skateboard.appconfig.domain.model.EmailTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetEmailTemplateService implements GetEmailTemplateUseCase {

    private final EmailTemplateRepositoryPort emailTemplateRepositoryPort;

    public GetEmailTemplateService(EmailTemplateRepositoryPort emailTemplateRepositoryPort) {
        this.emailTemplateRepositoryPort = emailTemplateRepositoryPort;
    }

    @Override
    @Transactional
    public EmailTemplate execute(Query query) {
        return emailTemplateRepositoryPort.findByTypeAndLanguage(query.type(), query.language())
                .orElseGet(() -> emailTemplateRepositoryPort.save(
                        EmailTemplate.createDefault(query.type(), query.language())));
    }
}
