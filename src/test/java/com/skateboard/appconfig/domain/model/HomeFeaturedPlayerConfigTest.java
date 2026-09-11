package com.skateboard.appconfig.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HomeFeaturedPlayerConfigTest {

    @Test
    void createDefaultsUsesTopYoutubeAndManualForANewConfiguration() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();

        assertThat(config.getPosition()).isEqualTo(HomeFeaturedPlayerConfig.Position.TOP);
        assertThat(config.getPreferredPlatform()).isEqualTo(HomeFeaturedPlayerConfig.PreferredPlatform.YOUTUBE);
        assertThat(config.getSelectionMode()).isEqualTo(HomeFeaturedPlayerConfig.SelectionMode.MANUAL);
    }

    /**
     * A row persisted before this field existed (or written back with a null
     * out of an incomplete caller) must not silently switch to AUTO — MANUAL
     * is the only mode that ever existed, so it's the safe fallback.
     */
    @Test
    void reconstituteWithNoSelectionModeDefaultsToManual() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.reconstitute(UUID.randomUUID(), true,
                FeaturedContentSource.PODCAST, "post-1", HomeFeaturedPlayerConfig.PlayerType.MINI,
                HomeFeaturedPlayerConfig.Position.BOTTOM, null, null, Instant.now(), "admin-1");

        assertThat(config.getSelectionMode()).isEqualTo(HomeFeaturedPlayerConfig.SelectionMode.MANUAL);
    }

    @Test
    void updateToAutoClearsAnyContentId() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();

        config.update(true, FeaturedContentSource.PODCAST, "post-1", HomeFeaturedPlayerConfig.PlayerType.MINI,
                HomeFeaturedPlayerConfig.Position.TOP, null, HomeFeaturedPlayerConfig.SelectionMode.AUTO);

        assertThat(config.getContentId()).isNull();
        assertThat(config.getSelectionMode()).isEqualTo(HomeFeaturedPlayerConfig.SelectionMode.AUTO);
    }

    @Test
    void updateToManualStillRequiresAContentId() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();

        assertThatThrownBy(() -> config.update(true, FeaturedContentSource.PODCAST, null,
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.TOP, null,
                HomeFeaturedPlayerConfig.SelectionMode.MANUAL))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateToAutoStillRequiresAContentSource() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();

        assertThatThrownBy(() -> config.update(true, null, null,
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.TOP, null,
                HomeFeaturedPlayerConfig.SelectionMode.AUTO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
