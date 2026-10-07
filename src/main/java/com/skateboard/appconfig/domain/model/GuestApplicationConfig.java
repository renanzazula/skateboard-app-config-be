package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

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
 *
 * <p>The confirmation/admin-notification email copy used to live here as a
 * flat confirmationSubject/confirmationBody pair; it now lives in
 * {@link EmailTemplate} rows (GUEST_APPLICATION_RECEIVED /
 * GUEST_APPLICATION_ADMIN_NOTIFICATION), which supports per-language copy
 * instead of English only. This model keeps only delivery settings.
 */
public class GuestApplicationConfig {

    private final UUID id;
    private boolean enabled;
    private Set<UUID> recipientIds;
    private final Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    private GuestApplicationConfig(UUID id, boolean enabled, Set<UUID> recipientIds, Instant createdAt,
                                    Instant updatedAt, String updatedBy) {
        this.id = id;
        this.enabled = enabled;
        this.recipientIds = recipientIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    /** Defaults for a brand-new singleton row only (first-ever GET before any admin has configured anything). */
    public static GuestApplicationConfig createDefaults() {
        return new GuestApplicationConfig(UUID.randomUUID(), false, new LinkedHashSet<>(), Instant.now(), null, null);
    }

    public static GuestApplicationConfig reconstitute(UUID id, boolean enabled, Set<UUID> recipientIds,
                                                        Instant createdAt, Instant updatedAt, String updatedBy) {
        return new GuestApplicationConfig(id, enabled, new LinkedHashSet<>(recipientIds), createdAt, updatedAt,
                updatedBy);
    }

    /** Per the README's acceptance criteria: enabling requires at least one recipient. */
    public void update(boolean enabled, Set<UUID> recipientIds, String adminId) {
        Set<UUID> ids = recipientIds != null ? new LinkedHashSet<>(recipientIds) : new LinkedHashSet<>();
        if (enabled && ids.isEmpty()) {
            throw new IllegalArgumentException("At least one eligible recipient is required to enable submissions.");
        }

        this.enabled = enabled;
        this.recipientIds = ids;
        this.updatedAt = Instant.now();
        this.updatedBy = adminId;
    }

    public UUID getId()                      { return id; }
    public boolean isEnabled()                { return enabled; }
    public Set<UUID> getRecipientIds()        { return Set.copyOf(recipientIds); }
    public Instant getCreatedAt()             { return createdAt; }
    public Instant getUpdatedAt()             { return updatedAt; }
    public String getUpdatedBy()              { return updatedBy; }
}
