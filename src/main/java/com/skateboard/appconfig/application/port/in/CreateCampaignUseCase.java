package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;

import java.time.Instant;

public interface CreateCampaignUseCase {

    record Command(String adminId, String name, String description, Instant startAt, Instant endAt, int priority,
                   CampaignAudience audience, CampaignFrequencyType frequencyType, Integer maxDisplaysPerDay) {}

    Campaign execute(Command command);
}
