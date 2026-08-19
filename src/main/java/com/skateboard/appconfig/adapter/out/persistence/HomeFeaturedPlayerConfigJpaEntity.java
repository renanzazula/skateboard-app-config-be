package com.skateboard.appconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import com.skateboard.appconfig.domain.model.FeaturedContentSource;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig.PlayerType;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig.Position;

@Entity
@Table(name = "home_featured_player_config")
public class HomeFeaturedPlayerConfigJpaEntity {

    @Id
    private UUID id;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_source")
    private FeaturedContentSource contentSource;

    @Column(name = "content_id")
    private String contentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "player_type", nullable = false)
    private PlayerType playerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    private Position position;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Nullable — reflects the domain's "last explicit admin change" (null
    // until update() runs), not a generic row-write audit; see
    // HomeFeaturedPlayerConfigPersistenceAdapter, mirrors
    // HomeVideoCategoryConfigJpaEntity.
    @Column(name = "updated_at")
    private Instant updatedAt;

    public HomeFeaturedPlayerConfigJpaEntity() {}

    public UUID getId()                            { return id; }
    public boolean isEnabled()                      { return enabled; }
    public FeaturedContentSource getContentSource() { return contentSource; }
    public String getContentId()                    { return contentId; }
    public PlayerType getPlayerType()                { return playerType; }
    public Position getPosition()                    { return position; }
    public String getUpdatedBy()                    { return updatedBy; }
    public Instant getCreatedAt()                   { return createdAt; }
    public Instant getUpdatedAt()                   { return updatedAt; }

    public void setId(UUID id)                                   { this.id = id; }
    public void setEnabled(boolean v)                             { this.enabled = v; }
    public void setContentSource(FeaturedContentSource v)         { this.contentSource = v; }
    public void setContentId(String v)                            { this.contentId = v; }
    public void setPlayerType(PlayerType v)                       { this.playerType = v; }
    public void setPosition(Position v)                           { this.position = v; }
    public void setUpdatedBy(String v)                            { this.updatedBy = v; }
    public void setCreatedAt(Instant v)                           { this.createdAt = v; }
    public void setUpdatedAt(Instant v)                           { this.updatedAt = v; }
}
