package com.skateboard.appconfig.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.appconfig.domain.model.AboutPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage of {@link AboutPagePersistenceAdapter}'s mapping
 * to/from {@link AboutPageJpaEntity}, including the JSON (de)serialization of
 * the opaque block list. No database: {@link SpringAboutPageRepository} is
 * mocked with {@code CALLS_REAL_METHODS} so {@code findSingleton()} also
 * runs for real; a plain {@link ObjectMapper} does the real JSON work.
 */
class AboutPagePersistenceAdapterTest {

    private SpringAboutPageRepository jpaRepository;

    private AboutPagePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = Mockito.mock(SpringAboutPageRepository.class, Mockito.CALLS_REAL_METHODS);
        adapter = new AboutPagePersistenceAdapter(jpaRepository, new ObjectMapper());
    }

    @Test
    void findReturnsEmptyWhenNoRowExists() {
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        assertThat(adapter.find()).isEmpty();
    }

    @Test
    void findMapsTheStoredRowIncludingBlocks() {
        AboutPageJpaEntity entity = new AboutPageJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("About us");
        entity.setSubtitle("Sub");
        entity.setStatus("published");
        entity.setBlocks("[{\"type\":\"text\",\"value\":\"hello\"}]");
        entity.setUpdatedBy("admin-1");
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        Optional<AboutPage> found = adapter.find();

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("About us");
        assertThat(found.get().getStatus()).isEqualTo(AboutPage.Status.PUBLISHED);
        assertThat(found.get().getBlocks()).hasSize(1);
        assertThat(found.get().getBlocks().get(0)).containsEntry("type", "text");
    }

    @Test
    void findTreatsBlankBlocksAsEmpty() {
        AboutPageJpaEntity entity = new AboutPageJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("About us");
        entity.setStatus("draft");
        entity.setBlocks(null);
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        Optional<AboutPage> found = adapter.find();

        assertThat(found).isPresent();
        assertThat(found.get().getBlocks()).isEmpty();
    }

    @Test
    void savesANewPageSerializingItsBlocks() {
        when(jpaRepository.findById(any())).thenReturn(Optional.empty());
        when(jpaRepository.save(any(AboutPageJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AboutPage page = AboutPage.createEmpty();
        page.update("About us", "Sub", AboutPage.Status.PUBLISHED,
                List.of(Map.of("type", "text", "value", "hi")), "admin-1");

        AboutPage saved = adapter.save(page);

        assertThat(saved.getTitle()).isEqualTo("About us");
        assertThat(saved.getStatus()).isEqualTo(AboutPage.Status.PUBLISHED);
        assertThat(saved.getBlocks()).hasSize(1);
    }

    @Test
    void savingAnExistingRowReusesTheEntityAndSkipsCreatedAt() {
        AboutPage page = AboutPage.createEmpty();
        page.update("About us", null, AboutPage.Status.DRAFT, List.of(), "admin-2");

        AboutPageJpaEntity existing = new AboutPageJpaEntity();
        existing.setId(page.getId());
        existing.setCreatedAt(java.time.Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findById(page.getId())).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(AboutPageJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(page);

        assertThat(existing.getCreatedAt()).isEqualTo(java.time.Instant.parse("2025-01-01T00:00:00Z"));
    }
}
