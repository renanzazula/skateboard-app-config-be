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

    private final UUID id;
    private boolean enabled;
    private FeaturedContentSource contentSource;
    private String contentId;
    private PlayerType playerType;
    private Position position;
    private PreferredPlatform preferredPlatform;
    private Instant updatedAt;
    private String updatedBy;

    private HomeFeaturedPlayerConfig(UUID id, boolean enabled, FeaturedContentSource contentSource, String contentId,
                                      PlayerType playerType, Position position, PreferredPlatform preferredPlatform,
                                      Instant updatedAt, String updatedBy) {
        this.id = id;
        this.enabled = enabled;
        this.contentSource = contentSource;
        this.contentId = contentId;
        this.playerType = playerType;
        this.position = position;
        this.preferredPlatform = preferredPlatform;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static HomeFeaturedPlayerConfig createDefaults() {
        return new HomeFeaturedPlayerConfig(UUID.randomUUID(), false, null, null,
                PlayerType.MINI, Position.BOTTOM, null, null, null);
    }

    public static HomeFeaturedPlayerConfig reconstitute(UUID id, boolean enabled, FeaturedContentSource contentSource,
                                                          String contentId, PlayerType playerType, Position position,
                                                          PreferredPlatform preferredPlatform,
                                                          Instant updatedAt, String updatedBy) {
        return new HomeFeaturedPlayerConfig(id, enabled, contentSource, contentId, playerType, position,
                preferredPlatform, updatedAt, updatedBy);
    }

    /**
     * Disabling does not clear the previously selected content — an admin
     * toggling the player off and back on keeps their selection. Enabling
     * requires a content reference.
     */
    public void update(boolean enabled, FeaturedContentSource contentSource, String contentId,
                        PlayerType playerType, Position position, PreferredPlatform preferredPlatform) {
        if (enabled && (contentSource == null || contentId == null || contentId.isBlank())) {
            throw new IllegalArgumentException("contentSource and contentId are required when enabled is true");
        }
        this.enabled = enabled;
        this.contentSource = contentSource;
        this.contentId = contentId;
        this.playerType = playerType != null ? playerType : PlayerType.MINI;
        this.position = position != null ? position : Position.BOTTOM;
        this.preferredPlatform = preferredPlatform;
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
    public Instant getUpdatedAt()                    { return updatedAt; }
    public String getUpdatedBy()                     { return updatedBy; }
}
