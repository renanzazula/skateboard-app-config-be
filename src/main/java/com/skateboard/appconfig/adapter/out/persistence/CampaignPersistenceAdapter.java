package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignMediaAsset;
import com.skateboard.appconfig.domain.model.CampaignScreen;
import com.skateboard.appconfig.domain.model.CampaignStatus;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Maps the {@link Campaign} aggregate to/from JPA by hand (no MapStruct, like
 * every other adapter here). {@code save} reconciles the persistent screen and
 * media-asset rows against the aggregate's current state — updating what stayed,
 * inserting what's new and letting {@code orphanRemoval} delete what's gone —
 * so the whole graph round-trips through one call.
 */
@Component
public class CampaignPersistenceAdapter implements CampaignRepositoryPort {

    private final SpringCampaignRepository jpaRepository;

    public CampaignPersistenceAdapter(SpringCampaignRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Campaign save(Campaign campaign) {
        CampaignJpaEntity entity = jpaRepository.findById(campaign.getId()).orElse(null);
        if (entity == null) {
            entity = new CampaignJpaEntity();
            entity.setId(campaign.getId());
            entity.setCreatedBy(campaign.getCreatedBy());
            entity.setCreatedAt(campaign.getCreatedAt() != null ? campaign.getCreatedAt() : Instant.now());
        }
        entity.setName(campaign.getName());
        entity.setDescription(campaign.getDescription());
        entity.setStatus(campaign.getStatus());
        entity.setStartAt(campaign.getStartAt());
        entity.setEndAt(campaign.getEndAt());
        entity.setPriority(campaign.getPriority());
        entity.setAudience(campaign.getAudience());
        entity.setFrequencyType(campaign.getFrequencyType());
        entity.setMaxDisplaysPerDay(campaign.getMaxDisplaysPerDay());
        entity.setUpdatedBy(campaign.getUpdatedBy());
        entity.setUpdatedAt(campaign.getUpdatedAt());
        entity.setPublishedBy(campaign.getPublishedBy());
        entity.setPublishedAt(campaign.getPublishedAt());
        reconcileScreens(entity, campaign.getScreens());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Campaign> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Campaign> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Campaign> findEligibleForRuntime(Instant now, boolean authenticated) {
        List<CampaignAudience> audiences = authenticated
                ? List.of(CampaignAudience.ALL, CampaignAudience.AUTHENTICATED)
                : List.of(CampaignAudience.ALL, CampaignAudience.ANONYMOUS);
        return jpaRepository.findEligible(CampaignStatus.PUBLISHED, now, audiences).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    // --- write mapping ---

    private void reconcileScreens(CampaignJpaEntity entity, List<CampaignScreen> screens) {
        Map<UUID, CampaignScreenJpaEntity> existingById = new HashMap<>();
        for (CampaignScreenJpaEntity existing : entity.getScreens()) {
            existingById.put(existing.getId(), existing);
        }
        Set<UUID> desiredIds = screens.stream().map(CampaignScreen::getId).collect(Collectors.toSet());
        entity.getScreens().removeIf(existing -> !desiredIds.contains(existing.getId()));

        for (CampaignScreen screen : screens) {
            CampaignScreenJpaEntity screenEntity = existingById.get(screen.getId());
            if (screenEntity == null) {
                screenEntity = new CampaignScreenJpaEntity();
                screenEntity.setId(screen.getId());
                screenEntity.setCampaign(entity);
                entity.getScreens().add(screenEntity);
            }
            applyScreen(screenEntity, screen);
        }
    }

    private void applyScreen(CampaignScreenJpaEntity entity, CampaignScreen screen) {
        entity.setPosition(screen.getPosition());
        entity.setDurationSeconds(screen.getDurationSeconds());
        entity.setLayoutType(screen.getLayoutType());
        entity.setBackgroundColor(screen.getBackgroundColor());
        entity.setTitle(screen.getTitle());
        entity.setDescription(screen.getDescription());
        entity.setTextAlignment(screen.getTextAlignment());
        entity.setTitleSize(screen.getTitleSize());
        entity.setDescriptionSize(screen.getDescriptionSize());
        entity.setTextColor(screen.getTextColor());
        entity.setOverlayOpacity(screen.getOverlayOpacity());
        entity.setCloseEnabled(screen.isCloseEnabled());
        entity.setCloseAfterSeconds(screen.getCloseAfterSeconds());
        entity.setActionType(screen.getActionType());
        entity.setActionLabel(screen.getActionLabel());
        entity.setActionTarget(screen.getActionTarget());
        reconcileBackground(entity, screen.getBackground());
    }

    private void reconcileBackground(CampaignScreenJpaEntity screenEntity, CampaignMediaAsset asset) {
        CampaignMediaAssetJpaEntity current = screenEntity.getBackground();
        if (asset == null) {
            screenEntity.setBackground(null);
            return;
        }
        if (current != null && current.getId().equals(asset.getId())) {
            applyAsset(current, asset);
            return;
        }
        CampaignMediaAssetJpaEntity replacement = new CampaignMediaAssetJpaEntity();
        replacement.setId(asset.getId());
        applyAsset(replacement, asset);
        screenEntity.setBackground(replacement);
    }

    private void applyAsset(CampaignMediaAssetJpaEntity entity, CampaignMediaAsset asset) {
        entity.setStorageKey(asset.getStorageKey());
        entity.setVersion(asset.getVersion());
        entity.setMimeType(asset.getMimeType());
        entity.setWidth(asset.getWidth());
        entity.setHeight(asset.getHeight());
        entity.setSizeBytes(asset.getSizeBytes());
        entity.setFocalPointX(asset.getFocalPointX());
        entity.setFocalPointY(asset.getFocalPointY());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(asset.getCreatedAt() != null ? asset.getCreatedAt() : Instant.now());
        }
    }

    // --- read mapping ---

    private Campaign toDomain(CampaignJpaEntity entity) {
        List<CampaignScreen> screens = entity.getScreens().stream()
                .map(this::toScreenDomain)
                .collect(Collectors.toList());
        return Campaign.reconstitute(entity.getId(), entity.getName(), entity.getDescription(), entity.getStatus(),
                entity.getStartAt(), entity.getEndAt(), entity.getPriority(), entity.getAudience(),
                entity.getFrequencyType(), entity.getMaxDisplaysPerDay(), screens, entity.getCreatedBy(),
                entity.getCreatedAt(), entity.getUpdatedBy(), entity.getUpdatedAt(), entity.getPublishedBy(),
                entity.getPublishedAt());
    }

    private CampaignScreen toScreenDomain(CampaignScreenJpaEntity entity) {
        CampaignMediaAssetJpaEntity bg = entity.getBackground();
        CampaignMediaAsset asset = bg == null ? null : CampaignMediaAsset.reconstitute(bg.getId(),
                bg.getStorageKey(), bg.getVersion(), bg.getMimeType(), bg.getWidth(), bg.getHeight(),
                bg.getSizeBytes(), bg.getFocalPointX(), bg.getFocalPointY(), bg.getCreatedAt());
        return CampaignScreen.reconstitute(entity.getId(), entity.getPosition(), entity.getDurationSeconds(),
                entity.getLayoutType(), asset, entity.getBackgroundColor(), entity.getTitle(),
                entity.getDescription(), entity.getTextAlignment(), entity.getTitleSize(),
                entity.getDescriptionSize(), entity.getTextColor(), entity.getOverlayOpacity(),
                entity.isCloseEnabled(), entity.getCloseAfterSeconds(), entity.getActionType(),
                entity.getActionLabel(), entity.getActionTarget());
    }
}
