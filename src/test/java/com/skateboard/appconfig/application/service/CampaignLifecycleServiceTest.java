package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ArchiveCampaignUseCase;
import com.skateboard.appconfig.application.port.in.PauseCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignInvalidStateTransitionException;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import com.skateboard.appconfig.domain.model.CampaignScreenDraft;
import com.skateboard.appconfig.domain.model.CampaignStatus;
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

/**
 * Pause and archive: the two lifecycle transitions with no test of their own.
 * Both must load the aggregate, let the domain veto an illegal transition, and
 * persist only a transition the domain accepted.
 */
class CampaignLifecycleServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private PauseCampaignService pauseService;
    private ArchiveCampaignService archiveService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pauseService = new PauseCampaignService(campaignRepositoryPort);
        archiveService = new ArchiveCampaignService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Campaign publishedCampaign() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "c", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 0, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS,
                null, "admin-1");
        campaign.addScreen(new CampaignScreenDraft(3, null, "#101010", null, null, null, null, null, null, null,
                false, null, null, null, null), "admin-1");
        campaign.publish("admin-1");
        return campaign;
    }

    private static Campaign draftCampaign() {
        return Campaign.create(UUID.randomUUID(), "c", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 0, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS,
                null, "admin-1");
    }

    @Test
    void pauseMovesAPublishedCampaignToPausedAndRecordsTheActor() {
        Campaign campaign = publishedCampaign();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        Campaign paused = pauseService.execute(new PauseCampaignUseCase.Command("admin-2", campaign.getId()));

        assertThat(paused.getStatus()).isEqualTo(CampaignStatus.PAUSED);
        assertThat(paused.getUpdatedBy()).isEqualTo("admin-2");
        verify(campaignRepositoryPort).save(campaign);
    }

    @Test
    void pauseIsRejectedForACampaignThatWasNeverPublishedAndNothingIsSaved() {
        Campaign campaign = draftCampaign();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> pauseService.execute(new PauseCampaignUseCase.Command("a", campaign.getId())))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.DRAFT);
        verify(campaignRepositoryPort, never()).save(any());
    }

    @Test
    void pauseRejectsAnUnknownCampaign() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pauseService.execute(new PauseCampaignUseCase.Command("a", id)))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void archiveIsAllowedStraightFromDraft() {
        Campaign campaign = draftCampaign();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        Campaign archived = archiveService.execute(new ArchiveCampaignUseCase.Command("admin-3", campaign.getId()));

        assertThat(archived.getStatus()).isEqualTo(CampaignStatus.ARCHIVED);
        assertThat(archived.getUpdatedBy()).isEqualTo("admin-3");
        verify(campaignRepositoryPort).save(campaign);
    }

    @Test
    void archivingTwiceIsRejectedRatherThanSilentlyRepeated() {
        Campaign campaign = draftCampaign();
        campaign.archive("admin-1");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> archiveService.execute(new ArchiveCampaignUseCase.Command("a", campaign.getId())))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);

        verify(campaignRepositoryPort, never()).save(any());
    }

    @Test
    void archiveRejectsAnUnknownCampaign() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> archiveService.execute(new ArchiveCampaignUseCase.Command("a", id)))
                .isInstanceOf(CampaignNotFoundException.class);
    }
}
