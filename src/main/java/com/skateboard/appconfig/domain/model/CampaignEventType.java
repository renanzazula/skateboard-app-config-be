package com.skateboard.appconfig.domain.model;

/**
 * A campaign analytics event kind (spec §18). Constant names match the values
 * the API contract uses on the wire and the strings stored in
 * {@code campaign_event.event_type}.
 */
public enum CampaignEventType {
    CAMPAIGN_STARTED,
    CAMPAIGN_SCREEN_IMPRESSION,
    CAMPAIGN_SCREEN_COMPLETED,
    CAMPAIGN_SCREEN_SKIPPED,
    CAMPAIGN_CTA_CLICKED,
    CAMPAIGN_CLOSED,
    CAMPAIGN_COMPLETED
}
