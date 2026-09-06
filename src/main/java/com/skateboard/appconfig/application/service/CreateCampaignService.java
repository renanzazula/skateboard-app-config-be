package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.CreateCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.model.Campaign;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateCampaignService implements CreateCampaignUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public CreateCampaignService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    @Transactional
    public Campaign execute(Command command) {
        Campaign campaign = Campaign.create(UUID.randomUUID(), command.name(), command.description(),
                command.startAt(), command.endAt(), command.priority(), command.audience(),
                command.frequencyType(), command.maxDisplaysPerDay(), command.adminId());
        return campaignRepositoryPort.save(campaign);
    }
}
