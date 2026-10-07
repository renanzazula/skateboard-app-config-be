package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.GuestApplicationConfig;
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
 * Unit-level coverage of {@link GuestApplicationConfigPersistenceAdapter}'s
 * mapping to/from {@link GuestApplicationConfigJpaEntity}. No database:
 * {@link SpringGuestApplicationConfigRepository} is mocked with
 * {@code CALLS_REAL_METHODS} so its {@code findSingleton()} default method
 * also runs for real — mirrors HomeFeaturedPlayerConfigPersistenceAdapterTest.
 */
class GuestApplicationConfigPersistenceAdapterTest {

    private SpringGuestApplicationConfigRepository jpaRepository;

    private GuestApplicationConfigPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = Mockito.mock(SpringGuestApplicationConfigRepository.class, Mockito.CALLS_REAL_METHODS);
        adapter = new GuestApplicationConfigPersistenceAdapter(jpaRepository);
    }

    @Test
    void createsDefaultsWhenNoRowExists() {
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(jpaRepository.findById(any())).thenReturn(Optional.empty());
        when(jpaRepository.save(any(GuestApplicationConfigJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        GuestApplicationConfig config = adapter.getOrCreate();

        assertThat(config.getId()).isNotNull();
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getRecipientIds()).isEmpty();
    }

    @Test
    void loadsTheExistingSingletonRow() {
        UUID recipient = UUID.randomUUID();
        GuestApplicationConfigJpaEntity entity = new GuestApplicationConfigJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setEnabled(true);
        entity.setRecipientIds(Set.of(recipient));
        entity.setUpdatedBy("admin-1");
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        GuestApplicationConfig config = adapter.getOrCreate();

        assertThat(config.getId()).isEqualTo(entity.getId());
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getRecipientIds()).containsExactly(recipient);
        assertThat(config.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void savingAnExistingRowReusesTheEntityAndSkipsCreatedAt() {
        UUID recipient = UUID.randomUUID();
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();
        config.update(true, Set.of(recipient), "admin-2");

        GuestApplicationConfigJpaEntity existing = new GuestApplicationConfigJpaEntity();
        existing.setId(config.getId());
        existing.setCreatedAt(java.time.Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findById(config.getId())).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(GuestApplicationConfigJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        GuestApplicationConfig saved = adapter.save(config);

        assertThat(saved.getRecipientIds()).containsExactly(recipient);
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-2");
        assertThat(existing.getCreatedAt()).isEqualTo(java.time.Instant.parse("2025-01-01T00:00:00Z"));
    }
}
