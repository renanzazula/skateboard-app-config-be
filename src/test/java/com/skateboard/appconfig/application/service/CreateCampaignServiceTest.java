package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.CreateCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import com.skateboard.appconfig.domain.model.CampaignStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreateCampaignServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private CreateCampaignService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CreateCampaignService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsADraftCampaignWithAGeneratedId() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-02-01T00:00:00Z");

        Campaign campaign = service.execute(new CreateCampaignUseCase.Command("admin-1", "Spring", "note",
                start, end, 5, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null));

        assertThat(campaign.getId()).isNotNull();
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.DRAFT);
        assertThat(campaign.getName()).isEqualTo("Spring");
        assertThat(campaign.getCreatedBy()).isEqualTo("admin-1");
    }
}
