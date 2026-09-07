package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetActiveCampaignsServiceTest {

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private GetActiveCampaignsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetActiveCampaignsService(campaignRepositoryPort);
        when(campaignRepositoryPort.findEligibleForRuntime(any(), eq(false))).thenReturn(List.of());
        when(campaignRepositoryPort.findEligibleForRuntime(any(), eq(true))).thenReturn(List.of());
    }

    @Test
    void passesTheAuthStateAndaNowInstantThrough() {
        service.execute(true);

        ArgumentCaptor<Instant> now = ArgumentCaptor.forClass(Instant.class);
        verify(campaignRepositoryPort).findEligibleForRuntime(now.capture(), eq(true));
        assertThat(now.getValue()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void anonymousCallForwardsFalse() {
        service.execute(false);
        verify(campaignRepositoryPort).findEligibleForRuntime(any(), eq(false));
    }
}
