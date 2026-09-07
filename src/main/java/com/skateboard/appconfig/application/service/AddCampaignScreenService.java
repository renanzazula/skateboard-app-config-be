package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.AddCampaignScreenUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.exception.CampaignScreenNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignScreen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddCampaignScreenService implements AddCampaignScreenUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public AddCampaignScreenService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    @Transactional
    public CampaignScreen execute(Command command) {
        Campaign campaign = campaignRepositoryPort.findById(command.campaignId())
                .orElseThrow(() -> new CampaignNotFoundException(command.campaignId()));
        CampaignScreen added = campaign.addScreen(command.draft(), command.adminId());
        Campaign saved = campaignRepositoryPort.save(campaign);
        return screen(saved, added.getId());
    }

    private static CampaignScreen screen(Campaign campaign, UUID screenId) {
        return campaign.getScreens().stream()
                .filter(s -> s.getId().equals(screenId))
                .findFirst()
                .orElseThrow(() -> new CampaignScreenNotFoundException(campaign.getId(), screenId));
    }
}
