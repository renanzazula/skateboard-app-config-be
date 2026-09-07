package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * A single campaign analytics event (spec §18). Not part of the {@link Campaign}
 * aggregate — it is fire-and-forget telemetry with no PII, recorded even for a
 * campaign that has since been deleted, so it carries only loose id references.
 */
public record CampaignEvent(
        UUID id,
        UUID campaignId,
        UUID screenId,
        CampaignEventType type,
        Instant occurredAt,
        String platform,
        String appVersion,
        String actionTarget) {

    public static CampaignEvent of(UUID campaignId, UUID screenId, CampaignEventType type,
                                   String platform, String appVersion, String actionTarget) {
        return new CampaignEvent(UUID.randomUUID(), campaignId, screenId, type, Instant.now(),
                platform, appVersion, actionTarget);
    }
}
