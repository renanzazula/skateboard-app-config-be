package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.FeaturedContentSource;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage of {@link HomeFeaturedPlayerConfigPersistenceAdapter}'s
 * mapping to/from {@link HomeFeaturedPlayerConfigJpaEntity}. No database:
 * {@link SpringHomeFeaturedPlayerConfigRepository} is mocked with
 * {@code CALLS_REAL_METHODS} so its {@code findSingleton()} default method
 * also runs for real.
 */
class HomeFeaturedPlayerConfigPersistenceAdapterTest {

    private SpringHomeFeaturedPlayerConfigRepository jpaRepository;

    private HomeFeaturedPlayerConfigPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = Mockito.mock(SpringHomeFeaturedPlayerConfigRepository.class, Mockito.CALLS_REAL_METHODS);
        adapter = new HomeFeaturedPlayerConfigPersistenceAdapter(jpaRepository);
    }

    @Test
    void createsDefaultsWhenNoRowExists() {
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(jpaRepository.findById(any())).thenReturn(Optional.empty());
        when(jpaRepository.save(any(HomeFeaturedPlayerConfigJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        HomeFeaturedPlayerConfig config = adapter.getOrCreate();

        assertThat(config.getId()).isNotNull();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getPlayerType()).isEqualTo(HomeFeaturedPlayerConfig.PlayerType.MINI);
    }

    @Test
    void loadsTheExistingSingletonRow() {
        HomeFeaturedPlayerConfigJpaEntity entity = new HomeFeaturedPlayerConfigJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setEnabled(true);
        entity.setContentSource(FeaturedContentSource.PODCAST);
        entity.setContentId("ep-1");
        entity.setPlayerType(HomeFeaturedPlayerConfig.PlayerType.MINI);
        entity.setPosition(HomeFeaturedPlayerConfig.Position.BOTTOM);
        entity.setPreferredPlatform(HomeFeaturedPlayerConfig.PreferredPlatform.SPOTIFY);
        entity.setSelectionMode(HomeFeaturedPlayerConfig.SelectionMode.MANUAL);
        entity.setUpdatedBy("admin-1");
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        HomeFeaturedPlayerConfig config = adapter.getOrCreate();

        assertThat(config.getId()).isEqualTo(entity.getId());
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getContentSource()).isEqualTo(FeaturedContentSource.PODCAST);
        assertThat(config.getContentId()).isEqualTo("ep-1");
        assertThat(config.getPosition()).isEqualTo(HomeFeaturedPlayerConfig.Position.BOTTOM);
        assertThat(config.getPreferredPlatform()).isEqualTo(HomeFeaturedPlayerConfig.PreferredPlatform.SPOTIFY);
        assertThat(config.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void savingAnExistingRowReusesTheEntityAndSkipsCreatedAt() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();
        config.update(true, FeaturedContentSource.PODCAST, "ep-2", HomeFeaturedPlayerConfig.PlayerType.MINI,
                HomeFeaturedPlayerConfig.Position.TOP, HomeFeaturedPlayerConfig.PreferredPlatform.YOUTUBE,
                HomeFeaturedPlayerConfig.SelectionMode.MANUAL);
        config.touch("admin-2");

        HomeFeaturedPlayerConfigJpaEntity existing = new HomeFeaturedPlayerConfigJpaEntity();
        existing.setId(config.getId());
        existing.setCreatedAt(java.time.Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findById(config.getId())).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(HomeFeaturedPlayerConfigJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        HomeFeaturedPlayerConfig saved = adapter.save(config);

        assertThat(saved.getContentId()).isEqualTo("ep-2");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-2");
        assertThat(existing.getCreatedAt()).isEqualTo(java.time.Instant.parse("2025-01-01T00:00:00Z"));
    }
}
