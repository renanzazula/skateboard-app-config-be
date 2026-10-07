package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetEmailTemplateUseCase;
import com.skateboard.appconfig.application.port.out.EmailTemplateRepositoryPort;
import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.domain.model.EmailTemplateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetEmailTemplateServiceTest {

    @Mock
    private EmailTemplateRepositoryPort emailTemplateRepositoryPort;

    private GetEmailTemplateService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetEmailTemplateService(emailTemplateRepositoryPort);
    }

    @Test
    void returnsTheExistingRowWithoutSaving() {
        EmailTemplate existing = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");
        when(emailTemplateRepositoryPort.findByTypeAndLanguage(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en"))
                .thenReturn(Optional.of(existing));

        EmailTemplate result = service.execute(
                new GetEmailTemplateUseCase.Query(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en"));

        assertThat(result).isSameAs(existing);
        verify(emailTemplateRepositoryPort, never()).save(any());
    }

    @Test
    void materializesAndSavesADefaultRowWhenNoneExists() {
        when(emailTemplateRepositoryPort.findByTypeAndLanguage(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "pt"))
                .thenReturn(Optional.empty());
        when(emailTemplateRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmailTemplate result = service.execute(
                new GetEmailTemplateUseCase.Query(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "pt"));

        assertThat(result.getLanguage()).isEqualTo("pt");
        assertThat(result.getSubject()).isEqualTo(EmailTemplateType.GUEST_APPLICATION_RECEIVED.defaultSubject());

        ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
        verify(emailTemplateRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EmailTemplateType.GUEST_APPLICATION_RECEIVED);
    }
}
