package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage of {@link HomeVideoCategoryConfigPersistenceAdapter}'s
 * mapping to/from {@link HomeVideoCategoryConfigJpaEntity}. No database:
 * {@link SpringHomeVideoCategoryConfigRepository} is mocked with
 * {@code CALLS_REAL_METHODS} so its {@code findSingleton()} default method
 * also runs for real.
 */
class HomeVideoCategoryConfigPersistenceAdapterTest {

    private SpringHomeVideoCategoryConfigRepository jpaRepository;

    private HomeVideoCategoryConfigPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = Mockito.mock(SpringHomeVideoCategoryConfigRepository.class, Mockito.CALLS_REAL_METHODS);
        adapter = new HomeVideoCategoryConfigPersistenceAdapter(jpaRepository);
    }

    @Test
    void createsDefaultsWhenNoRowExists() {
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(jpaRepository.findById(any())).thenReturn(Optional.empty());
        when(jpaRepository.save(any(HomeVideoCategoryConfigJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        HomeVideoCategoryConfig config = adapter.getOrCreate();

        assertThat(config.getId()).isNotNull();
        assertThat(config.getMode()).isEqualTo(HomeVideoCategoryConfig.Mode.ALL);
        assertThat(config.getEnabledCategoryIds()).isEmpty();
    }

    @Test
    void loadsTheExistingSingletonRow() {
        HomeVideoCategoryConfigJpaEntity entity = new HomeVideoCategoryConfigJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setMode(HomeVideoCategoryConfig.Mode.SELECTED);
        entity.setEnabledCategoryIds(Set.of("cat-1", "cat-2"));
        entity.setUpdatedBy("admin-1");
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        HomeVideoCategoryConfig config = adapter.getOrCreate();

        assertThat(config.getId()).isEqualTo(entity.getId());
        assertThat(config.getMode()).isEqualTo(HomeVideoCategoryConfig.Mode.SELECTED);
        assertThat(config.getEnabledCategoryIds()).containsExactlyInAnyOrder("cat-1", "cat-2");
        assertThat(config.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void savingAnExistingRowReusesTheEntityAndSkipsCreatedAt() {
        HomeVideoCategoryConfig config = HomeVideoCategoryConfig.createDefaults();
        config.updateCategories(HomeVideoCategoryConfig.Mode.SELECTED, Set.of("cat-3"));
        config.touch("admin-2");

        HomeVideoCategoryConfigJpaEntity existing = new HomeVideoCategoryConfigJpaEntity();
        existing.setId(config.getId());
        existing.setCreatedAt(java.time.Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findById(config.getId())).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(HomeVideoCategoryConfigJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        HomeVideoCategoryConfig saved = adapter.save(config);

        assertThat(saved.getEnabledCategoryIds()).containsExactly("cat-3");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-2");
        assertThat(existing.getCreatedAt()).isEqualTo(java.time.Instant.parse("2025-01-01T00:00:00Z"));
    }
}
