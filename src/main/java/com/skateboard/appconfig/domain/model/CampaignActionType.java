package com.skateboard.appconfig.domain.model;

/**
 * The optional call-to-action on a {@link CampaignScreen} (spec §7).
 * {@code INTERNAL} targets are validated against a controlled allow-list of
 * app routes; {@code EXTERNAL} targets must be absolute http(s) URLs;
 * {@code NONE} means the screen has no CTA and any label/target is cleared.
 */
public enum CampaignActionType {
    NONE,
    INTERNAL,
    EXTERNAL
}
