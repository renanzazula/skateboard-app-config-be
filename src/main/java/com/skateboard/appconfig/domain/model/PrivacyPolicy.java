package com.skateboard.appconfig.domain.model;

import java.time.Instant;

/**
 * Singleton, application-wide Privacy Policy page — exactly one row for the
 * whole application (no tenant scoping), mirroring {@link AboutPage}.
 * <p>
 * Simpler than {@link AboutPage}: a plain {@code title}/{@code body} text
 * pair, no content blocks, no images — reading the published row requires no
 * authentication at all (see api/openapi.yaml's {@code privacy-policy} tag).
 */
public class PrivacyPolicy {

    public enum Status { DRAFT, PUBLISHED }

    private final java.util.UUID id;
    private String title;
    private String body;
    private Status status;
    private final Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    private PrivacyPolicy(java.util.UUID id, String title, String body, Status status,
                          Instant createdAt, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static PrivacyPolicy createEmpty() {
        return new PrivacyPolicy(java.util.UUID.randomUUID(), "", "", Status.DRAFT, Instant.now(), null, null);
    }

    public static PrivacyPolicy reconstitute(java.util.UUID id, String title, String body, Status status,
                                             Instant createdAt, Instant updatedAt, String updatedBy) {
        return new PrivacyPolicy(id, title, body, status, createdAt, updatedAt, updatedBy);
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
