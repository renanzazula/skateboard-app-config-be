package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetEmailTemplateUseCase;
import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.domain.model.EmailTemplateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

class GetEmailTemplatesServiceTest {

    @Mock
    private GetEmailTemplateUseCase getEmailTemplateUseCase;

    private GetEmailTemplatesService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetEmailTemplatesService(getEmailTemplateUseCase);
    }

    @Test
    void returnsOneRowPerTypeAndSupportedLanguage() {
        when(getEmailTemplateUseCase.execute(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> {
            GetEmailTemplateUseCase.Query query = inv.getArgument(0);
            return EmailTemplate.createDefault(query.type(), query.language());
        });

        List<EmailTemplate> templates = service.execute();

        assertThat(templates).hasSize(EmailTemplateType.values().length * EmailTemplate.SUPPORTED_LANGUAGES.size());
        assertThat(templates)
                .extracting(EmailTemplate::getType, EmailTemplate::getLanguage)
                .contains(
                        tuple(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en"),
                        tuple(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "es"),
                        tuple(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "pt"),
                        tuple(EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION, "en"),
                        tuple(EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION, "es"),
                        tuple(EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION, "pt"));
    }
}
