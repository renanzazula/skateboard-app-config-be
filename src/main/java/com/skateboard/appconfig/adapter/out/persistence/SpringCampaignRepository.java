package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringCampaignRepository extends JpaRepository<CampaignJpaEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = {"screens", "screens.background"})
    Optional<CampaignJpaEntity> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"screens", "screens.background"})
    List<CampaignJpaEntity> findAll();

    /**
     * Runtime eligibility filter (implementation plan, gap #4): published and
     * inside the schedule window, for the audiences a session may see. Ordered
     * highest priority first, then oldest first as a stable tie-break.
     */
    @EntityGraph(attributePaths = {"screens", "screens.background"})
    @Query("""
            select c from CampaignJpaEntity c
            where c.status = :status
              and c.startAt <= :now
              and c.endAt > :now
              and c.audience in :audiences
            order by c.priority desc, c.createdAt asc
            """)
    List<CampaignJpaEntity> findEligible(@Param("status") CampaignStatus status,
                                        @Param("now") Instant now,
                                        @Param("audiences") Collection<CampaignAudience> audiences);
}
