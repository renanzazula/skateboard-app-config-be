package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateCampaignUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UpdateCampaignServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private UpdateCampaignService service;

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-02-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UpdateCampaignService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void throwsWhenCampaignIsUnknown() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new UpdateCampaignUseCase.Command("admin-1", id, "New", null,
                START, END, 1, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null)))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void updatesTheCampaignDetails() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "Spring", null, START, END, 1, CampaignAudience.ALL,
                CampaignFrequencyType.ALWAYS, null, "admin-1");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        Campaign result = service.execute(new UpdateCampaignUseCase.Command("admin-2", campaign.getId(), "Summer",
                "desc", START, END, 9, CampaignAudience.AUTHENTICATED, CampaignFrequencyType.ONCE_PER_DAY, null));

        assertThat(result.getName()).isEqualTo("Summer");
        assertThat(result.getDescription()).isEqualTo("desc");
        assertThat(result.getPriority()).isEqualTo(9);
        assertThat(result.getAudience()).isEqualTo(CampaignAudience.AUTHENTICATED);
        assertThat(result.getUpdatedBy()).isEqualTo("admin-2");
    }
}
