package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.CampaignEvent;
import com.skateboard.appconfig.domain.model.CampaignEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

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
    void publishMapsEveryFieldToTheEntity() {
        CampaignEvent event = CampaignEvent.of(UUID.randomUUID(), UUID.randomUUID(),
                CampaignEventType.CAMPAIGN_CTA_CLICKED, "ios", "1.2.3", "/promo");

        adapter.publish(event);

        ArgumentCaptor<CampaignEventJpaEntity> captor = ArgumentCaptor.forClass(CampaignEventJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        CampaignEventJpaEntity entity = captor.getValue();
        assertThat(entity.getId()).isEqualTo(event.id());
        assertThat(entity.getCampaignId()).isEqualTo(event.campaignId());
        assertThat(entity.getScreenId()).isEqualTo(event.screenId());
        assertThat(entity.getEventType()).isEqualTo(CampaignEventType.CAMPAIGN_CTA_CLICKED);
        assertThat(entity.getOccurredAt()).isEqualTo(event.occurredAt());
        assertThat(entity.getPlatform()).isEqualTo("ios");
        assertThat(entity.getAppVersion()).isEqualTo("1.2.3");
        assertThat(entity.getActionTarget()).isEqualTo("/promo");
    }
}
