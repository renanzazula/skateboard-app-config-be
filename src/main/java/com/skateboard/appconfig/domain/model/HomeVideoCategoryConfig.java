package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Singleton, app-wide configuration of which video categories are eligible
 * for the mobile Home dashboard — there is exactly one row for the whole
 * application (no tenant scoping), mirroring {@link AppConfig}. Category IDs
 * are stored as opaque strings: this service does not own category data
 * (that lives in skateboard-podcast-be), so existence validation of IDs is a
 * consumer concern, not enforced here.
 */
public class HomeVideoCategoryConfig {

    public enum Mode { ALL, SELECTED }

    private final UUID id;
    private Mode mode;
    private Set<String> enabledCategoryIds;
    private Instant updatedAt;
    private String updatedBy;

    private HomeVideoCategoryConfig(UUID id, Mode mode, Set<String> enabledCategoryIds, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.mode = mode;
        this.enabledCategoryIds = new LinkedHashSet<>(enabledCategoryIds);
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static HomeVideoCategoryConfig createDefaults() {
        return new HomeVideoCategoryConfig(UUID.randomUUID(), Mode.ALL, Set.of(), null, null);
    }

    public static HomeVideoCategoryConfig reconstitute(UUID id, Mode mode, Set<String> enabledCategoryIds,
                                                         Instant updatedAt, String updatedBy) {
        return new HomeVideoCategoryConfig(id, mode, enabledCategoryIds, updatedAt, updatedBy);
    }

    public void updateCategories(Mode mode, Set<String> enabledCategoryIds) {
        if (mode == Mode.SELECTED && (enabledCategoryIds == null || enabledCategoryIds.isEmpty())) {
            throw new IllegalArgumentException("At least one category must be selected when mode is SELECTED");
        }
        this.mode = mode;
        this.enabledCategoryIds = mode == Mode.ALL ? new LinkedHashSet<>() : new LinkedHashSet<>(enabledCategoryIds);
        this.updatedAt = Instant.now();
    }

    public void touch(String adminId) {
        this.updatedBy = adminId;
    }

    public UUID getId()                      { return id; }
    public Mode getMode()                    { return mode; }
    public Set<String> getEnabledCategoryIds() { return Collections.unmodifiableSet(enabledCategoryIds); }
    public Instant getUpdatedAt()            { return updatedAt; }
    public String getUpdatedBy()             { return updatedBy; }
}
