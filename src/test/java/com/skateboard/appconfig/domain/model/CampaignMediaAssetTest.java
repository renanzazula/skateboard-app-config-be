package com.skateboard.appconfig.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampaignMediaAssetTest {

    @Test
    void createStampsVersionAndCreatedAt() {
        CampaignMediaAsset asset = CampaignMediaAsset.create(UUID.randomUUID(), 1,
                "campaigns/c1/s1.webp", "image/webp", 1080, 1920, 512_000L, 0.5, 0.4);

        assertThat(asset.getVersion()).isEqualTo(1);
        assertThat(asset.getStorageKey()).isEqualTo("campaigns/c1/s1.webp");
        assertThat(asset.getMimeType()).isEqualTo("image/webp");
        assertThat(asset.getWidth()).isEqualTo(1080);
        assertThat(asset.getHeight()).isEqualTo(1920);
        assertThat(asset.getSizeBytes()).isEqualTo(512_000L);
        assertThat(asset.getFocalPointX()).isEqualTo(0.5);
        assertThat(asset.getFocalPointY()).isEqualTo(0.4);
        assertThat(asset.getCreatedAt()).isNotNull();
    }

    @Test
    void allowsUnknownDimensionsAndFocalPoint() {
        CampaignMediaAsset asset = CampaignMediaAsset.create(UUID.randomUUID(), 1,
                "campaigns/c1/s1.webp", "image/webp", null, null, null, null, null);

        assertThat(asset.getWidth()).isNull();
        assertThat(asset.getFocalPointX()).isNull();
    }

    @Test
    void rejectsBlankStorageKey() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> CampaignMediaAsset.create(id, 1, "  ", "image/webp",
                null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveVersion() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> CampaignMediaAsset.create(id, 0, "campaigns/c1/s1.webp",
                "image/webp", null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsFocalPointOutsideUnitInterval() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> CampaignMediaAsset.create(id, 1, "campaigns/c1/s1.webp",
                "image/webp", null, null, null, 1.2, 0.5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CampaignMediaAsset.create(id, 1, "campaigns/c1/s1.webp",
                "image/webp", null, null, null, 0.5, -0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
