package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetEmailTemplateUseCase;
import com.skateboard.appconfig.application.port.in.GetEmailTemplatesUseCase;
import com.skateboard.appconfig.application.port.in.UpdateEmailTemplateUseCase;
import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.infrastructure.web.dto.EmailTemplateResponse;
import com.skateboard.appconfig.infrastructure.web.dto.UpdateEmailTemplateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Exercises the real openapi-generator enum
 * ({@code infrastructure.web.dto.EmailTemplateType}) on the response side
 * and real path-variable strings on the request side — this combination is
 * exactly what caught two bugs during development: mapping the response
 * type via {@code .name()}/{@code .valueOf()} instead of
 * {@code getValue()}/{@code fromValue()} (compiles, throws at runtime), and
 * declaring the {@code type} path parameter as the generated enum instead of
 * a plain String (Spring's default path-variable enum conversion is
 * {@code Enum.valueOf} against the Java constant name, and the generator
 * strips the shared "GUEST_APPLICATION_" prefix from those names — so a real
 * caller sending the full value, as every client here does, would 400).
 */
class EmailTemplateControllerTest {

    @Mock private GetEmailTemplatesUseCase getEmailTemplatesUseCase;
    @Mock private GetEmailTemplateUseCase getEmailTemplateUseCase;
    @Mock private UpdateEmailTemplateUseCase updateEmailTemplateUseCase;

    private EmailTemplateController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new EmailTemplateController(getEmailTemplatesUseCase, getEmailTemplateUseCase,
                updateEmailTemplateUseCase);
    }

    @Test
    void getEmailTemplateMapsTheDtoTypeToTheDomainTypeAndBack() {
        EmailTemplate template = EmailTemplate.createDefault(
                com.skateboard.appconfig.domain.model.EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION, "en");
        when(getEmailTemplateUseCase.execute(eq(new GetEmailTemplateUseCase.Query(
                com.skateboard.appconfig.domain.model.EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION, "en"))))
                .thenReturn(template);

        ResponseEntity<EmailTemplateResponse> response =
                controller.getEmailTemplate("GUEST_APPLICATION_ADMIN_NOTIFICATION", "en");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.EmailTemplateType.ADMIN_NOTIFICATION);
        assertThat(response.getBody().getLanguage()).isEqualTo("en");
    }

    @Test
    void getEmailTemplateRejectsAnUnknownTypeString() {
        assertThatThrownBy(() -> controller.getEmailTemplate("NOT_A_REAL_TYPE", "en"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NOT_A_REAL_TYPE");
    }

    @Test
    void listEmailTemplatesMapsEveryDomainTypeToItsDtoType() {
        EmailTemplate received = EmailTemplate.createDefault(
                com.skateboard.appconfig.domain.model.EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");
        when(getEmailTemplatesUseCase.execute()).thenReturn(List.of(received));

        ResponseEntity<List<EmailTemplateResponse>> response = controller.listEmailTemplates();

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getType())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.EmailTemplateType.RECEIVED);
    }

    @Test
    void updateEmailTemplateReturnsTheMappedResponse() {
        UUID id = UUID.randomUUID();
        EmailTemplate template = EmailTemplate.createDefault(
                com.skateboard.appconfig.domain.model.EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");
        when(updateEmailTemplateUseCase.execute(any())).thenReturn(template);
        UpdateEmailTemplateRequest request = new UpdateEmailTemplateRequest().subject("S").body("B").enabled(true);

        ResponseEntity<EmailTemplateResponse> response = controller.updateEmailTemplate(id, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.EmailTemplateType.RECEIVED);
    }
}
