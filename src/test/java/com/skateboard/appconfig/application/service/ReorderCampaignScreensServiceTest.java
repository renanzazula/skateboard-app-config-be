package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ReorderCampaignScreensUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignActionType;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
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

    @Test
    void throwsWhenCampaignIsUnknown() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ReorderCampaignScreensUseCase.Command("admin-1", id, List.of())))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void reordersTheScreens() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "Spring", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 5, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null,
                "admin-1");
        CampaignScreenDraft draft = new CampaignScreenDraft(2, null, "#101010", "Title", null, null, null, null,
                null, null, false, null, CampaignActionType.NONE, null, null);
        var first = campaign.addScreen(draft, "admin-1");
        var second = campaign.addScreen(draft, "admin-1");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        Campaign result = service.execute(new ReorderCampaignScreensUseCase.Command("admin-2", campaign.getId(),
                List.of(second.getId(), first.getId())));

        assertThat(result.getScreens().get(0).getId()).isEqualTo(second.getId());
        assertThat(result.getScreens().get(1).getId()).isEqualTo(first.getId());
        assertThat(result.getUpdatedBy()).isEqualTo("admin-2");
    }
}
