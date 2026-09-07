package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.RemoveCampaignScreenUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.exception.CampaignScreenNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignScreen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveCampaignScreenService implements RemoveCampaignScreenUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;
    private final ObjectStoragePort objectStoragePort;

    public RemoveCampaignScreenService(CampaignRepositoryPort campaignRepositoryPort,
                                       ObjectStoragePort objectStoragePort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @Transactional
    public void execute(Command command) {
        Campaign campaign = campaignRepositoryPort.findById(command.campaignId())
                .orElseThrow(() -> new CampaignNotFoundException(command.campaignId()));
        CampaignScreen screen = campaign.getScreens().stream()
                .filter(s -> s.getId().equals(command.screenId()))
                .findFirst()
                .orElseThrow(() -> new CampaignScreenNotFoundException(command.campaignId(), command.screenId()));
        String orphanedKey = screen.getBackground() != null ? screen.getBackground().getStorageKey() : null;

        campaign.removeScreen(command.screenId(), command.adminId());
        campaignRepositoryPort.save(campaign);

        if (orphanedKey != null) {
            objectStoragePort.delete(orphanedKey);
        }
    }
}
