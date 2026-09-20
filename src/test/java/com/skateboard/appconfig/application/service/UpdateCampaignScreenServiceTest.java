package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateCampaignScreenUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
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
import static org.mockito.Mockito.when;

class UpdateCampaignScreenServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private UpdateCampaignScreenService service;

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-02-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UpdateCampaignScreenService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static CampaignScreenDraft draft(int durationSeconds, String title) {
        return new CampaignScreenDraft(durationSeconds, null, "#101010", title, null, null, null, null, null, null,
                false, null, CampaignActionType.NONE, null, null);
    }

    @Test
    void throwsWhenCampaignIsUnknown() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new UpdateCampaignScreenUseCase.Command("admin-1", id, UUID.randomUUID(), draft(2, "Title"))))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void throwsWhenScreenIsUnknown() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "Spring", null, START, END, 5, CampaignAudience.ALL,
                CampaignFrequencyType.ALWAYS, null, "admin-1");
        campaign.addScreen(draft(2, "Title"), "admin-1");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.execute(
                new UpdateCampaignScreenUseCase.Command("admin-1", campaign.getId(), UUID.randomUUID(),
                        draft(2, "Title"))))
                .isInstanceOf(CampaignScreenNotFoundException.class);
    }

    @Test
    void updatesAndReturnsTheScreen() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "Spring", null, START, END, 5, CampaignAudience.ALL,
                CampaignFrequencyType.ALWAYS, null, "admin-1");
        CampaignScreen original = campaign.addScreen(draft(2, "Title"), "admin-1");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        CampaignScreen result = service.execute(new UpdateCampaignScreenUseCase.Command("admin-2", campaign.getId(),
                original.getId(), draft(5, "New title")));

        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getDurationSeconds()).isEqualTo(5);
        assertThat(result.getTitle()).isEqualTo("New title");
    }
}
