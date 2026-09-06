package com.skateboard.appconfig.domain.exception;

/**
 * A lifecycle operation was requested from a state that does not allow it
 * (e.g. pausing a draft, editing an archived campaign, deleting a published
 * one) or a publish failed one of its aggregate invariants (e.g. no screens).
 * Translated to HTTP 409 by the global exception handler.
 */
public class CampaignInvalidStateTransitionException extends RuntimeException {

    public CampaignInvalidStateTransitionException(String message) {
        super(message);
    }
}
