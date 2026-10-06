package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Singleton, app-wide configuration for the podcast Guest Application
 * feature — there is exactly one row for the whole application (no tenant
 * scoping), mirroring {@link HomeFeaturedPlayerConfig}. {@code recipientIds}
 * are Keycloak user IDs; this service stores them as-is and does not call
 * skateboard-user-be to verify they are active administrators — that
 * resolution happens downstream (BFF / notification-be), which is also where
 * actual addresses are looked up. See api/openapi.yaml's
 * updateGuestApplicationSettings description for why this boundary is drawn
 * here.
 */
public class GuestApplicationConfig {

    /** Only {name} is supported in V1 — any other {placeholder} is rejected by update(). */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^{}]*)}");
    private static final String SUPPORTED_PLACEHOLDER = "name";

    public static final String DEFAULT_CONFIRMATION_SUBJECT = "We've received your podcast guest application";
    public static final String DEFAULT_CONFIRMATION_BODY =
            "Thank you for your interest in joining our podcast! We've received your application "
                    + "and will contact you as soon as possible.";

    private final UUID id;
    private boolean enabled;
    private Set<UUID> recipientIds;
    private String confirmationSubject;
    private String confirmationBody;
    private final Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    private GuestApplicationConfig(UUID id, boolean enabled, Set<UUID> recipientIds, String confirmationSubject,
                                    String confirmationBody, Instant createdAt, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.enabled = enabled;
        this.recipientIds = recipientIds;
        this.confirmationSubject = confirmationSubject;
        this.confirmationBody = confirmationBody;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    /** Defaults for a brand-new singleton row only (first-ever GET before any admin has configured anything). */
    public static GuestApplicationConfig createDefaults() {
        return new GuestApplicationConfig(UUID.randomUUID(), false, new LinkedHashSet<>(),
                DEFAULT_CONFIRMATION_SUBJECT, DEFAULT_CONFIRMATION_BODY, Instant.now(), null, null);
    }

    public static GuestApplicationConfig reconstitute(UUID id, boolean enabled, Set<UUID> recipientIds,
                                                        String confirmationSubject, String confirmationBody,
                                                        Instant createdAt, Instant updatedAt, String updatedBy) {
        return new GuestApplicationConfig(id, enabled, new LinkedHashSet<>(recipientIds), confirmationSubject,
                confirmationBody, createdAt, updatedAt, updatedBy);
    }

    /**
     * Per the README's acceptance criteria: enabling requires at least one
     * recipient, and the templates must only use the {name} placeholder so a
     * later renderer never encounters something it doesn't know how to
     * (HTML-)escape.
     */
    public void update(boolean enabled, Set<UUID> recipientIds, String confirmationSubject, String confirmationBody,
                        String adminId) {
        if (confirmationSubject == null || confirmationSubject.isBlank()) {
            throw new IllegalArgumentException("A confirmation email subject is required.");
        }
        if (confirmationBody == null || confirmationBody.isBlank()) {
            throw new IllegalArgumentException("A confirmation email body is required.");
        }
        Set<UUID> ids = recipientIds != null ? new LinkedHashSet<>(recipientIds) : new LinkedHashSet<>();
        if (enabled && ids.isEmpty()) {
            throw new IllegalArgumentException("At least one eligible recipient is required to enable submissions.");
        }
        validatePlaceholders(confirmationSubject);
        validatePlaceholders(confirmationBody);

        this.enabled = enabled;
        this.recipientIds = ids;
        this.confirmationSubject = confirmationSubject.strip();
        this.confirmationBody = confirmationBody.strip();
        this.updatedAt = Instant.now();
        this.updatedBy = adminId;
    }

    private static void validatePlaceholders(String text) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            if (!SUPPORTED_PLACEHOLDER.equals(placeholder)) {
                throw new IllegalArgumentException("Unsupported placeholder {" + placeholder + "} — only {name} is supported.");
            }
        }
    }

    public UUID getId()                      { return id; }
    public boolean isEnabled()                { return enabled; }
    public Set<UUID> getRecipientIds()        { return Set.copyOf(recipientIds); }
    public String getConfirmationSubject()    { return confirmationSubject; }
    public String getConfirmationBody()       { return confirmationBody; }
    public Instant getCreatedAt()             { return createdAt; }
    public Instant getUpdatedAt()             { return updatedAt; }
    public String getUpdatedBy()              { return updatedBy; }
}
