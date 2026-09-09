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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * The two admin read use cases. Both are deliberately unfiltered — the admin
 * list shows every campaign regardless of status, and a single read never
 * silently returns null.
 */
class CampaignReadServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private ListCampaignsService listService;
    private GetCampaignService getService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listService = new ListCampaignsService(campaignRepositoryPort);
        getService = new GetCampaignService(campaignRepositoryPort);
    }

    private static Campaign campaign(String name) {
        return Campaign.create(UUID.randomUUID(), name, null, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"), 0, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS,
                null, "admin-1");
    }

    @Test
    void theAdminListRelaysEveryCampaignInRepositoryOrder() {
        Campaign first = campaign("first");
        Campaign second = campaign("second");
        when(campaignRepositoryPort.findAll()).thenReturn(List.of(first, second));

        assertThat(listService.execute()).containsExactly(first, second);
    }

    @Test
    void anEmptyRepositoryYieldsAnEmptyListNotNull() {
        when(campaignRepositoryPort.findAll()).thenReturn(List.of());

        assertThat(listService.execute()).isEmpty();
    }

    @Test
    void gettingAKnownCampaignReturnsTheAggregate() {
        Campaign campaign = campaign("c");
        when(campaignRepositoryPort.findById(campaign.getId())).thenReturn(Optional.of(campaign));

        assertThat(getService.execute(campaign.getId())).isSameAs(campaign);
    }

    @Test
    void gettingAnUnknownCampaignThrowsRatherThanReturningNull() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getService.execute(id))
                .isInstanceOf(CampaignNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}
