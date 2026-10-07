package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateEmailTemplateUseCase;
import com.skateboard.appconfig.application.port.out.EmailTemplateRepositoryPort;
import com.skateboard.appconfig.domain.exception.EmailTemplateNotFoundException;
import com.skateboard.appconfig.domain.model.EmailTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEmailTemplateService implements UpdateEmailTemplateUseCase {

    private final EmailTemplateRepositoryPort emailTemplateRepositoryPort;

    public UpdateEmailTemplateService(EmailTemplateRepositoryPort emailTemplateRepositoryPort) {
        this.emailTemplateRepositoryPort = emailTemplateRepositoryPort;
    }

    @Override
    @Transactional
    public EmailTemplate execute(Command command) {
        EmailTemplate template = emailTemplateRepositoryPort.findById(command.id())
                .orElseThrow(() -> new EmailTemplateNotFoundException(command.id().toString()));
        template.update(command.subject(), command.body(), command.enabled(), command.adminId());
        return emailTemplateRepositoryPort.save(template);
    }
}
