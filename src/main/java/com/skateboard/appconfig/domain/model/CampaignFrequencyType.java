package com.skateboard.appconfig.domain.model;

/**
 * How often a {@link Campaign} may be shown to the same device (spec §5.3).
 * Enforcement of the cap is a mobile-client concern (it persists exposure state
 * locally so startup needs no network round-trip); this service only stores the
 * policy. {@code MAX_PER_DAY} is the only value that also requires
 * {@link Campaign#getMaxDisplaysPerDay()} to be set.
 */
public enum CampaignFrequencyType {
    ALWAYS,
    ONCE,
    ONCE_PER_SESSION,
    ONCE_PER_DAY,
    MAX_PER_DAY
}
