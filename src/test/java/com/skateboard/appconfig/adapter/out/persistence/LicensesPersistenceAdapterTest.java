package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.Licenses;
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
 * Unit-level coverage of {@link LicensesPersistenceAdapter}'s mapping to/from
 * {@link LicensesJpaEntity}. No database: {@link SpringLicensesRepository} is
 * mocked with {@code CALLS_REAL_METHODS} so {@code findSingleton()} also
 * runs for real. Mirrors {@link PrivacyPolicyPersistenceAdapterTest}.
 */
class LicensesPersistenceAdapterTest {

    private SpringLicensesRepository jpaRepository;

    private LicensesPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = Mockito.mock(SpringLicensesRepository.class, Mockito.CALLS_REAL_METHODS);
        adapter = new LicensesPersistenceAdapter(jpaRepository);
    }

    @Test
    void findReturnsEmptyWhenNoRowExists() {
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        assertThat(adapter.find()).isEmpty();
    }

    @Test
    void findMapsTheStoredRow() {
        LicensesJpaEntity entity = new LicensesJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("Open-source Licenses");
        entity.setBody("This app uses...");
        entity.setStatus("published");
        entity.setUpdatedBy("admin-1");
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        Optional<Licenses> found = adapter.find();

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Open-source Licenses");
        assertThat(found.get().getBody()).isEqualTo("This app uses...");
        assertThat(found.get().getStatus()).isEqualTo(Licenses.Status.PUBLISHED);
    }

    @Test
    void savesANewPage() {
        when(jpaRepository.findById(any())).thenReturn(Optional.empty());
        when(jpaRepository.save(any(LicensesJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Licenses page = Licenses.createEmpty();
        page.update("Open-source Licenses", "This app uses...", Licenses.Status.PUBLISHED, "admin-1");

        Licenses saved = adapter.save(page);

        assertThat(saved.getTitle()).isEqualTo("Open-source Licenses");
        assertThat(saved.getBody()).isEqualTo("This app uses...");
        assertThat(saved.getStatus()).isEqualTo(Licenses.Status.PUBLISHED);
    }

    @Test
    void savingAnExistingRowReusesTheEntityAndSkipsCreatedAt() {
        Licenses page = Licenses.createEmpty();
        page.update("Open-source Licenses", "Body", Licenses.Status.DRAFT, "admin-2");

        LicensesJpaEntity existing = new LicensesJpaEntity();
        existing.setId(page.getId());
        existing.setCreatedAt(java.time.Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findById(page.getId())).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(LicensesJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(page);

        assertThat(existing.getCreatedAt()).isEqualTo(java.time.Instant.parse("2025-01-01T00:00:00Z"));
    }
}
