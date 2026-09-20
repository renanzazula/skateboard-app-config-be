package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ArchiveCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import com.skateboard.appconfig.domain.model.CampaignStatus;
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

class ArchiveCampaignServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private ArchiveCampaignService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ArchiveCampaignService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void throwsWhenCampaignIsUnknown() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ArchiveCampaignUseCase.Command("admin-1", id)))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void archivesAPublishedCampaign() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-02-01T00:00:00Z");
        Campaign campaign = Campaign.reconstitute(UUID.randomUUID(), "Spring", null, CampaignStatus.PUBLISHED,
                start, end, 5, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, List.of(), "admin-1", start,
                "admin-1", start, "admin-1", start);
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        Campaign result = service.execute(new ArchiveCampaignUseCase.Command("admin-2", campaign.getId()));

        assertThat(result.getStatus()).isEqualTo(CampaignStatus.ARCHIVED);
        assertThat(result.getUpdatedBy()).isEqualTo("admin-2");
    }
}
