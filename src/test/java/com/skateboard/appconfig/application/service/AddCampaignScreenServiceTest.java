package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.AddCampaignScreenUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
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

class AddCampaignScreenServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private AddCampaignScreenService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AddCampaignScreenService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static CampaignScreenDraft draft() {
        return new CampaignScreenDraft(3, null, "#101010", "t", null, null, null, null, null, null,
                false, null, CampaignActionType.NONE, null, null);
    }

    @Test
    void rejectsAnUnknownCampaign() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        AddCampaignScreenUseCase.Command command = new AddCampaignScreenUseCase.Command("a", id, draft());
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void returnsTheNewlyAddedScreenFromThePersistedAggregate() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "c", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 1, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, "a");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        CampaignScreen added = service.execute(new AddCampaignScreenUseCase.Command("a", campaign.getId(), draft()));

        assertThat(added.getPosition()).isEqualTo(1);
        assertThat(added.getDurationSeconds()).isEqualTo(3);
        assertThat(campaign.getScreens()).extracting(CampaignScreen::getId).contains(added.getId());
    }
}
