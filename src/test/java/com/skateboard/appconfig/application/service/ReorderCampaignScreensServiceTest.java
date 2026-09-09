package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ReorderCampaignScreensUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReorderCampaignScreensServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private ReorderCampaignScreensService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReorderCampaignScreensService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Campaign campaignWithThreeScreens() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "c", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 0, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS,
                null, "admin-1");
        for (String title : List.of("one", "two", "three")) {
            campaign.addScreen(new CampaignScreenDraft(2, null, "#101010", title, null, null, null, null, null,
                    null, false, null, null, null, null), "admin-1");
        }
        return campaign;
    }

    @Test
    void rejectsAnUnknownCampaign() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ReorderCampaignScreensUseCase.Command(
                "a", id, List.of(UUID.randomUUID()))))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void writesContiguousPositionsInTheSubmittedOrder() {
        Campaign campaign = campaignWithThreeScreens();
        List<UUID> ids = campaign.getScreens().stream().map(CampaignScreen::getId).toList();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        Campaign reordered = service.execute(new ReorderCampaignScreensUseCase.Command(
                "admin-2", campaign.getId(), List.of(ids.get(2), ids.get(0), ids.get(1))));

        assertThat(reordered.getScreens()).extracting(CampaignScreen::getTitle)
                .containsExactly("three", "one", "two");
        assertThat(reordered.getScreens()).extracting(CampaignScreen::getPosition)
                .containsExactly(1, 2, 3);
        assertThat(reordered.getUpdatedBy()).isEqualTo("admin-2");
        verify(campaignRepositoryPort).save(campaign);
    }

    @Test
    void aPartialOrderingIsRejectedSoNoScreenIsSilentlyDropped() {
        Campaign campaign = campaignWithThreeScreens();
        List<UUID> ids = campaign.getScreens().stream().map(CampaignScreen::getId).toList();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.execute(new ReorderCampaignScreensUseCase.Command(
                "a", campaign.getId(), List.of(ids.get(0), ids.get(1)))))
                .isInstanceOf(IllegalArgumentException.class);

        verify(campaignRepositoryPort, never()).save(any());
    }

    @Test
    void anOrderingNamingAForeignScreenIsRejected() {
        Campaign campaign = campaignWithThreeScreens();
        List<UUID> ids = campaign.getScreens().stream().map(CampaignScreen::getId).toList();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.execute(new ReorderCampaignScreensUseCase.Command(
                "a", campaign.getId(), List.of(ids.get(0), ids.get(1), UUID.randomUUID()))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(campaign.getScreens()).extracting(CampaignScreen::getTitle)
                .containsExactly("one", "two", "three");
        verify(campaignRepositoryPort, never()).save(any());
    }

    @Test
    void aDuplicatedIdIsRejectedRatherThanCloningAScreen() {
        Campaign campaign = campaignWithThreeScreens();
        List<UUID> ids = campaign.getScreens().stream().map(CampaignScreen::getId).toList();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.execute(new ReorderCampaignScreensUseCase.Command(
                "a", campaign.getId(), List.of(ids.get(0), ids.get(0), ids.get(1)))))
                .isInstanceOf(IllegalArgumentException.class);

        verify(campaignRepositoryPort, never()).save(any());
    }
}
