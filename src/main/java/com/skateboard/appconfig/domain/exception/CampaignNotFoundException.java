package com.skateboard.appconfig.domain.exception;

import java.util.UUID;

/**
 * A campaign (or one of its screens — see {@link CampaignScreenNotFoundException})
 * was referenced by an id that does not exist. Translated to HTTP 404 by the
 * global exception handler.
 */
public class CampaignNotFoundException extends RuntimeException {

    public CampaignNotFoundException(UUID id) {
        super("Campaign not found: " + id);
    }

    protected CampaignNotFoundException(String message) {
        super(message);
    }
}
