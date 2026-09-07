package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.CampaignInvalidStateTransitionException;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteCampaignServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;
    @Mock
    private ObjectStoragePort objectStoragePort;

    private DeleteCampaignService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DeleteCampaignService(campaignRepositoryPort, objectStoragePort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Campaign draftCampaign() {
        return Campaign.create(UUID.randomUUID(), "c", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 1, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, "a");
    }

    private static CampaignScreenDraft screenDraft() {
        return new CampaignScreenDraft(3, null, "#101010", "t", null, null, null, null, null, null,
                false, null, com.skateboard.appconfig.domain.model.CampaignActionType.NONE, null, null);
    }

    @Test
    void rejectsAnUnknownCampaign() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id)).isInstanceOf(CampaignNotFoundException.class);
        verify(campaignRepositoryPort, never()).deleteById(any());
    }

    @Test
    void rejectsACampaignThatIsNoLongerADraft() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(), "a");
        campaign.publish("a");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.execute(campaign.getId()))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);
        verify(campaignRepositoryPort, never()).deleteById(any());
    }

    @Test
    void deletesTheDraftAndCleansUpItsScreenImages() {
        Campaign campaign = draftCampaign();
        CampaignScreen screen = campaign.addScreen(screenDraft(), "a");
        campaign.replaceScreenImage(screen.getId(), "campaigns/c/s.webp", "image/webp",
                null, null, 1L, null, null, "a");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        service.execute(campaign.getId());

        verify(objectStoragePort).delete("campaigns/c/s.webp");
        verify(campaignRepositoryPort).deleteById(campaign.getId());
    }
}
