package com.skateboard.appconfig.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuestApplicationConfigTest {

    @Test
    void createDefaultsIsDisabledWithNoRecipientsAndTheSuggestedDefaultMessage() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getRecipientIds()).isEmpty();
        assertThat(config.getConfirmationSubject()).isEqualTo(GuestApplicationConfig.DEFAULT_CONFIRMATION_SUBJECT);
        assertThat(config.getConfirmationBody()).isEqualTo(GuestApplicationConfig.DEFAULT_CONFIRMATION_BODY);
    }

    @Test
    void enablingWithAtLeastOneRecipientSucceeds() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();
        UUID recipient = UUID.randomUUID();

        config.update(true, Set.of(recipient), "Subject", "Body", "admin-1");

        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getRecipientIds()).containsExactly(recipient);
        assertThat(config.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void enablingWithNoRecipientsIsRejected() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        assertThatThrownBy(() -> config.update(true, Set.of(), "Subject", "Body", "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void disablingDoesNotRequireRecipients() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        config.update(false, Set.of(), "Subject", "Body", "admin-1");

        assertThat(config.isEnabled()).isFalse();
    }

    @Test
    void blankSubjectIsRejected() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        assertThatThrownBy(() -> config.update(false, Set.of(), "  ", "Body", "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankBodyIsRejected() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        assertThatThrownBy(() -> config.update(false, Set.of(), "Subject", " ", "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theNamePlaceholderIsAccepted() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        config.update(false, Set.of(), "Hi {name}", "Hello {name}, thanks!", "admin-1");

        assertThat(config.getConfirmationSubject()).isEqualTo("Hi {name}");
    }

    @Test
    void anUnsupportedPlaceholderIsRejected() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        assertThatThrownBy(() -> config.update(false, Set.of(), "Hi {firstName}", "Body", "admin-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("{firstName}");
    }

    @Test
    void anUnsupportedPlaceholderInTheBodyIsRejected() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        assertThatThrownBy(() -> config.update(false, Set.of(), "Subject", "Hi {lastName}", "admin-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("{lastName}");
    }

    @Test
    void reconstituteRehydratesEveryField() {
        UUID id = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(60);
        Instant updatedAt = Instant.now();

        GuestApplicationConfig config = GuestApplicationConfig.reconstitute(id, true, Set.of(recipient),
                "Subject", "Body", createdAt, updatedAt, "admin-1");

        assertThat(config.getId()).isEqualTo(id);
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getRecipientIds()).containsExactly(recipient);
        assertThat(config.getConfirmationSubject()).isEqualTo("Subject");
        assertThat(config.getConfirmationBody()).isEqualTo("Body");
        assertThat(config.getCreatedAt()).isEqualTo(createdAt);
        assertThat(config.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(config.getUpdatedBy()).isEqualTo("admin-1");
    }
}
