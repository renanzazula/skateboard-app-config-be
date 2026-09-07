package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ListCampaignsUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.model.Campaign;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListCampaignsService implements ListCampaignsUseCase {

    private final CampaignRepositoryPort campaignRepositoryPort;

    public ListCampaignsService(CampaignRepositoryPort campaignRepositoryPort) {
        this.campaignRepositoryPort = campaignRepositoryPort;
    }

    @Override
    public List<Campaign> execute() {
        return campaignRepositoryPort.findAll();
    }
}
