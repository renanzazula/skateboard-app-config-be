package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateCampaignScreenUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.exception.CampaignScreenLimitExceededException;
import com.skateboard.appconfig.domain.exception.CampaignScreenNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateCampaignScreenServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private UpdateCampaignScreenService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UpdateCampaignScreenService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Campaign campaign() {
        return Campaign.create(UUID.randomUUID(), "c", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 0, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS,
                null, "admin-1");
    }

    private static CampaignScreenDraft draft(int durationSeconds, String title) {
        return new CampaignScreenDraft(durationSeconds, null, "#101010", title, null, null, null, null, null,
                null, false, null, null, null, null);
    }

    @Test
    void rejectsAnUnknownCampaign() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new UpdateCampaignScreenUseCase.Command("a", id, UUID.randomUUID(), draft(3, "t"))))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void rejectsAScreenThatBelongsToNoCampaign() {
        Campaign campaign = campaign();
        campaign.addScreen(draft(3, "t"), "admin-1");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.execute(new UpdateCampaignScreenUseCase.Command(
                "a", campaign.getId(), UUID.randomUUID(), draft(3, "t"))))
                .isInstanceOf(CampaignScreenNotFoundException.class);

        verify(campaignRepositoryPort, never()).save(any());
    }

    @Test
    void appliesTheNewContentAndReturnsTheUpdatedScreen() {
        Campaign campaign = campaign();
        UUID screenId = campaign.addScreen(draft(3, "before"), "admin-1").getId();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        CampaignScreen updated = service.execute(new UpdateCampaignScreenUseCase.Command(
                "admin-2", campaign.getId(), screenId, draft(5, "after")));

        assertThat(updated.getId()).isEqualTo(screenId);
        assertThat(updated.getTitle()).isEqualTo("after");
        assertThat(updated.getDurationSeconds()).isEqualTo(5);
        assertThat(campaign.getUpdatedBy()).isEqualTo("admin-2");
        verify(campaignRepositoryPort).save(campaign);
    }

    @Test
    void anEditKeepsTheScreensPositionInTheSequence() {
        Campaign campaign = campaign();
        campaign.addScreen(draft(3, "first"), "admin-1");
        UUID secondId = campaign.addScreen(draft(3, "second"), "admin-1").getId();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        CampaignScreen updated = service.execute(new UpdateCampaignScreenUseCase.Command(
                "a", campaign.getId(), secondId, draft(2, "second edited")));

        assertThat(updated.getPosition()).isEqualTo(2);
    }

    @Test
    void anEditIsMeasuredAgainstTheOtherScreensNotTheOldTotal() {
        Campaign campaign = campaign();
        campaign.addScreen(draft(4, "a"), "admin-1");
        UUID secondId = campaign.addScreen(draft(4, "b"), "admin-1").getId();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        // 4 + 6 = 10s exactly, which is the cap — allowed even though the screen grew.
        CampaignScreen widened = service.execute(new UpdateCampaignScreenUseCase.Command(
                "a", campaign.getId(), secondId, draft(6, "b")));

        assertThat(widened.getDurationSeconds()).isEqualTo(6);
    }

    @Test
    void anEditThatPushesTheCombinedDurationOverTheCapIsRejected() {
        Campaign campaign = campaign();
        campaign.addScreen(draft(4, "a"), "admin-1");
        UUID secondId = campaign.addScreen(draft(4, "b"), "admin-1").getId();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.execute(new UpdateCampaignScreenUseCase.Command(
                "a", campaign.getId(), secondId, draft(7, "b"))))
                .isInstanceOf(CampaignScreenLimitExceededException.class);

        assertThat(campaign.getScreens().get(1).getDurationSeconds()).isEqualTo(4);
        verify(campaignRepositoryPort, never()).save(any());
    }
}
