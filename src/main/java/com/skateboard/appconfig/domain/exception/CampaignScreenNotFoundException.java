package com.skateboard.appconfig.domain.exception;

import java.util.UUID;

/**
 * A screen id was not found within the given campaign. Extends
 * {@link CampaignNotFoundException} so the single 404 handler covers both
 * ("Campaign or screen not found" in the API contract).
 */
public class CampaignScreenNotFoundException extends CampaignNotFoundException {

    public CampaignScreenNotFoundException(UUID campaignId, UUID screenId) {
        super("Screen " + screenId + " not found in campaign " + campaignId);
    }
}
