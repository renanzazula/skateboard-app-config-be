package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.CampaignEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A recorded campaign analytics event. No relations — {@code campaign_id} /
 * {@code screen_id} are loose references so an event survives the campaign
 * being deleted (spec §18, implementation plan gap #3).
 */
@Entity
@Table(name = "campaign_event")
public class CampaignEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "screen_id")
    private UUID screenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private CampaignEventType eventType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(length = 50)
    private String platform;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "action_target", length = 2048)
    private String actionTarget;

    // Required by JPA/Hibernate to instantiate the entity via reflection.
    public CampaignEventJpaEntity() {}

    public UUID getId()                     { return id; }
    public UUID getCampaignId()             { return campaignId; }
    public UUID getScreenId()               { return screenId; }
    public CampaignEventType getEventType() { return eventType; }
    public Instant getOccurredAt()          { return occurredAt; }
    public String getPlatform()             { return platform; }
    public String getAppVersion()           { return appVersion; }
    public String getActionTarget()         { return actionTarget; }

    public void setId(UUID v)                     { this.id = v; }
    public void setCampaignId(UUID v)             { this.campaignId = v; }
    public void setScreenId(UUID v)               { this.screenId = v; }
    public void setEventType(CampaignEventType v) { this.eventType = v; }
    public void setOccurredAt(Instant v)          { this.occurredAt = v; }
    public void setPlatform(String v)             { this.platform = v; }
    public void setAppVersion(String v)           { this.appVersion = v; }
    public void setActionTarget(String v)         { this.actionTarget = v; }
}
