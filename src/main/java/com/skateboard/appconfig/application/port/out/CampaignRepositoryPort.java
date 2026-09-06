package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.Campaign;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for the {@link Campaign} aggregate. Combined
 * (not split into {@code Load}/{@code Save}) because a campaign is a genuine
 * multi-row aggregate, mirroring {@link BrandingAssetRepositoryPort} — the
 * {@code Load}/{@code Save} split is reserved for the singleton configs.
 * <p>
 * Every {@code find} returns the aggregate whole (its screens and their
 * background assets loaded); {@code save} persists the whole graph, deleting
 * screens/assets that the aggregate no longer holds.
 */
public interface CampaignRepositoryPort {

    Campaign save(Campaign campaign);

    Optional<Campaign> findById(UUID id);

    /** All campaigns regardless of status, for the admin list. */
    List<Campaign> findAll();

    /**
     * Campaigns eligible to be shown to the runtime app at {@code now}: published,
     * inside their schedule window, and matching a session with the given auth
     * state. Ordered highest {@code priority} first (ties broken by creation
     * order). Eligibility is computed here rather than stored (implementation
     * plan, gap #4).
     */
    List<Campaign> findEligibleForRuntime(Instant now, boolean authenticated);

    void deleteById(UUID id);
}
