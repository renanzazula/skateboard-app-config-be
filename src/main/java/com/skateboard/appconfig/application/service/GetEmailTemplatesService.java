package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetEmailTemplateUseCase;
import com.skateboard.appconfig.application.port.in.GetEmailTemplatesUseCase;
import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.domain.model.EmailTemplateType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Materializes the full (type × language) matrix via {@link GetEmailTemplateUseCase}
 * rather than duplicating its find-or-create-default logic.
 */
@Service
public class GetEmailTemplatesService implements GetEmailTemplatesUseCase {

    private final GetEmailTemplateUseCase getEmailTemplateUseCase;

    public GetEmailTemplatesService(GetEmailTemplateUseCase getEmailTemplateUseCase) {
        this.getEmailTemplateUseCase = getEmailTemplateUseCase;
    }

    @Override
    @Transactional
    public List<EmailTemplate> execute() {
        List<EmailTemplate> templates = new ArrayList<>();
        for (EmailTemplateType type : EmailTemplateType.values()) {
            for (String language : EmailTemplate.SUPPORTED_LANGUAGES) {
                templates.add(getEmailTemplateUseCase.execute(new GetEmailTemplateUseCase.Query(type, language)));
            }
        }
        return templates;
    }
}
