package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.PauseCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignInvalidStateTransitionException;
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

class PauseCampaignServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private PauseCampaignService service;

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-02-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PauseCampaignService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Campaign published() {
        return Campaign.reconstitute(UUID.randomUUID(), "Spring", null, CampaignStatus.PUBLISHED, START, END, 5,
                CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, List.of(), "admin-1", START, "admin-1",
                START, "admin-1", START);
    }

    @Test
    void throwsWhenCampaignIsUnknown() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new PauseCampaignUseCase.Command("admin-1", id)))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void pausesAPublishedCampaign() {
        Campaign campaign = published();
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        Campaign result = service.execute(new PauseCampaignUseCase.Command("admin-2", campaign.getId()));

        assertThat(result.getStatus()).isEqualTo(CampaignStatus.PAUSED);
        assertThat(result.getUpdatedBy()).isEqualTo("admin-2");
    }

    @Test
    void rejectsPausingADraftCampaign() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "Spring", null, START, END, 5, CampaignAudience.ALL,
                CampaignFrequencyType.ALWAYS, null, "admin-1");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.execute(new PauseCampaignUseCase.Command("admin-2", campaign.getId())))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);
    }
}
