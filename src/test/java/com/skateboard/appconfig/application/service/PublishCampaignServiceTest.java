package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.PublishCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignActionType;
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
import static org.mockito.Mockito.when;

class PublishCampaignServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private PublishCampaignService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PublishCampaignService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void rejectsAnUnknownCampaign() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        PublishCampaignUseCase.Command command = new PublishCampaignUseCase.Command("a", id);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void publishesAValidDraftAndPersistsIt() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "c", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 1, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, "a");
        campaign.addScreen(new CampaignScreenDraft(3, null, "#101010", "t", null, null, null, null, null, null,
                false, null, CampaignActionType.NONE, null, null), "a");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        Campaign published = service.execute(new PublishCampaignUseCase.Command("publisher-1", campaign.getId()));

        assertThat(published.getStatus()).isEqualTo(CampaignStatus.PUBLISHED);
        assertThat(published.getPublishedBy()).isEqualTo("publisher-1");
    }
}
