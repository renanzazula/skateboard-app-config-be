package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.RecordCampaignEventUseCase;
import com.skateboard.appconfig.application.port.out.CampaignEventPublisherPort;
import com.skateboard.appconfig.domain.model.CampaignEvent;
import com.skateboard.appconfig.domain.model.CampaignEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class RecordCampaignEventServiceTest {

    @Mock
    private CampaignEventPublisherPort campaignEventPublisherPort;

    private RecordCampaignEventService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RecordCampaignEventService(campaignEventPublisherPort);
    }

    @Test
    void publishesTheEvent() {
        UUID campaignId = UUID.randomUUID();
        UUID screenId = UUID.randomUUID();

        service.execute(new RecordCampaignEventUseCase.Command(campaignId, screenId,
                CampaignEventType.CAMPAIGN_CTA_CLICKED, "ios", "1.2.3", "/podcasts/1"));

        ArgumentCaptor<CampaignEvent> event = ArgumentCaptor.forClass(CampaignEvent.class);
        verify(campaignEventPublisherPort).publish(event.capture());
        assertThat(event.getValue().campaignId()).isEqualTo(campaignId);
        assertThat(event.getValue().type()).isEqualTo(CampaignEventType.CAMPAIGN_CTA_CLICKED);
        assertThat(event.getValue().id()).isNotNull();
        assertThat(event.getValue().occurredAt()).isNotNull();
    }

    @Test
    void neverPropagatesAPublisherFailureToTheCaller() {
        doThrow(new RuntimeException("sink down")).when(campaignEventPublisherPort).publish(org.mockito.ArgumentMatchers.any());

        assertThatCode(() -> service.execute(new RecordCampaignEventUseCase.Command(
                UUID.randomUUID(), null, CampaignEventType.CAMPAIGN_CLOSED, null, null, null)))
                .doesNotThrowAnyException();
    }
}
