package com.skateboard.appconfig.domain.model;

/**
 * Who a {@link Campaign} is eligible to be shown to. {@code ANONYMOUS} targets
 * signed-out sessions specifically (it is filtered out once a user is
 * authenticated); {@code AUTHENTICATED} is the mirror; {@code ALL} ignores auth
 * state. Future segmentation (platform, app version, locale, role) is expected
 * to extend this dimension rather than replace it (spec §5.2).
 */
public enum CampaignAudience {
    ALL,
    AUTHENTICATED,
    ANONYMOUS
}
