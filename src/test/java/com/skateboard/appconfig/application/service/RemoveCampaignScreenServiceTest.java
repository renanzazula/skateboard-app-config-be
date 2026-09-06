package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.RemoveCampaignScreenUseCase;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoveCampaignScreenServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;
    @Mock
    private ObjectStoragePort objectStoragePort;

    private RemoveCampaignScreenService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RemoveCampaignScreenService(campaignRepositoryPort, objectStoragePort);
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
    void rejectsAnUnknownScreen() {
        Campaign campaign = campaignWithOneScreen();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.execute(
                new RemoveCampaignScreenUseCase.Command("a", campaign.getId(), UUID.randomUUID())))
                .isInstanceOf(CampaignScreenNotFoundException.class);
        verify(campaignRepositoryPort, never()).save(any());
    }

    @Test
    void removesTheScreenThenDeletesItsOrphanedImage() {
        Campaign campaign = campaignWithOneScreen();
        CampaignScreen screen = campaign.getScreens().get(0);
        campaign.replaceScreenImage(screen.getId(), "campaigns/c/s.webp", "image/webp", null, null, 1L, null, null, "a");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        service.execute(new RemoveCampaignScreenUseCase.Command("a", campaign.getId(), screen.getId()));

        assertThat(campaign.getScreens()).isEmpty();
        InOrder inOrder = inOrder(campaignRepositoryPort, objectStoragePort);
        inOrder.verify(campaignRepositoryPort).save(any());
        inOrder.verify(objectStoragePort).delete("campaigns/c/s.webp");
    }
}
