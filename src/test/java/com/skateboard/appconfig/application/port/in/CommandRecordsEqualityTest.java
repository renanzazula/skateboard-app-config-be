package com.skateboard.appconfig.application.port.in;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These upload {@code Command} records carry a {@code byte[]} field, so they
 * hand-write {@code equals}/{@code hashCode}/{@code toString} instead of using
 * the record-generated ones (which would compare the array by reference).
 * Nothing else in the codebase exercises those hand-written methods, so this
 * covers them directly.
 */
class CommandRecordsEqualityTest {

    @Test
    void uploadAboutImageCommand() {
        UploadAboutImageUseCase.Command a = new UploadAboutImageUseCase.Command(new byte[]{1, 2}, "image/png");
        UploadAboutImageUseCase.Command same = new UploadAboutImageUseCase.Command(new byte[]{1, 2}, "image/png");
        UploadAboutImageUseCase.Command differentData = new UploadAboutImageUseCase.Command(new byte[]{9}, "image/png");
        UploadAboutImageUseCase.Command differentMime = new UploadAboutImageUseCase.Command(new byte[]{1, 2}, "image/webp");

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(same);
        assertThat(a).hasSameHashCodeAs(same);
        assertThat(a).isNotEqualTo(differentData);
        assertThat(a).isNotEqualTo(differentMime);
        assertThat(a).isNotEqualTo("not a command");
        assertThat(a.toString()).contains("data=byte[2]").contains("mimeType=image/png");
        assertThat(new UploadAboutImageUseCase.Command(null, null).toString()).contains("data=byte[0]");
    }

    @Test
    void uploadAppLogoCommand() {
        UploadAppLogoUseCase.Command a = new UploadAppLogoUseCase.Command("admin-1", new byte[]{1, 2}, "image/png");
        UploadAppLogoUseCase.Command same = new UploadAppLogoUseCase.Command("admin-1", new byte[]{1, 2}, "image/png");
        UploadAppLogoUseCase.Command differentAdmin = new UploadAppLogoUseCase.Command("admin-2", new byte[]{1, 2}, "image/png");

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(same);
        assertThat(a).hasSameHashCodeAs(same);
        assertThat(a).isNotEqualTo(differentAdmin);
        assertThat(a).isNotEqualTo(null);
        assertThat(a.toString()).contains("adminId=admin-1").contains("data=byte[2]");
    }

    @Test
    void uploadLoginBackgroundCommand() {
        UploadLoginBackgroundUseCase.Command a = new UploadLoginBackgroundUseCase.Command("admin-1", new byte[]{1}, "image/png");
        UploadLoginBackgroundUseCase.Command same = new UploadLoginBackgroundUseCase.Command("admin-1", new byte[]{1}, "image/png");
        UploadLoginBackgroundUseCase.Command different = new UploadLoginBackgroundUseCase.Command("admin-1", new byte[]{2}, "image/png");

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(same);
        assertThat(a).hasSameHashCodeAs(same);
        assertThat(a).isNotEqualTo(different);
        assertThat(a.toString()).contains("adminId=admin-1");
    }

    @Test
    void uploadBrandingAssetCommand() {
        UploadBrandingAssetUseCase.Command a = new UploadBrandingAssetUseCase.Command("admin-1", "home-header", new byte[]{1}, "image/png");
        UploadBrandingAssetUseCase.Command same = new UploadBrandingAssetUseCase.Command("admin-1", "home-header", new byte[]{1}, "image/png");
        UploadBrandingAssetUseCase.Command differentName = new UploadBrandingAssetUseCase.Command("admin-1", "other", new byte[]{1}, "image/png");

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(same);
        assertThat(a).hasSameHashCodeAs(same);
        assertThat(a).isNotEqualTo(differentName);
        assertThat(a.toString()).contains("name=home-header");
    }

    @Test
    void replaceBrandingAssetCommand() {
        UUID assetId = UUID.randomUUID();
        ReplaceBrandingAssetUseCase.Command a = new ReplaceBrandingAssetUseCase.Command("admin-1", assetId, new byte[]{1}, "image/png");
        ReplaceBrandingAssetUseCase.Command same = new ReplaceBrandingAssetUseCase.Command("admin-1", assetId, new byte[]{1}, "image/png");
        ReplaceBrandingAssetUseCase.Command differentId = new ReplaceBrandingAssetUseCase.Command("admin-1", UUID.randomUUID(), new byte[]{1}, "image/png");

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(same);
        assertThat(a).hasSameHashCodeAs(same);
        assertThat(a).isNotEqualTo(differentId);
        assertThat(a.toString()).contains("assetId=" + assetId);
    }

    @Test
    void uploadCampaignScreenImageCommand() {
        UUID campaignId = UUID.randomUUID();
        UUID screenId = UUID.randomUUID();
        UploadCampaignScreenImageUseCase.Command a = new UploadCampaignScreenImageUseCase.Command(
                "admin-1", campaignId, screenId, new byte[]{1}, "image/png", 0.5, 0.5);
        UploadCampaignScreenImageUseCase.Command same = new UploadCampaignScreenImageUseCase.Command(
                "admin-1", campaignId, screenId, new byte[]{1}, "image/png", 0.5, 0.5);
        UploadCampaignScreenImageUseCase.Command differentFocal = new UploadCampaignScreenImageUseCase.Command(
                "admin-1", campaignId, screenId, new byte[]{1}, "image/png", 0.1, 0.9);

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(same);
        assertThat(a).hasSameHashCodeAs(same);
        assertThat(a).isNotEqualTo(differentFocal);
        assertThat(a.toString()).contains("campaignId=" + campaignId).contains("screenId=" + screenId);
    }
}
