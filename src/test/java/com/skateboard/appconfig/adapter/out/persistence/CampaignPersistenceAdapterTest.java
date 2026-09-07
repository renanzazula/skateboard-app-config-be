package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignActionType;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import com.skateboard.appconfig.domain.model.CampaignLayoutType;
import com.skateboard.appconfig.domain.model.CampaignMediaAsset;
import com.skateboard.appconfig.domain.model.CampaignScreen;
import com.skateboard.appconfig.domain.model.CampaignStatus;
import com.skateboard.appconfig.domain.model.CampaignTextAlignment;
import com.skateboard.appconfig.domain.model.CampaignTextSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage of {@link CampaignPersistenceAdapter}'s hand-written
 * mapping and — the part with real logic — {@code save}'s screen/media
 * reconciliation. No database: the {@link SpringCampaignRepository} is mocked
 * and the entity handed to {@code save(...)} is captured and inspected.
 */
class CampaignPersistenceAdapterTest {

    @Mock
    private SpringCampaignRepository jpaRepository;

    private CampaignPersistenceAdapter adapter;

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-02-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new CampaignPersistenceAdapter(jpaRepository);
        when(jpaRepository.save(any(CampaignJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static CampaignScreen screen(UUID id, int position, int duration, CampaignMediaAsset background) {
        return CampaignScreen.reconstitute(id, position, duration, CampaignLayoutType.FULL_BACKGROUND, background,
                "#101010", "Title " + position, null, CampaignTextAlignment.CENTER, CampaignTextSize.LARGE,
                CampaignTextSize.MEDIUM, null, null, false, null, CampaignActionType.NONE, null, null);
    }

    private static CampaignMediaAsset asset(UUID id, int version) {
        return CampaignMediaAsset.reconstitute(id, "campaigns/" + id + ".webp", version, "image/webp",
                1080, 1920, 200_000L, 0.5, 0.5, START);
    }

    private Campaign campaignWith(UUID id, List<CampaignScreen> screens) {
        return Campaign.reconstitute(id, "Launch week", null, CampaignStatus.DRAFT, START, END, 5,
                CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, screens, "admin-1", START,
                "admin-1", START, null, null);
    }

    private CampaignJpaEntity persistedWith(UUID id, CampaignScreen... screens) {
        // Round-trip a fresh entity through save() so the JPA graph matches the domain.
        adapter.save(campaignWith(id, List.of(screens)));
        ArgumentCaptor<CampaignJpaEntity> captor = ArgumentCaptor.forClass(CampaignJpaEntity.class);
        org.mockito.Mockito.verify(jpaRepository).save(captor.capture());
        CampaignJpaEntity entity = captor.getValue();
        org.mockito.Mockito.reset(jpaRepository);
        when(jpaRepository.save(any(CampaignJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        return entity;
    }

    @Test
    void insertsANewCampaignWhenNoRowExists() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(java.util.Optional.empty());

        Campaign saved = adapter.save(campaignWith(id, List.of(screen(UUID.randomUUID(), 1, 4, null))));

        ArgumentCaptor<CampaignJpaEntity> captor = ArgumentCaptor.forClass(CampaignJpaEntity.class);
        org.mockito.Mockito.verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(id);
        assertThat(captor.getValue().getScreens()).hasSize(1);
        assertThat(saved.getScreens()).hasSize(1);
    }

    @Test
    void reconcilesScreens_keepsUpdated_dropsRemoved_addsNew() {
        UUID id = UUID.randomUUID();
        UUID keep = UUID.randomUUID();
        UUID drop = UUID.randomUUID();
        CampaignJpaEntity existing = persistedWith(id, screen(keep, 1, 3, null), screen(drop, 2, 3, null));
        when(jpaRepository.findById(id)).thenReturn(java.util.Optional.of(existing));

        UUID add = UUID.randomUUID();
        adapter.save(campaignWith(id, List.of(
                screen(keep, 1, 9, null),   // same id, changed duration
                screen(add, 2, 3, null))));  // brand new

        ArgumentCaptor<CampaignJpaEntity> captor = ArgumentCaptor.forClass(CampaignJpaEntity.class);
        org.mockito.Mockito.verify(jpaRepository).save(captor.capture());
        List<CampaignScreenJpaEntity> screens = captor.getValue().getScreens();

        assertThat(screens).extracting(CampaignScreenJpaEntity::getId).containsExactlyInAnyOrder(keep, add);
        assertThat(screens).filteredOn(s -> s.getId().equals(keep)).singleElement()
                .extracting(CampaignScreenJpaEntity::getDurationSeconds).isEqualTo(9);
    }

    @Test
    void reconcilesBackground_updatesInPlace_replacesOnNewId_clearsOnNull() {
        UUID id = UUID.randomUUID();
        UUID screenId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        CampaignJpaEntity existing = persistedWith(id, screen(screenId, 1, 3, asset(assetId, 1)));
        when(jpaRepository.findById(id)).thenReturn(java.util.Optional.of(existing));

        // same asset id, new version -> in-place update, same entity instance
        CampaignMediaAssetJpaEntity beforeBg = existing.getScreens().get(0).getBackground();
        adapter.save(campaignWith(id, List.of(screen(screenId, 1, 3, asset(assetId, 4)))));
        CampaignMediaAssetJpaEntity afterBg = existing.getScreens().get(0).getBackground();
        assertThat(afterBg).isSameAs(beforeBg);
        assertThat(afterBg.getVersion()).isEqualTo(4);

        // different asset id -> replacement entity
        UUID newAssetId = UUID.randomUUID();
        adapter.save(campaignWith(id, List.of(screen(screenId, 1, 3, asset(newAssetId, 1)))));
        assertThat(existing.getScreens().get(0).getBackground().getId()).isEqualTo(newAssetId);

        // null -> cleared
        adapter.save(campaignWith(id, List.of(screen(screenId, 1, 3, null))));
        assertThat(existing.getScreens().get(0).getBackground()).isNull();
    }

    @Test
    void roundTripsTheAggregateBackToTheDomain() {
        UUID id = UUID.randomUUID();
        UUID screenId = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(java.util.Optional.empty());

        Campaign saved = adapter.save(campaignWith(id, List.of(screen(screenId, 1, 6, asset(UUID.randomUUID(), 2)))));

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getScreens()).singleElement().satisfies(s -> {
            assertThat(s.getId()).isEqualTo(screenId);
            assertThat(s.getDurationSeconds()).isEqualTo(6);
            assertThat(s.getBackground()).isNotNull();
            assertThat(s.getBackground().getVersion()).isEqualTo(2);
        });
    }

    @Test
    void findEligibleForRuntime_widensAudienceByAuthState() {
        when(jpaRepository.findEligible(any(), any(), any())).thenReturn(List.of());

        adapter.findEligibleForRuntime(START, true);
        adapter.findEligibleForRuntime(START, false);

        ArgumentCaptor<List<CampaignAudience>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(jpaRepository, org.mockito.Mockito.times(2))
                .findEligible(org.mockito.ArgumentMatchers.eq(CampaignStatus.PUBLISHED), any(), captor.capture());

        assertThat(captor.getAllValues().get(0)).containsExactlyInAnyOrder(CampaignAudience.ALL, CampaignAudience.AUTHENTICATED);
        assertThat(captor.getAllValues().get(1)).containsExactlyInAnyOrder(CampaignAudience.ALL, CampaignAudience.ANONYMOUS);
    }
}
