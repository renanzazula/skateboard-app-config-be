package com.skateboard.appconfig.application.port.in;

import java.util.UUID;

public interface RemoveCampaignScreenUseCase {

    record Command(String adminId, UUID campaignId, UUID screenId) {}

    void execute(Command command);
}
