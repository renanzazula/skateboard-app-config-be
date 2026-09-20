package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class GetCampaignServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private GetCampaignService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetCampaignService(campaignRepositoryPort);
    }

    @Test
    void throwsWhenCampaignIsUnknown() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id)).isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void returnsTheCampaign() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "Spring", null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 5, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null,
                "admin-1");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThat(service.execute(campaign.getId())).isSameAs(campaign);
    }
}
