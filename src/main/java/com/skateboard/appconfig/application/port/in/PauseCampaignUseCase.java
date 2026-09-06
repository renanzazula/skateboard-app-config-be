package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Campaign;

import java.util.UUID;

public interface PauseCampaignUseCase {

    record Command(String adminId, UUID campaignId) {}

    Campaign execute(Command command);
}
