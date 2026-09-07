package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UploadCampaignScreenImageUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.exception.CampaignScreenNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignScreen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Uploads a screen background as-is (no resizing/variants in V1) under
 * {@code campaigns/<campaignId>/<screenId><ext>} and bumps the asset version so
 * the client can treat each version as an immutable, cacheable object. Same
 * 5 MB / jpeg-png-webp rules as the branding uploads ({@link ImageUploadValidator}).
 */
@Service
public class UploadCampaignScreenImageService implements UploadCampaignScreenImageUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;
    private final ObjectStoragePort objectStoragePort;

    public UploadCampaignScreenImageService(CampaignRepositoryPort campaignRepositoryPort,
                                            ObjectStoragePort objectStoragePort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @Transactional
    public CampaignScreen execute(Command command) {
        String extension = ImageUploadValidator.extensionFor(command.data(), command.mimeType());
        Campaign campaign = campaignRepositoryPort.findById(command.campaignId())
                .orElseThrow(() -> new CampaignNotFoundException(command.campaignId()));
        boolean screenExists = campaign.getScreens().stream()
                .anyMatch(s -> s.getId().equals(command.screenId()));
        if (!screenExists) {
            throw new CampaignScreenNotFoundException(command.campaignId(), command.screenId());
        }

        String key = "campaigns/" + command.campaignId() + "/" + command.screenId() + extension;
        objectStoragePort.put(key, command.data(), command.mimeType());

        campaign.replaceScreenImage(command.screenId(), key, command.mimeType(), null, null,
                (long) command.data().length, command.focalPointX(), command.focalPointY(), command.adminId());
        Campaign saved = campaignRepositoryPort.save(campaign);
        return saved.getScreens().stream()
                .filter(s -> s.getId().equals(command.screenId()))
                .findFirst()
                .orElseThrow(() -> new CampaignScreenNotFoundException(command.campaignId(), command.screenId()));
    }
}
