package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Campaign;

import java.util.List;
import java.util.UUID;

public interface ReorderCampaignScreensUseCase {

    record Command(String adminId, UUID campaignId, List<UUID> orderedScreenIds) {}

    Campaign execute(Command command);
}
