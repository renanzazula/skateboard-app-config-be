package com.skateboard.appconfig.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigTest {

    @Test
    void createDefaultsStartsEmpty() {
        AppConfig config = AppConfig.createDefaults();

        assertThat(config.getId()).isNotNull();
        assertThat(config.getLoginBackgroundKey()).isNull();
        assertThat(config.getLoginBackgroundVersion()).isZero();
        assertThat(config.getAppLogoKey()).isNull();
        assertThat(config.getAppLogoVersion()).isZero();
        assertThat(config.getLoginTitle()).isNull();
        assertThat(config.getLoginMessage()).isNull();
        assertThat(config.getUpdatedBy()).isNull();
    }

    @Test
    void updatingTheLoginBackgroundBumpsVersionAndTimestamp() {
        AppConfig config = AppConfig.createDefaults();

        config.updateLoginBackground("login/bg.png");

        assertThat(config.getLoginBackgroundKey()).isEqualTo("login/bg.png");
        assertThat(config.getLoginBackgroundVersion()).isEqualTo(1);
        assertThat(config.getLoginBackgroundUpdatedAt()).isNotNull();
    }

    @Test
    void updatingTheAppLogoBumpsVersionAndTimestamp() {
        AppConfig config = AppConfig.createDefaults();

        config.updateAppLogo("logo/app-logo.png");

        assertThat(config.getAppLogoKey()).isEqualTo("logo/app-logo.png");
        assertThat(config.getAppLogoVersion()).isEqualTo(1);
        assertThat(config.getAppLogoUpdatedAt()).isNotNull();
    }

    @Test
    void touchRecordsTheActor() {
        AppConfig config = AppConfig.createDefaults();

        config.touch("admin-9");

        assertThat(config.getUpdatedBy()).isEqualTo("admin-9");
    }

    @Test
    void reconstituteRestoresEveryField() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        AppConfig config = AppConfig.reconstitute(id, "login/bg.png", 2, now, "logo/app-logo.png", 3, now,
                "Welcome", "Please log in", "admin-1");

        assertThat(config.getId()).isEqualTo(id);
        assertThat(config.getLoginBackgroundKey()).isEqualTo("login/bg.png");
        assertThat(config.getLoginBackgroundVersion()).isEqualTo(2);
        assertThat(config.getLoginBackgroundUpdatedAt()).isEqualTo(now);
        assertThat(config.getAppLogoKey()).isEqualTo("logo/app-logo.png");
        assertThat(config.getAppLogoVersion()).isEqualTo(3);
        assertThat(config.getAppLogoUpdatedAt()).isEqualTo(now);
        assertThat(config.getLoginTitle()).isEqualTo("Welcome");
        assertThat(config.getLoginMessage()).isEqualTo("Please log in");
        assertThat(config.getUpdatedBy()).isEqualTo("admin-1");
    }
}
