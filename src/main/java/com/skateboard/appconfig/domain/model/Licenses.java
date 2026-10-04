package com.skateboard.appconfig.domain.model;

import java.time.Instant;

/**
 * Singleton, application-wide Open-source Licenses page — exactly one row
 * for the whole application (no tenant scoping), mirroring {@link
 * PrivacyPolicy}: a plain {@code title}/{@code body} text pair, no content
 * blocks, no images. Reading it still requires an authenticated caller (see
 * api/openapi.yaml's {@code licenses} tag).
 */
public class Licenses {

    public enum Status { DRAFT, PUBLISHED }

    private final java.util.UUID id;
    private String title;
    private String body;
    private Status status;
    private final Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    private Licenses(java.util.UUID id, String title, String body, Status status,
                     Instant createdAt, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static Licenses createEmpty() {
        return new Licenses(java.util.UUID.randomUUID(), "", "", Status.DRAFT, Instant.now(), null, null);
    }

    public static Licenses reconstitute(java.util.UUID id, String title, String body, Status status,
                                        Instant createdAt, Instant updatedAt, String updatedBy) {
        return new Licenses(id, title, body, status, createdAt, updatedAt, updatedBy);
    }

    public void update(String title, String body, Status status, String adminId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("A page title is required.");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Page body text is required.");
        }
        if (status == null) {
            throw new IllegalArgumentException("A status is required.");
        }
        this.title = title.strip();
        this.body = body.strip();
        this.status = status;
        this.updatedAt = Instant.now();
        this.updatedBy = adminId;
    }

    public boolean isPublished() {
        return status == Status.PUBLISHED;
    }

    public java.util.UUID getId() { return id; }
    public String getTitle()      { return title; }
    public String getBody()       { return body; }
    public Status getStatus()     { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy()  { return updatedBy; }
}
