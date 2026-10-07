package com.skateboard.appconfig.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuestApplicationConfigTest {

    @Test
    void createDefaultsIsDisabledWithNoRecipients() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getRecipientIds()).isEmpty();
    }

    @Test
    void enablingWithAtLeastOneRecipientSucceeds() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();
        UUID recipient = UUID.randomUUID();

        config.update(true, Set.of(recipient), "admin-1");

        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getRecipientIds()).containsExactly(recipient);
        assertThat(config.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void enablingWithNoRecipientsIsRejected() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        assertThatThrownBy(() -> config.update(true, Set.of(), "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void disablingDoesNotRequireRecipients() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();

        config.update(false, Set.of(), "admin-1");

        assertThat(config.isEnabled()).isFalse();
    }

    @Test
    void reconstituteRehydratesEveryField() {
        UUID id = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(60);
        Instant updatedAt = Instant.now();

        GuestApplicationConfig config = GuestApplicationConfig.reconstitute(id, true, Set.of(recipient),
                createdAt, updatedAt, "admin-1");

        assertThat(config.getId()).isEqualTo(id);
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getRecipientIds()).containsExactly(recipient);
        assertThat(config.getCreatedAt()).isEqualTo(createdAt);
        assertThat(config.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(config.getUpdatedBy()).isEqualTo("admin-1");
    }
}
