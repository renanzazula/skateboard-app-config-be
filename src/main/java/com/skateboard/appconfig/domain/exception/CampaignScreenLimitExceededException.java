package com.skateboard.appconfig.domain.exception;

/**
 * A screen add/update would break a V1 sizing limit — more than
 * {@link com.skateboard.appconfig.domain.model.Campaign#MAX_SCREENS} screens or
 * more than {@link com.skateboard.appconfig.domain.model.Campaign#MAX_TOTAL_DURATION_SECONDS}
 * seconds of combined duration. Translated to HTTP 400 by the global exception
 * handler. (A publish that fails these same checks is a
 * {@link CampaignInvalidStateTransitionException} → 409 instead, matching the
 * API contract.)
 */
public class CampaignScreenLimitExceededException extends RuntimeException {

    private CampaignScreenLimitExceededException(String message) {
        super(message);
    }

    public static CampaignScreenLimitExceededException tooManyScreens(int maxScreens) {
        return new CampaignScreenLimitExceededException(
                "A campaign cannot have more than " + maxScreens + " screens.");
    }

    public static CampaignScreenLimitExceededException totalDurationExceeded(int maxSeconds) {
        return new CampaignScreenLimitExceededException(
                "The combined screen duration cannot exceed " + maxSeconds + " seconds.");
    }
}
