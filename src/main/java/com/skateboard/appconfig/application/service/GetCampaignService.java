package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCampaignService implements GetCampaignUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public GetCampaignService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    public Campaign execute(UUID campaignId) {
        return campaignRepositoryPort.findById(campaignId)
                .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }
}
