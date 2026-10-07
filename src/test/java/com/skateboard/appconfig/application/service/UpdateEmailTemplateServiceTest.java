package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateEmailTemplateUseCase;
import com.skateboard.appconfig.application.port.out.EmailTemplateRepositoryPort;
import com.skateboard.appconfig.domain.exception.EmailTemplateNotFoundException;
import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.domain.model.EmailTemplateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateEmailTemplateServiceTest {

    @Mock
    private EmailTemplateRepositoryPort emailTemplateRepositoryPort;

    private UpdateEmailTemplateService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UpdateEmailTemplateService(emailTemplateRepositoryPort);
    }

    @Test
    void updatesAndPersistsAnExistingTemplate() {
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");
        when(emailTemplateRepositoryPort.findById(template.getId())).thenReturn(Optional.of(template));
        when(emailTemplateRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmailTemplate updated = service.execute(new UpdateEmailTemplateUseCase.Command(
                "admin-1", template.getId(), "New subject {{name}}", "New body", false));

        assertThat(updated.getSubject()).isEqualTo("New subject {{name}}");
        assertThat(updated.getBody()).isEqualTo("New body");
        assertThat(updated.isEnabled()).isFalse();
        assertThat(updated.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void anUnknownIdIsRejectedAndNotSaved() {
        UUID id = UUID.randomUUID();
        when(emailTemplateRepositoryPort.findById(id)).thenReturn(Optional.empty());

        UpdateEmailTemplateUseCase.Command command =
                new UpdateEmailTemplateUseCase.Command("admin-1", id, "Subject", "Body", true);

        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(EmailTemplateNotFoundException.class);
        verify(emailTemplateRepositoryPort, never()).save(any());
    }
}
