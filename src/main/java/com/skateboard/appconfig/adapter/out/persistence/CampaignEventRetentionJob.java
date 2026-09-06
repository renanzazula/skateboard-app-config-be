package com.skateboard.appconfig.adapter.out.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Deletes {@code campaign_event} rows past the retention window. The table is
 * append-only analytics with no aggregation/reporting in V1 and would
 * otherwise grow without bound. Runs daily; the window defaults to 90 days
 * ({@code app.campaign.events.retention-days}).
 */
@Component
public class CampaignEventRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(CampaignEventRetentionJob.class);

    private final SpringCampaignEventRepository repository;
    private final long retentionDays;

    public CampaignEventRetentionJob(SpringCampaignEventRepository repository,
                                     @Value("${app.campaign.events.retention-days:90}") long retentionDays) {
        this.repository = repository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "${app.campaign.events.retention-cron:0 30 3 * * *}")
    @Transactional
    public void purgeExpired() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        int deleted = repository.deleteByOccurredAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Purged {} campaign_event rows older than {}", deleted, cutoff);
        }
    }
}
