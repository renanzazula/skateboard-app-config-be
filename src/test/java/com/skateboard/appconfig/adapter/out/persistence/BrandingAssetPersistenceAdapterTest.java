package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.BrandingAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BrandingAssetPersistenceAdapterTest {

    @Mock
    private SpringBrandingAssetRepository jpaRepository;

    private BrandingAssetPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new BrandingAssetPersistenceAdapter(jpaRepository);
    }

    private static BrandingAsset asset(UUID id) {
        return BrandingAsset.create(id, "home-header", "assets/" + id + ".png", "image/png", "admin-1");
    }

    @Test
    void savesAndRoundTripsAnAsset() {
        when(jpaRepository.save(any(BrandingAssetJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        UUID id = UUID.randomUUID();

        BrandingAsset saved = adapter.save(asset(id));

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getName()).isEqualTo("home-header");
        assertThat(saved.getObjectKey()).isEqualTo("assets/" + id + ".png");
        assertThat(saved.getVersion()).isEqualTo(1);
    }

    @Test
    void findByIdMapsToTheDomain() {
        UUID id = UUID.randomUUID();
        BrandingAssetJpaEntity entity = new BrandingAssetJpaEntity();
        entity.setId(id);
        entity.setName("home-header");
        entity.setObjectKey("assets/" + id + ".png");
        entity.setContentType("image/png");
        entity.setVersion(2);
        entity.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2025-01-02T00:00:00Z"));
        entity.setUpdatedBy("admin-1");
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<BrandingAsset> found = adapter.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getVersion()).isEqualTo(2);
        assertThat(found.get().getCreatedAt()).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(adapter.findById(id)).isEmpty();
    }

    @Test
    void findByNameDelegatesToTheRepository() {
        UUID id = UUID.randomUUID();
        BrandingAssetJpaEntity entity = new BrandingAssetJpaEntity();
        entity.setId(id);
        entity.setName("home-header");
        when(jpaRepository.findByName("home-header")).thenReturn(Optional.of(entity));

        assertThat(adapter.findByName("home-header")).isPresent();
    }

    @Test
    void findAllMapsEveryRow() {
        BrandingAssetJpaEntity entity = new BrandingAssetJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("home-header");
        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        assertThat(adapter.findAll()).hasSize(1);
    }

    @Test
    void deleteByIdDelegatesToTheRepository() {
        UUID id = UUID.randomUUID();

        adapter.deleteById(id);

        verify(jpaRepository).deleteById(id);
    }

    @Test
    void existsByNameDelegatesToTheRepository() {
        when(jpaRepository.existsByName("home-header")).thenReturn(true);

        assertThat(adapter.existsByName("home-header")).isTrue();
    }
}
