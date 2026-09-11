package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Singleton, app-wide default configuration of the Home dashboard's Featured
 * Player — there is exactly one row for the whole application (no tenant
 * scoping), mirroring {@link HomeVideoCategoryConfig}. Stores only a
 * {@code contentSource}/{@code contentId} reference: this service does not
 * own the referenced content (that lives in the source service, e.g.
 * skateboard-podcast-be), so existence/status validation of the id is a
 * consumer concern, not enforced here.
 */
public class HomeFeaturedPlayerConfig {

    public enum PlayerType { MINI }
    public enum Position { TOP, BOTTOM }

    /**
     * Which distribution platform to play when the featured content has more
     * than one available (e.g. a podcast episode with both a Spotify and a
     * YouTube link). {@code null} means "let the resolver decide" — today
     * that means skateboard-ui-backend's PodcastFeaturedContentResolver
     * preferring Spotify, falling back to YouTube. An explicit value here
     * asks the resolver to prefer that platform instead, still falling back
     * to whichever is actually available if the preferred one isn't.
     */
    public enum PreferredPlatform { SPOTIFY, YOUTUBE }

    /**
     * How the featured content is chosen. {@code MANUAL} (the default, and
     * the only mode that existed before this field) keeps {@code contentId}
     * as an explicit admin pick that YouTube sync / any other automation must
     * never touch. {@code AUTO} means this service stores no concrete
     * selection at all — {@code contentId} is always {@code null} under
     * AUTO — and the consumer (skateboard-ui-backend) resolves the latest
     * eligible episode itself at read time. This service never talks to the
     * content-owning services, so it cannot and does not do that resolution.
     */
    public enum SelectionMode { MANUAL, AUTO }

    private final UUID id;
    private boolean enabled;
    private FeaturedContentSource contentSource;
    private String contentId;
    private PlayerType playerType;
    private Position position;
    private PreferredPlatform preferredPlatform;
    private SelectionMode selectionMode;
    private Instant updatedAt;
    private String updatedBy;

    private HomeFeaturedPlayerConfig(UUID id, boolean enabled, FeaturedContentSource contentSource, String contentId,
                                      PlayerType playerType, Position position, PreferredPlatform preferredPlatform,
                                      SelectionMode selectionMode, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.enabled = enabled;
        this.contentSource = contentSource;
        this.contentId = contentId;
        this.playerType = playerType;
        this.position = position;
        this.preferredPlatform = preferredPlatform;
        this.selectionMode = selectionMode;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    /**
     * Defaults for a brand-new singleton row only (first-ever GET before any
     * admin has configured anything). Position TOP, preferredPlatform
     * YOUTUBE and selectionMode MANUAL are the defaults for a *new*
     * configuration; existing rows keep whatever they already had, via
     * {@link #reconstitute} and the V9 migration's backfill to MANUAL.
     */
    public static HomeFeaturedPlayerConfig createDefaults() {
        return new HomeFeaturedPlayerConfig(UUID.randomUUID(), false, null, null,
                PlayerType.MINI, Position.TOP, PreferredPlatform.YOUTUBE, SelectionMode.MANUAL, null, null);
    }

    public static HomeFeaturedPlayerConfig reconstitute(UUID id, boolean enabled, FeaturedContentSource contentSource,
                                                          String contentId, PlayerType playerType, Position position,
                                                          PreferredPlatform preferredPlatform, SelectionMode selectionMode,
                                                          Instant updatedAt, String updatedBy) {
        return new HomeFeaturedPlayerConfig(id, enabled, contentSource, contentId, playerType, position,
                preferredPlatform, selectionMode != null ? selectionMode : SelectionMode.MANUAL, updatedAt, updatedBy);
    }

    /**
     * Disabling does not clear the previously selected content — an admin
     * toggling the player off and back on keeps their selection. Enabling
     * requires a content source in both modes (so a consumer knows which
     * resolver family to use), plus a concrete contentId for MANUAL only.
     *
     * <p>AUTO never persists a real contentId — the actual episode is
     * resolved by the consumer at read time — so switching to (or staying
     * in) AUTO always clears it here, even if the caller passed one in.
     */
    public void update(boolean enabled, FeaturedContentSource contentSource, String contentId,
                        PlayerType playerType, Position position, PreferredPlatform preferredPlatform,
                        SelectionMode selectionMode) {
        SelectionMode mode = selectionMode != null ? selectionMode : SelectionMode.MANUAL;
        if (enabled && contentSource == null) {
            throw new IllegalArgumentException("contentSource is required when enabled is true");
        }
        if (enabled && mode == SelectionMode.MANUAL && (contentId == null || contentId.isBlank())) {
            throw new IllegalArgumentException("contentId is required when enabled is true and selectionMode is MANUAL");
        }
        this.enabled = enabled;
        this.contentSource = contentSource;
        this.contentId = mode == SelectionMode.AUTO ? null : contentId;
        this.playerType = playerType != null ? playerType : PlayerType.MINI;
        this.position = position != null ? position : Position.BOTTOM;
        this.preferredPlatform = preferredPlatform;
        this.selectionMode = mode;
        this.updatedAt = Instant.now();
    }

    public void touch(String adminId) {
        this.updatedBy = adminId;
    }

    public UUID getId()                             { return id; }
    public boolean isEnabled()                       { return enabled; }
    public FeaturedContentSource getContentSource()  { return contentSource; }
    public String getContentId()                     { return contentId; }
    public PlayerType getPlayerType()                { return playerType; }
    public Position getPosition()                    { return position; }
    public PreferredPlatform getPreferredPlatform()  { return preferredPlatform; }
    public SelectionMode getSelectionMode()          { return selectionMode; }
    public Instant getUpdatedAt()                    { return updatedAt; }
    public String getUpdatedBy()                     { return updatedBy; }
}
