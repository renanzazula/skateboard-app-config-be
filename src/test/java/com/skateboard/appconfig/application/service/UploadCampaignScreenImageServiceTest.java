package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UploadCampaignScreenImageUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.CampaignScreenNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignActionType;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import com.skateboard.appconfig.domain.model.CampaignScreen;
import com.skateboard.appconfig.domain.model.CampaignScreenDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UploadCampaignScreenImageServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;
    @Mock
    private ObjectStoragePort objectStoragePort;

    private UploadCampaignScreenImageService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UploadCampaignScreenImageService(campaignRepositoryPort, objectStoragePort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Campaign campaignWithOneScreen() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "c", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 1, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, "a");
        campaign.addScreen(new CampaignScreenDraft(3, null, "#101010", "t", null, null, null, null, null, null,
                false, null, CampaignActionType.NONE, null, null), "a");
        return campaign;
    }

    @Test
    void rejectsAnUnsupportedImageTypeBeforeTouchingStorage() {
        Campaign campaign = campaignWithOneScreen();
        UUID screenId = campaign.getScreens().get(0).getId();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        UploadCampaignScreenImageUseCase.Command command = new UploadCampaignScreenImageUseCase.Command(
                "a", campaign.getId(), screenId, new byte[]{1}, "image/gif", null, null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verify(objectStoragePort, never()).put(any(), any(), any());
    }

    @Test
    void rejectsAnUnknownScreen() {
        Campaign campaign = campaignWithOneScreen();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        UploadCampaignScreenImageUseCase.Command command = new UploadCampaignScreenImageUseCase.Command(
                "a", campaign.getId(), UUID.randomUUID(), new byte[]{1, 2}, "image/webp", null, null);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CampaignScreenNotFoundException.class);
        verify(objectStoragePort, never()).put(any(), any(), any());
    }

    @Test
    void storesUnderTheScreenKeyAndAttachesAVersionedAsset() {
        Campaign campaign = campaignWithOneScreen();
        UUID screenId = campaign.getScreens().get(0).getId();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        CampaignScreen screen = service.execute(new UploadCampaignScreenImageUseCase.Command(
                "a", campaign.getId(), screenId, new byte[]{1, 2, 3}, "image/webp", 0.5, 0.4));

        String expectedKey = "campaigns/" + campaign.getId() + "/" + screenId + ".webp";
        verify(objectStoragePort).put(expectedKey, new byte[]{1, 2, 3}, "image/webp");
        assertThat(screen.getBackground().getStorageKey()).isEqualTo(expectedKey);
        assertThat(screen.getBackground().getVersion()).isEqualTo(1);
        assertThat(screen.getBackground().getFocalPointX()).isEqualTo(0.5);
        assertThat(screen.getBackground().getSizeBytes()).isEqualTo(3L);
    }
}
