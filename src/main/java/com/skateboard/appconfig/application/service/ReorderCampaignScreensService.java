package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ReorderCampaignScreensUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReorderCampaignScreensService implements ReorderCampaignScreensUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public ReorderCampaignScreensService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    @Transactional
    public Campaign execute(Command command) {
        Campaign campaign = campaignRepositoryPort.findById(command.campaignId())
                .orElseThrow(() -> new CampaignNotFoundException(command.campaignId()));
        campaign.reorderScreens(command.orderedScreenIds(), command.adminId());
        return campaignRepositoryPort.save(campaign);
    }
}
