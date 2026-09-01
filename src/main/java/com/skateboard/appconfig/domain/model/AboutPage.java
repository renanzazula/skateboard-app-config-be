package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Singleton, application-wide "About Us" page — exactly one row for the whole
 * application (no tenant scoping), mirroring {@link HomeVideoCategoryConfig}
 * and {@link AppConfig}.
 * <p>
 * {@code blocks} is an ordered list of typed content blocks kept opaque here
 * (each element is whatever the frontend serialized — a {@code Map}); this
 * service does not own the block schema, the same way it does not own category
 * IDs. Image references inside a block are stored as bare object keys and
 * re-signed by the adapter layer on every read.
 */
public class AboutPage {

    public enum Status { DRAFT, PUBLISHED }

    private final UUID id;
    private String title;
    private String subtitle;
    private Status status;
    private List<Map<String, Object>> blocks;
    private final Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    private AboutPage(UUID id, String title, String subtitle, Status status, List<Map<String, Object>> blocks,
                      Instant createdAt, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.status = status;
        this.blocks = new ArrayList<>(blocks);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static AboutPage createEmpty() {
        return new AboutPage(UUID.randomUUID(), "", null, Status.DRAFT, List.of(), Instant.now(), null, null);
    }

    public static AboutPage reconstitute(UUID id, String title, String subtitle, Status status, List<Map<String, Object>> blocks,
                                         Instant createdAt, Instant updatedAt, String updatedBy) {
        return new AboutPage(id, title, subtitle, status, blocks, createdAt, updatedAt, updatedBy);
    }

    public void update(String title, String subtitle, Status status, List<Map<String, Object>> blocks, String adminId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("A page title is required.");
        }
        if (status == null) {
            throw new IllegalArgumentException("A status is required.");
        }
        this.title = title.strip();
        this.subtitle = (subtitle == null || subtitle.isBlank()) ? null : subtitle.strip();
        this.status = status;
        this.blocks = blocks == null ? new ArrayList<>() : new ArrayList<>(blocks);
        this.updatedAt = Instant.now();
        this.updatedBy = adminId;
    }

    public boolean isPublished() {
        return status == Status.PUBLISHED;
    }

    public UUID getId()            { return id; }
    public String getTitle()       { return title; }
    public String getSubtitle()    { return subtitle; }
    public Status getStatus()      { return status; }
    public List<Map<String, Object>> getBlocks() { return Collections.unmodifiableList(blocks); }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getUpdatedAt()  { return updatedAt; }
    public String getUpdatedBy()   { return updatedBy; }
}
