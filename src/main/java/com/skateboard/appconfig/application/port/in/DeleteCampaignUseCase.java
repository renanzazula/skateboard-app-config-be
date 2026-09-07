package com.skateboard.appconfig.application.port.in;

import java.util.UUID;

public interface DeleteCampaignUseCase {

    /**
     * @throws com.skateboard.appconfig.domain.exception.CampaignNotFoundException if no such campaign
     * @throws com.skateboard.appconfig.domain.exception.CampaignInvalidStateTransitionException
     *         if the campaign is not still a draft (archive it instead)
     */
    void execute(UUID campaignId);
}
