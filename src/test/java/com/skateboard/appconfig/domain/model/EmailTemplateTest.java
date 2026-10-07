package com.skateboard.appconfig.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTemplateTest {

    @Test
    void createDefaultUsesTheTypesDefaultCopyAndIsEnabled() {
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");

        assertThat(template.getType()).isEqualTo(EmailTemplateType.GUEST_APPLICATION_RECEIVED);
        assertThat(template.getLanguage()).isEqualTo("en");
        assertThat(template.getSubject()).isEqualTo(EmailTemplateType.GUEST_APPLICATION_RECEIVED.defaultSubject());
        assertThat(template.getBody()).isEqualTo(EmailTemplateType.GUEST_APPLICATION_RECEIVED.defaultBody());
        assertThat(template.isEnabled()).isTrue();
    }

    @Test
    void createDefaultRejectsAnUnsupportedLanguage() {
        assertThatThrownBy(() -> EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "fr"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateAcceptsTheTypesSupportedVariable() {
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");

        template.update("Hi {{name}}", "Hello {{name}}, thanks!", true, "admin-1");

        assertThat(template.getSubject()).isEqualTo("Hi {{name}}");
        assertThat(template.getBody()).isEqualTo("Hello {{name}}, thanks!");
        assertThat(template.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void updateRejectsAnUnsupportedVariable() {
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");

        assertThatThrownBy(() -> template.update("Hi {{firstName}}", "Body", true, "admin-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("{{firstName}}");
    }

    @Test
    void updateRejectsAnUnsupportedVariableInTheBody() {
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");

        assertThatThrownBy(() -> template.update("Subject", "Hi {{lastName}}", true, "admin-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("{{lastName}}");
    }

    @Test
    void updateRejectsASingleBracePlaceholderAsPlainText() {
        // {{name}} is the only recognized syntax now — a stray {name} is just
        // literal text, not rejected, since the regex only matches {{...}}.
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");

        template.update("Hi {name}", "Body", true, "admin-1");

        assertThat(template.getSubject()).isEqualTo("Hi {name}");
    }

    @Test
    void blankSubjectIsRejected() {
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");

        assertThatThrownBy(() -> template.update("  ", "Body", true, "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankBodyIsRejected() {
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");

        assertThatThrownBy(() -> template.update("Subject", " ", true, "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstituteRehydratesEveryField() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(60);
        Instant updatedAt = Instant.now();

        EmailTemplate template = EmailTemplate.reconstitute(id, EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION,
                "es", "Subject", "Body", false, createdAt, updatedAt, "admin-1");

        assertThat(template.getId()).isEqualTo(id);
        assertThat(template.getType()).isEqualTo(EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION);
        assertThat(template.getLanguage()).isEqualTo("es");
        assertThat(template.getSubject()).isEqualTo("Subject");
        assertThat(template.getBody()).isEqualTo("Body");
        assertThat(template.isEnabled()).isFalse();
        assertThat(template.getCreatedAt()).isEqualTo(createdAt);
        assertThat(template.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(template.getUpdatedBy()).isEqualTo("admin-1");
    }
}
