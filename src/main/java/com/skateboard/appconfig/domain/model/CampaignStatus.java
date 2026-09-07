package com.skateboard.appconfig.domain.model;

/**
 * The persisted, admin-driven lifecycle state of a {@link Campaign}. Only these
 * four values are ever stored: they represent an admin's intent, never a
 * time-derived state. The API's {@code SCHEDULED}/{@code ACTIVE}/{@code EXPIRED}
 * distinctions are computed from the schedule at read time
 * ({@link Campaign#runtimeStatus(java.time.Instant)}), not stored or flipped by
 * a background job.
 */
public enum CampaignStatus {
    DRAFT,
    PUBLISHED,
    PAUSED,
    ARCHIVED
}
