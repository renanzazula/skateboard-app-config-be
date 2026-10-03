package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.PrivacyPolicy;
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
 * Unit-level coverage of {@link PrivacyPolicyPersistenceAdapter}'s mapping
 * to/from {@link PrivacyPolicyJpaEntity}. No database:
 * {@link SpringPrivacyPolicyRepository} is mocked with
 * {@code CALLS_REAL_METHODS} so {@code findSingleton()} also runs for real.
 * Mirrors {@link AboutPagePersistenceAdapterTest}, minus the JSON block
 * (de)serialization — {@code body} is a plain string column.
 */
class PrivacyPolicyPersistenceAdapterTest {

    private SpringPrivacyPolicyRepository jpaRepository;

    private PrivacyPolicyPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = Mockito.mock(SpringPrivacyPolicyRepository.class, Mockito.CALLS_REAL_METHODS);
        adapter = new PrivacyPolicyPersistenceAdapter(jpaRepository);
    }

    @Test
    void findReturnsEmptyWhenNoRowExists() {
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        assertThat(adapter.find()).isEmpty();
    }

    @Test
    void findMapsTheStoredRow() {
        PrivacyPolicyJpaEntity entity = new PrivacyPolicyJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("Privacy Policy");
        entity.setBody("We collect...");
        entity.setStatus("published");
        entity.setUpdatedBy("admin-1");
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        Optional<PrivacyPolicy> found = adapter.find();

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Privacy Policy");
        assertThat(found.get().getBody()).isEqualTo("We collect...");
        assertThat(found.get().getStatus()).isEqualTo(PrivacyPolicy.Status.PUBLISHED);
    }

    @Test
    void savesANewPage() {
        when(jpaRepository.findById(any())).thenReturn(Optional.empty());
        when(jpaRepository.save(any(PrivacyPolicyJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PrivacyPolicy page = PrivacyPolicy.createEmpty();
        page.update("Privacy Policy", "We collect...", PrivacyPolicy.Status.PUBLISHED, "admin-1");

        PrivacyPolicy saved = adapter.save(page);

        assertThat(saved.getTitle()).isEqualTo("Privacy Policy");
        assertThat(saved.getBody()).isEqualTo("We collect...");
        assertThat(saved.getStatus()).isEqualTo(PrivacyPolicy.Status.PUBLISHED);
    }

    @Test
    void savingAnExistingRowReusesTheEntityAndSkipsCreatedAt() {
        PrivacyPolicy page = PrivacyPolicy.createEmpty();
        page.update("Privacy Policy", "Body", PrivacyPolicy.Status.DRAFT, "admin-2");

        PrivacyPolicyJpaEntity existing = new PrivacyPolicyJpaEntity();
        existing.setId(page.getId());
        existing.setCreatedAt(java.time.Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findById(page.getId())).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(PrivacyPolicyJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(page);

        assertThat(existing.getCreatedAt()).isEqualTo(java.time.Instant.parse("2025-01-01T00:00:00Z"));
    }
}
