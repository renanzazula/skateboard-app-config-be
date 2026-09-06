package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Campaign;

import java.util.List;

public interface ListCampaignsUseCase {

    List<Campaign> execute();
}
