package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetActiveCampaignsUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.model.Campaign;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GetActiveCampaignsService implements GetActiveCampaignsUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public GetActiveCampaignsService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    public List<Campaign> execute(boolean authenticated) {
        return campaignRepositoryPort.findEligibleForRuntime(Instant.now(), authenticated);
    }
}
