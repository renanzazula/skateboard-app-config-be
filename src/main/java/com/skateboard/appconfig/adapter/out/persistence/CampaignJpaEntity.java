package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import com.skateboard.appconfig.domain.model.CampaignStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The {@code campaign} aggregate root row. {@code status} only ever holds the
 * admin-driven value; the schedule-derived lifecycle is computed in the domain,
 * not stored. Screens are owned children ({@code cascade = ALL},
 * {@code orphanRemoval = true}) and always loaded with the campaign.
 */
@Entity
@Table(name = "campaign")
public class CampaignJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(nullable = false)
    private int priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignAudience audience;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false, length = 20)
    private CampaignFrequencyType frequencyType;

    @Column(name = "max_displays_per_day")
    private Integer maxDisplaysPerDay;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "published_by", length = 100)
    private String publishedBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<CampaignScreenJpaEntity> screens = new ArrayList<>();

    public CampaignJpaEntity() {}

    public UUID getId()                             { return id; }
    public String getName()                         { return name; }
    public String getDescription()                  { return description; }
    public CampaignStatus getStatus()               { return status; }
    public Instant getStartAt()                     { return startAt; }
    public Instant getEndAt()                       { return endAt; }
    public int getPriority()                        { return priority; }
    public CampaignAudience getAudience()           { return audience; }
    public CampaignFrequencyType getFrequencyType() { return frequencyType; }
    public Integer getMaxDisplaysPerDay()           { return maxDisplaysPerDay; }
    public String getCreatedBy()                    { return createdBy; }
    public Instant getCreatedAt()                   { return createdAt; }
    public String getUpdatedBy()                    { return updatedBy; }
    public Instant getUpdatedAt()                   { return updatedAt; }
    public String getPublishedBy()                  { return publishedBy; }
    public Instant getPublishedAt()                 { return publishedAt; }
    public List<CampaignScreenJpaEntity> getScreens() { return screens; }

    public void setId(UUID v)                            { this.id = v; }
    public void setName(String v)                        { this.name = v; }
    public void setDescription(String v)                 { this.description = v; }
    public void setStatus(CampaignStatus v)              { this.status = v; }
    public void setStartAt(Instant v)                    { this.startAt = v; }
    public void setEndAt(Instant v)                      { this.endAt = v; }
    public void setPriority(int v)                       { this.priority = v; }
    public void setAudience(CampaignAudience v)          { this.audience = v; }
    public void setFrequencyType(CampaignFrequencyType v) { this.frequencyType = v; }
    public void setMaxDisplaysPerDay(Integer v)          { this.maxDisplaysPerDay = v; }
    public void setCreatedBy(String v)                   { this.createdBy = v; }
    public void setCreatedAt(Instant v)                  { this.createdAt = v; }
    public void setUpdatedBy(String v)                   { this.updatedBy = v; }
    public void setUpdatedAt(Instant v)                  { this.updatedAt = v; }
    public void setPublishedBy(String v)                 { this.publishedBy = v; }
    public void setPublishedAt(Instant v)                { this.publishedAt = v; }
    public void setScreens(List<CampaignScreenJpaEntity> v) { this.screens = v; }
}
