package com.skateboard.appconfig.adapter.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skateboard.appconfig.application.port.out.LoadAboutPagePort;
import com.skateboard.appconfig.application.port.out.SaveAboutPagePort;
import com.skateboard.appconfig.domain.model.AboutPage;
import org.springframework.stereotype.Component;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AboutPagePersistenceAdapter implements LoadAboutPagePort, SaveAboutPagePort {

    private static final TypeReference<List<Map<String, Object>>> BLOCK_LIST = new TypeReference<>() {};

    private final SpringAboutPageRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public AboutPagePersistenceAdapter(SpringAboutPageRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<AboutPage> find() {
        return Optional.ofNullable(jpaRepository.findSingleton()).map(this::toDomain);
    }

    @Override
    public AboutPage save(AboutPage page) {
        AboutPageJpaEntity existing = jpaRepository.findById(page.getId()).orElse(null);
        AboutPageJpaEntity entity = existing != null ? existing : new AboutPageJpaEntity();
        if (existing == null) {
            entity.setCreatedAt(page.getCreatedAt() != null ? page.getCreatedAt() : Instant.now());
        }
        entity.setId(page.getId());
        entity.setTitle(page.getTitle());
        entity.setSubtitle(page.getSubtitle());
        entity.setStatus(page.getStatus().name().toLowerCase());
        entity.setBlocks(writeBlocks(page.getBlocks()));
        entity.setUpdatedBy(page.getUpdatedBy());
        entity.setUpdatedAt(page.getUpdatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    private AboutPage toDomain(AboutPageJpaEntity e) {
        return AboutPage.reconstitute(
                e.getId(),
                e.getTitle(),
                e.getSubtitle(),
                AboutPage.Status.valueOf(e.getStatus().toUpperCase()),
                readBlocks(e.getBlocks()),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getUpdatedBy());
    }

    private List<Map<String, Object>> readBlocks(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, BLOCK_LIST);
        } catch (Exception e) {
            throw new UncheckedIOException("Stored About Us blocks are not valid JSON", asIo(e));
        }
    }

    private String writeBlocks(List<Map<String, Object>> blocks) {
        try {
            return objectMapper.writeValueAsString(blocks == null ? List.of() : blocks);
        } catch (Exception e) {
            throw new UncheckedIOException("Could not serialize About Us blocks", asIo(e));
        }
    }

    private java.io.IOException asIo(Exception e) {
        return e instanceof java.io.IOException io ? io : new java.io.IOException(e);
    }
}
