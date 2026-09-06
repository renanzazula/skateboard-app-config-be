package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.CampaignScreen;
import com.skateboard.appconfig.domain.model.CampaignScreenDraft;

import java.util.UUID;

public interface UpdateCampaignScreenUseCase {

    record Command(String adminId, UUID campaignId, UUID screenId, CampaignScreenDraft draft) {}

    CampaignScreen execute(Command command);
}
