package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.CampaignScreen;

import java.util.UUID;

public interface UploadCampaignScreenImageUseCase {

    record Command(String adminId, UUID campaignId, UUID screenId, byte[] data, String mimeType,
                   Double focalPointX, Double focalPointY) {}

    CampaignScreen execute(Command command);
}
