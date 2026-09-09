package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.CampaignEvent;
import com.skateboard.appconfig.domain.model.CampaignEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * The V1 analytics sink is a straight insert, but it is the only place the
 * event's fields reach the database — a dropped field here is silently lost
 * telemetry, not a failure.
 */
class CampaignEventPersistenceAdapterTest {

    @Mock
    private SpringCampaignEventRepository jpaRepository;

    private CampaignEventPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new CampaignEventPersistenceAdapter(jpaRepository);
    }

    @Test
    void writesEveryFieldOfTheEventIncludingItsPreAssignedId() {
        UUID eventId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID screenId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-05-01T10:15:30Z");
        CampaignEvent event = new CampaignEvent(eventId, campaignId, screenId,
                CampaignEventType.CAMPAIGN_CTA_CLICKED, occurredAt, "ios", "1.4.2", "/podcasts/123");

        adapter.publish(event);

        ArgumentCaptor<CampaignEventJpaEntity> captor = ArgumentCaptor.forClass(CampaignEventJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        CampaignEventJpaEntity saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(eventId);
        assertThat(saved.getCampaignId()).isEqualTo(campaignId);
        assertThat(saved.getScreenId()).isEqualTo(screenId);
        assertThat(saved.getEventType()).isEqualTo(CampaignEventType.CAMPAIGN_CTA_CLICKED);
        assertThat(saved.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(saved.getPlatform()).isEqualTo("ios");
        assertThat(saved.getAppVersion()).isEqualTo("1.4.2");
        assertThat(saved.getActionTarget()).isEqualTo("/podcasts/123");
    }

    @Test
    void acceptsACampaignLevelEventThatCarriesNoScreenOrTarget() {
        CampaignEvent event = CampaignEvent.of(UUID.randomUUID(), null, CampaignEventType.CAMPAIGN_STARTED,
                null, null, null);

        adapter.publish(event);

        ArgumentCaptor<CampaignEventJpaEntity> captor = ArgumentCaptor.forClass(CampaignEventJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getScreenId()).isNull();
        assertThat(captor.getValue().getActionTarget()).isNull();
        assertThat(captor.getValue().getEventType()).isEqualTo(CampaignEventType.CAMPAIGN_STARTED);
        assertThat(captor.getValue().getOccurredAt()).isNotNull();
    }
}
