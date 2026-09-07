package com.skateboard.appconfig.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CampaignEventRetentionJobTest {

    @Mock
    private SpringCampaignEventRepository repository;

    private CampaignEventRetentionJob job;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        job = new CampaignEventRetentionJob(repository, 90);
    }

    @Test
    void deletesRowsOlderThanTheRetentionWindow() {
        when(repository.deleteByOccurredAtBefore(org.mockito.ArgumentMatchers.any())).thenReturn(5);

        Instant before = Instant.now().minus(90, ChronoUnit.DAYS).minusSeconds(5);
        job.purgeExpired();
        Instant after = Instant.now().minus(90, ChronoUnit.DAYS).plusSeconds(5);

        ArgumentCaptor<Instant> cutoff = ArgumentCaptor.forClass(Instant.class);
        verify(repository).deleteByOccurredAtBefore(cutoff.capture());
        assertThat(cutoff.getValue()).isBetween(before, after);
    }
}
