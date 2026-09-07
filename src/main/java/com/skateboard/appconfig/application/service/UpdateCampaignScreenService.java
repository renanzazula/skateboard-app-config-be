package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateCampaignScreenUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.exception.CampaignScreenNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignScreen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateCampaignScreenService implements UpdateCampaignScreenUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public UpdateCampaignScreenService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    @Transactional
    public CampaignScreen execute(Command command) {
        Campaign campaign = campaignRepositoryPort.findById(command.campaignId())
                .orElseThrow(() -> new CampaignNotFoundException(command.campaignId()));
        campaign.updateScreen(command.screenId(), command.draft(), command.adminId());
        Campaign saved = campaignRepositoryPort.save(campaign);
        return saved.getScreens().stream()
                .filter(s -> s.getId().equals(command.screenId()))
                .findFirst()
                .orElseThrow(() -> new CampaignScreenNotFoundException(command.campaignId(), command.screenId()));
    }
}
