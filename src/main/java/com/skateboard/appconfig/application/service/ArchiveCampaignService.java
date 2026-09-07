package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ArchiveCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiveCampaignService implements ArchiveCampaignUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public ArchiveCampaignService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    @Transactional
    public Campaign execute(Command command) {
        Campaign campaign = campaignRepositoryPort.findById(command.campaignId())
                .orElseThrow(() -> new CampaignNotFoundException(command.campaignId()));
        campaign.archive(command.adminId());
        return campaignRepositoryPort.save(campaign);
    }
}
