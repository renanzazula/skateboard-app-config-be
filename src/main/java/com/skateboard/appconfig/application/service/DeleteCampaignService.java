package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.DeleteCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.CampaignInvalidStateTransitionException;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteCampaignService implements DeleteCampaignUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;
    private final ObjectStoragePort objectStoragePort;

    public DeleteCampaignService(CampaignRepositoryPort campaignRepositoryPort, ObjectStoragePort objectStoragePort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @Transactional
    public void execute(UUID campaignId) {
        Campaign campaign = campaignRepositoryPort.findById(campaignId)
                .orElseThrow(() -> new CampaignNotFoundException(campaignId));
        if (!campaign.isDeletable()) {
            throw new CampaignInvalidStateTransitionException(
                    "Only a draft campaign can be deleted; archive it instead.");
        }
        campaign.getScreens().forEach(screen -> {
            if (screen.getBackground() != null) {
                objectStoragePort.delete(screen.getBackground().getStorageKey());
            }
        });
        campaignRepositoryPort.deleteById(campaignId);
    }
}
