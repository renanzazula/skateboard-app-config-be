package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCampaignService implements UpdateCampaignUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public UpdateCampaignService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    @Transactional
    public Campaign execute(Command command) {
        Campaign campaign = campaignRepositoryPort.findById(command.campaignId())
                .orElseThrow(() -> new CampaignNotFoundException(command.campaignId()));
        campaign.updateDetails(command.name(), command.description(), command.startAt(), command.endAt(),
                command.priority(), command.audience(), command.frequencyType(), command.maxDisplaysPerDay(),
                command.adminId());
        return campaignRepositoryPort.save(campaign);
    }
}
