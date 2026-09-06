package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Campaign;

import java.util.List;

public interface GetActiveCampaignsUseCase {

    /** Runtime read: campaigns eligible to display right now for a session with the given auth state. */
    List<Campaign> execute(boolean authenticated);
}
