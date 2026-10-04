package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.Terms;
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
 * Unit-level coverage of {@link TermsPersistenceAdapter}'s mapping to/from
 * {@link TermsJpaEntity}. No database: {@link SpringTermsRepository} is
 * mocked with {@code CALLS_REAL_METHODS} so {@code findSingleton()} also
 * runs for real. Mirrors {@link PrivacyPolicyPersistenceAdapterTest}.
 */
class TermsPersistenceAdapterTest {

    private SpringTermsRepository jpaRepository;

    private TermsPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = Mockito.mock(SpringTermsRepository.class, Mockito.CALLS_REAL_METHODS);
        adapter = new TermsPersistenceAdapter(jpaRepository);
    }

    @Test
    void findReturnsEmptyWhenNoRowExists() {
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        assertThat(adapter.find()).isEmpty();
    }

    @Test
    void findMapsTheStoredRow() {
        TermsJpaEntity entity = new TermsJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("Terms & Conditions");
        entity.setBody("By using this app...");
        entity.setStatus("published");
        entity.setUpdatedBy("admin-1");
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        Optional<Terms> found = adapter.find();

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Terms & Conditions");
        assertThat(found.get().getBody()).isEqualTo("By using this app...");
        assertThat(found.get().getStatus()).isEqualTo(Terms.Status.PUBLISHED);
    }

    @Test
    void savesANewPage() {
        when(jpaRepository.findById(any())).thenReturn(Optional.empty());
        when(jpaRepository.save(any(TermsJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Terms page = Terms.createEmpty();
        page.update("Terms & Conditions", "By using this app...", Terms.Status.PUBLISHED, "admin-1");

        Terms saved = adapter.save(page);

        assertThat(saved.getTitle()).isEqualTo("Terms & Conditions");
        assertThat(saved.getBody()).isEqualTo("By using this app...");
        assertThat(saved.getStatus()).isEqualTo(Terms.Status.PUBLISHED);
    }

    @Test
    void savingAnExistingRowReusesTheEntityAndSkipsCreatedAt() {
        Terms page = Terms.createEmpty();
        page.update("Terms & Conditions", "Body", Terms.Status.DRAFT, "admin-2");

        TermsJpaEntity existing = new TermsJpaEntity();
        existing.setId(page.getId());
        existing.setCreatedAt(java.time.Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findById(page.getId())).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(TermsJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(page);

        assertThat(existing.getCreatedAt()).isEqualTo(java.time.Instant.parse("2025-01-01T00:00:00Z"));
    }
}
