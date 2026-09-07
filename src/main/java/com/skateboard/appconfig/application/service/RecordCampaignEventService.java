package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.RecordCampaignEventUseCase;
import com.skateboard.appconfig.application.port.out.CampaignEventPublisherPort;
import com.skateboard.appconfig.domain.model.CampaignEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Records a campaign analytics event. Campaigns are promotional content that
 * must never block or fail the app (spec §12/§18), so a storage failure here is
 * logged and swallowed rather than surfaced to the client — the endpoint only
 * ever rejects a malformed request body (handled upstream by validation).
 */
@Service
public class RecordCampaignEventService implements RecordCampaignEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordCampaignEventService.class);

    private final CampaignEventPublisherPort campaignEventPublisherPort;

    public RecordCampaignEventService(CampaignEventPublisherPort campaignEventPublisherPort) {
        this.campaignEventPublisherPort = campaignEventPublisherPort;
    }

    @Override
    public void execute(Command command) {
        try {
            campaignEventPublisherPort.publish(CampaignEvent.of(command.campaignId(), command.screenId(),
                    command.type(), command.platform(), command.appVersion(), command.actionTarget()));
        } catch (RuntimeException ex) {
            log.warn("Dropping campaign event {} for campaign {}: {}",
                    command.type(), command.campaignId(), ex.toString());
        }
    }
}
