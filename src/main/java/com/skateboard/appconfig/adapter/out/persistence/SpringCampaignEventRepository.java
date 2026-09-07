package com.skateboard.appconfig.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface SpringCampaignEventRepository extends JpaRepository<CampaignEventJpaEntity, UUID> {

    /** Retention cleanup — see CampaignEventRetentionJob. Returns the row count deleted. */
    @Modifying
    @Query("delete from CampaignEventJpaEntity e where e.occurredAt < :cutoff")
    int deleteByOccurredAtBefore(@Param("cutoff") Instant cutoff);
}
