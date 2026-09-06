package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.CampaignActionType;
import com.skateboard.appconfig.domain.model.CampaignLayoutType;
import com.skateboard.appconfig.domain.model.CampaignTextAlignment;
import com.skateboard.appconfig.domain.model.CampaignTextSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * One screen of a {@link CampaignJpaEntity}. Child of the campaign aggregate —
 * only ever loaded and written through {@link CampaignPersistenceAdapter}, never
 * on its own. Its background image is a separate row it fully owns
 * ({@code cascade = ALL}, {@code orphanRemoval = true}).
 */
@Entity
@Table(name = "campaign_screen")
public class CampaignScreenJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignJpaEntity campaign;

    @Column(nullable = false)
    private int position;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "layout_type", nullable = false, length = 20)
    private CampaignLayoutType layoutType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "background_asset_id")
    private CampaignMediaAssetJpaEntity background;

    @Column(name = "background_color", length = 30)
    private String backgroundColor;

    @Column(length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "text_alignment", nullable = false, length = 10)
    private CampaignTextAlignment textAlignment;

    @Enumerated(EnumType.STRING)
    @Column(name = "title_size", nullable = false, length = 20)
    private CampaignTextSize titleSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "description_size", nullable = false, length = 20)
    private CampaignTextSize descriptionSize;

    @Column(name = "text_color", length = 30)
    private String textColor;

    @Column(name = "overlay_opacity")
    private Double overlayOpacity;

    @Column(name = "close_enabled", nullable = false)
    private boolean closeEnabled;

    @Column(name = "close_after_seconds")
    private Integer closeAfterSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private CampaignActionType actionType;

    @Column(name = "action_label", length = 150)
    private String actionLabel;

    @Column(name = "action_target", length = 2048)
    private String actionTarget;

    public CampaignScreenJpaEntity() {}

    public UUID getId()                             { return id; }
    public CampaignJpaEntity getCampaign()          { return campaign; }
    public int getPosition()                        { return position; }
    public int getDurationSeconds()                 { return durationSeconds; }
    public CampaignLayoutType getLayoutType()       { return layoutType; }
    public CampaignMediaAssetJpaEntity getBackground() { return background; }
    public String getBackgroundColor()              { return backgroundColor; }
    public String getTitle()                        { return title; }
    public String getDescription()                  { return description; }
    public CampaignTextAlignment getTextAlignment() { return textAlignment; }
    public CampaignTextSize getTitleSize()          { return titleSize; }
    public CampaignTextSize getDescriptionSize()    { return descriptionSize; }
    public String getTextColor()                    { return textColor; }
    public Double getOverlayOpacity()               { return overlayOpacity; }
    public boolean isCloseEnabled()                 { return closeEnabled; }
    public Integer getCloseAfterSeconds()           { return closeAfterSeconds; }
    public CampaignActionType getActionType()       { return actionType; }
    public String getActionLabel()                  { return actionLabel; }
    public String getActionTarget()                 { return actionTarget; }

    public void setId(UUID v)                              { this.id = v; }
    public void setCampaign(CampaignJpaEntity v)           { this.campaign = v; }
    public void setPosition(int v)                         { this.position = v; }
    public void setDurationSeconds(int v)                  { this.durationSeconds = v; }
    public void setLayoutType(CampaignLayoutType v)        { this.layoutType = v; }
    public void setBackground(CampaignMediaAssetJpaEntity v) { this.background = v; }
    public void setBackgroundColor(String v)               { this.backgroundColor = v; }
    public void setTitle(String v)                         { this.title = v; }
    public void setDescription(String v)                   { this.description = v; }
    public void setTextAlignment(CampaignTextAlignment v)  { this.textAlignment = v; }
    public void setTitleSize(CampaignTextSize v)           { this.titleSize = v; }
    public void setDescriptionSize(CampaignTextSize v)     { this.descriptionSize = v; }
    public void setTextColor(String v)                     { this.textColor = v; }
    public void setOverlayOpacity(Double v)                { this.overlayOpacity = v; }
    public void setCloseEnabled(boolean v)                 { this.closeEnabled = v; }
    public void setCloseAfterSeconds(Integer v)            { this.closeAfterSeconds = v; }
    public void setActionType(CampaignActionType v)        { this.actionType = v; }
    public void setActionLabel(String v)                   { this.actionLabel = v; }
    public void setActionTarget(String v)                  { this.actionTarget = v; }
}
