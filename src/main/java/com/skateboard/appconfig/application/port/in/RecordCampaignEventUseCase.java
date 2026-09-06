package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.CampaignEventType;

import java.util.UUID;

public interface RecordCampaignEventUseCase {

    record Command(UUID campaignId, UUID screenId, CampaignEventType type, String platform, String appVersion,
                   String actionTarget) {}

    void execute(Command command);
}
