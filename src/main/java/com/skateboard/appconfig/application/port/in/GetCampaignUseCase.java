package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Campaign;

import java.util.UUID;

public interface GetCampaignUseCase {

    /** @throws com.skateboard.appconfig.domain.exception.CampaignNotFoundException if no such campaign */
    Campaign execute(UUID campaignId);
}
