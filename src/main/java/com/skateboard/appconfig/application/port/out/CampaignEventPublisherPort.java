package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.CampaignEvent;

/**
 * Outbound seam for campaign analytics events (implementation plan, gap #3).
 * V1's only implementation writes a row to {@code campaign_event}; a later swap
 * to a real analytics sink touches only its adapter, not the domain or the use
 * case.
 */
public interface CampaignEventPublisherPort {
    void publish(CampaignEvent event);
}
