package com.skateboard.appconfig.domain.model;

import com.skateboard.appconfig.domain.exception.CampaignInvalidStateTransitionException;
import com.skateboard.appconfig.domain.exception.CampaignScreenLimitExceededException;
import com.skateboard.appconfig.domain.exception.CampaignScreenNotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate root for a Startup Campaign — an ordered sequence of 1–3 screens
 * shown between the native splash and the app's normal destination (spec §1).
 * All structural change goes through this root: it owns its {@link CampaignScreen}
 * children, keeps them contiguously positioned from 1, and enforces the V1
 * limits (≤3 screens, ≤10s combined duration) on every mutation as well as at
 * publish time.
 * <p>
 * {@link #getStatus()} only ever holds an admin-driven value; whether a
 * published campaign is currently {@code SCHEDULED}/{@code ACTIVE}/{@code EXPIRED}
 * is derived from the schedule at read time via
 * {@link #runtimeStatus(Instant)} / {@link #isEligibleAt(Instant, boolean)},
 * never stored or transitioned by a job (implementation plan, gap #4).
 */
public class Campaign {

    public static final int MAX_SCREENS = 3;
    public static final int MAX_TOTAL_DURATION_SECONDS = 10;

    /** The schedule-derived lifecycle value exposed by the API (spec §4). */
    public enum RuntimeStatus {
        DRAFT,
        SCHEDULED,
        ACTIVE,
        PAUSED,
        EXPIRED,
        ARCHIVED
    }

    private final UUID id;
    private String name;
    private String description;
    private CampaignStatus status;
    private Instant startAt;
    private Instant endAt;
    private int priority;
    private CampaignAudience audience;
    private CampaignFrequencyType frequencyType;
    private Integer maxDisplaysPerDay;
    private final List<CampaignScreen> screens;
    private final String createdBy;
    private final Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;
    private String publishedBy;
    private Instant publishedAt;

    private Campaign(UUID id, String name, String description, CampaignStatus status, Instant startAt, Instant endAt,
                     int priority, CampaignAudience audience, CampaignFrequencyType frequencyType,
                     Integer maxDisplaysPerDay, List<CampaignScreen> screens, String createdBy, Instant createdAt,
                     String updatedBy, Instant updatedAt, String publishedBy, Instant publishedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
        this.priority = priority;
        this.audience = audience;
        this.frequencyType = frequencyType;
        this.maxDisplaysPerDay = maxDisplaysPerDay;
        this.screens = screens;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
    }

    public static Campaign create(UUID id, String name, String description, Instant startAt, Instant endAt,
                                  int priority, CampaignAudience audience, CampaignFrequencyType frequencyType,
                                  Integer maxDisplaysPerDay, String createdBy) {
        Instant now = Instant.now();
        Campaign campaign = new Campaign(id, requireName(name), blankToNull(description), CampaignStatus.DRAFT,
                null, null, priority, requireAudience(audience), null, null,
                new ArrayList<>(), createdBy, now, createdBy, now, null, null);
        campaign.applySchedule(startAt, endAt);
        campaign.applyFrequency(frequencyType, maxDisplaysPerDay);
        return campaign;
    }

    public static Campaign reconstitute(UUID id, String name, String description, CampaignStatus status,
                                        Instant startAt, Instant endAt, int priority, CampaignAudience audience,
                                        CampaignFrequencyType frequencyType, Integer maxDisplaysPerDay,
                                        List<CampaignScreen> screens, String createdBy, Instant createdAt,
                                        String updatedBy, Instant updatedAt, String publishedBy, Instant publishedAt) {
        List<CampaignScreen> ordered = new ArrayList<>(screens);
        ordered.sort(Comparator.comparingInt(CampaignScreen::getPosition));
        return new Campaign(id, name, description, status, startAt, endAt, priority, audience, frequencyType,
                maxDisplaysPerDay, ordered, createdBy, createdAt, updatedBy, updatedAt, publishedBy, publishedAt);
    }

    // --- general configuration ---

    /**
     * Update the campaign's general information, audience, frequency and
     * schedule (spec §5). Allowed in every state except {@code ARCHIVED};
     * concurrent admin edits are last-write-wins by design (implementation
     * plan, gap #8).
     */
    public void updateDetails(String name, String description, Instant startAt, Instant endAt, int priority,
                              CampaignAudience audience, CampaignFrequencyType frequencyType,
                              Integer maxDisplaysPerDay, String actorId) {
        if (status == CampaignStatus.ARCHIVED) {
            throw new CampaignInvalidStateTransitionException("An archived campaign cannot be edited.");
        }
        this.name = requireName(name);
        this.description = blankToNull(description);
        this.priority = priority;
        this.audience = requireAudience(audience);
        applySchedule(startAt, endAt);
        applyFrequency(frequencyType, maxDisplaysPerDay);
        markUpdated(actorId);
    }

    // --- screens ---

    public CampaignScreen addScreen(CampaignScreenDraft draft, String actorId) {
        ensureNotArchived();
        if (screens.size() >= MAX_SCREENS) {
            throw CampaignScreenLimitExceededException.tooManyScreens(MAX_SCREENS);
        }
        CampaignScreen screen = CampaignScreen.fromDraft(UUID.randomUUID(), screens.size() + 1, draft);
        if (totalDurationSeconds() + screen.getDurationSeconds() > MAX_TOTAL_DURATION_SECONDS) {
            throw CampaignScreenLimitExceededException.totalDurationExceeded(MAX_TOTAL_DURATION_SECONDS);
        }
        screens.add(screen);
        markUpdated(actorId);
        return screen;
    }

    public CampaignScreen updateScreen(UUID screenId, CampaignScreenDraft draft, String actorId) {
        ensureNotArchived();
        CampaignScreen screen = findScreen(screenId);
        int othersDuration = totalDurationSeconds() - screen.getDurationSeconds();
        Integer newDuration = draft.durationSeconds();
        if (newDuration != null && othersDuration + newDuration > MAX_TOTAL_DURATION_SECONDS) {
            throw CampaignScreenLimitExceededException.totalDurationExceeded(MAX_TOTAL_DURATION_SECONDS);
        }
        screen.applyContent(draft);
        markUpdated(actorId);
        return screen;
    }

    public void removeScreen(UUID screenId, String actorId) {
        ensureNotArchived();
        CampaignScreen screen = findScreen(screenId);
        screens.remove(screen);
        renumber();
        markUpdated(actorId);
    }

    public void reorderScreens(List<UUID> orderedScreenIds, String actorId) {
        ensureNotArchived();
        Set<UUID> current = new HashSet<>();
        for (CampaignScreen screen : screens) {
            current.add(screen.getId());
        }
        if (orderedScreenIds.size() != screens.size() || !current.equals(new HashSet<>(orderedScreenIds))) {
            throw new IllegalArgumentException(
                    "The provided screen ids must be exactly the campaign's current screens.");
        }
        List<CampaignScreen> reordered = new ArrayList<>(screens.size());
        for (UUID screenId : orderedScreenIds) {
            reordered.add(findScreen(screenId));
        }
        screens.clear();
        screens.addAll(reordered);
        renumber();
        markUpdated(actorId);
    }

    /**
     * Replace a screen's background image. The new asset's {@code version} is
     * one greater than the previous one (or 1 for the first upload), so every
     * version is an immutable, safely cacheable object (spec §12).
     */
    public CampaignScreen replaceScreenImage(UUID screenId, String storageKey, String mimeType, Integer width,
                                             Integer height, Long sizeBytes, Double focalPointX, Double focalPointY,
                                             String actorId) {
        ensureNotArchived();
        CampaignScreen screen = findScreen(screenId);
        int nextVersion = screen.getBackground() != null ? screen.getBackground().getVersion() + 1 : 1;
        CampaignMediaAsset asset = CampaignMediaAsset.create(UUID.randomUUID(), nextVersion, storageKey, mimeType,
                width, height, sizeBytes, focalPointX, focalPointY);
        screen.attachBackground(asset);
        markUpdated(actorId);
        return screen;
    }

    // --- lifecycle ---

    /**
     * Publish the campaign (immediately or, if {@code startAt} is in the future,
     * on schedule). Re-checks every aggregate invariant from spec §13 first:
     * at least one screen, the screen/duration limits, {@code endAt > startAt},
     * and that every screen has a usable background (an uploaded image or a
     * fallback colour). Allowed from {@code DRAFT} or {@code PAUSED}.
     */
    public void publish(String actorId) {
        if (status != CampaignStatus.DRAFT && status != CampaignStatus.PAUSED) {
            throw new CampaignInvalidStateTransitionException(
                    "Only a draft or paused campaign can be published.");
        }
        if (screens.isEmpty()) {
            throw new CampaignInvalidStateTransitionException(
                    "A campaign needs at least one screen before it can be published.");
        }
        if (screens.size() > MAX_SCREENS) {
            throw new CampaignInvalidStateTransitionException(
                    "A campaign cannot have more than " + MAX_SCREENS + " screens.");
        }
        if (totalDurationSeconds() > MAX_TOTAL_DURATION_SECONDS) {
            throw new CampaignInvalidStateTransitionException(
                    "The combined screen duration cannot exceed " + MAX_TOTAL_DURATION_SECONDS + " seconds.");
        }
        if (!endAt.isAfter(startAt)) {
            throw new CampaignInvalidStateTransitionException(
                    "The campaign end time must be after its start time.");
        }
        for (CampaignScreen screen : screens) {
            boolean hasColour = screen.getBackgroundColor() != null && !screen.getBackgroundColor().isBlank();
            if (screen.getBackground() == null && !hasColour) {
                throw new CampaignInvalidStateTransitionException(
                        "Screen " + screen.getPosition()
                                + " needs a background image or a fallback colour before publishing.");
            }
        }
        this.status = CampaignStatus.PUBLISHED;
        this.publishedBy = actorId;
        this.publishedAt = Instant.now();
        markUpdated(actorId);
    }

    public void pause(String actorId) {
        if (status != CampaignStatus.PUBLISHED) {
            throw new CampaignInvalidStateTransitionException("Only a published campaign can be paused.");
        }
        this.status = CampaignStatus.PAUSED;
        markUpdated(actorId);
    }

    public void archive(String actorId) {
        if (status == CampaignStatus.ARCHIVED) {
            throw new CampaignInvalidStateTransitionException("The campaign is already archived.");
        }
        this.status = CampaignStatus.ARCHIVED;
        markUpdated(actorId);
    }

    /** A campaign may be hard-deleted only while it is still a draft (implementation plan, gap #5). */
    public boolean isDeletable() {
        return status == CampaignStatus.DRAFT;
    }

    // --- runtime / read-time projections ---

    /**
     * Whether this campaign should be returned to the runtime app at
     * {@code now} for a session with the given auth state. {@code ANONYMOUS}
     * campaigns are shown only to signed-out sessions and {@code AUTHENTICATED}
     * only to signed-in ones (spec §5.2/§11).
     */
    public boolean isEligibleAt(Instant now, boolean authenticated) {
        if (status != CampaignStatus.PUBLISHED) {
            return false;
        }
        if (now.isBefore(startAt) || !now.isBefore(endAt)) {
            return false;
        }
        return switch (audience) {
            case ALL -> true;
            case AUTHENTICATED -> authenticated;
            case ANONYMOUS -> !authenticated;
        };
    }

    public RuntimeStatus runtimeStatus(Instant now) {
        return switch (status) {
            case DRAFT -> RuntimeStatus.DRAFT;
            case PAUSED -> RuntimeStatus.PAUSED;
            case ARCHIVED -> RuntimeStatus.ARCHIVED;
            case PUBLISHED -> {
                if (now.isBefore(startAt)) {
                    yield RuntimeStatus.SCHEDULED;
                }
                yield now.isBefore(endAt) ? RuntimeStatus.ACTIVE : RuntimeStatus.EXPIRED;
            }
        };
    }

    // --- internal helpers ---

    private int totalDurationSeconds() {
        return screens.stream().mapToInt(CampaignScreen::getDurationSeconds).sum();
    }

    private CampaignScreen findScreen(UUID screenId) {
        return screens.stream()
                .filter(screen -> screen.getId().equals(screenId))
                .findFirst()
                .orElseThrow(() -> new CampaignScreenNotFoundException(id, screenId));
    }

    private void renumber() {
        int position = 1;
        for (CampaignScreen screen : screens) {
            screen.assignPosition(position++);
        }
    }

    private void ensureNotArchived() {
        if (status == CampaignStatus.ARCHIVED) {
            throw new CampaignInvalidStateTransitionException("An archived campaign cannot be modified.");
        }
    }

    private void applySchedule(Instant startAt, Instant endAt) {
        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException("A campaign start and end time are required.");
        }
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("The campaign end time must be after its start time.");
        }
        this.startAt = startAt;
        this.endAt = endAt;
    }

    private void applyFrequency(CampaignFrequencyType frequencyType, Integer maxDisplaysPerDay) {
        if (frequencyType == null) {
            throw new IllegalArgumentException("A display frequency is required.");
        }
        if (frequencyType == CampaignFrequencyType.MAX_PER_DAY) {
            if (maxDisplaysPerDay == null || maxDisplaysPerDay < 1) {
                throw new IllegalArgumentException(
                        "maxDisplaysPerDay must be a positive number when the frequency is MAX_PER_DAY.");
            }
            this.maxDisplaysPerDay = maxDisplaysPerDay;
        } else {
            this.maxDisplaysPerDay = null;
        }
        this.frequencyType = frequencyType;
    }

    private void markUpdated(String actorId) {
        this.updatedBy = actorId;
        this.updatedAt = Instant.now();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A campaign name is required.");
        }
        return name.strip();
    }

    private static CampaignAudience requireAudience(CampaignAudience audience) {
        if (audience == null) {
            throw new IllegalArgumentException("A campaign audience is required.");
        }
        return audience;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }

    // --- accessors ---

    public UUID getId()                              { return id; }
    public String getName()                          { return name; }
    public String getDescription()                   { return description; }
    public CampaignStatus getStatus()                { return status; }
    public Instant getStartAt()                      { return startAt; }
    public Instant getEndAt()                        { return endAt; }
    public int getPriority()                         { return priority; }
    public CampaignAudience getAudience()            { return audience; }
    public CampaignFrequencyType getFrequencyType()  { return frequencyType; }
    public Integer getMaxDisplaysPerDay()            { return maxDisplaysPerDay; }
    public List<CampaignScreen> getScreens()         { return Collections.unmodifiableList(screens); }
    public String getCreatedBy()                     { return createdBy; }
    public Instant getCreatedAt()                    { return createdAt; }
    public String getUpdatedBy()                     { return updatedBy; }
    public Instant getUpdatedAt()                    { return updatedAt; }
    public String getPublishedBy()                   { return publishedBy; }
    public Instant getPublishedAt()                  { return publishedAt; }
}
