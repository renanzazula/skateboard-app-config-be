package com.skateboard.appconfig.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig.Mode;

@Entity
@Table(name = "home_video_category_config")
public class HomeVideoCategoryConfigJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private Mode mode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "home_video_category_config_category", joinColumns = @JoinColumn(name = "config_id"))
    @Column(name = "category_id")
    private Set<String> enabledCategoryIds = new LinkedHashSet<>();

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public HomeVideoCategoryConfigJpaEntity() {}

    public UUID getId()                        { return id; }
    public Mode getMode()                      { return mode; }
    public Set<String> getEnabledCategoryIds() { return enabledCategoryIds; }
    public String getUpdatedBy()               { return updatedBy; }
    public Instant getCreatedAt()              { return createdAt; }
    public Instant getUpdatedAt()              { return updatedAt; }

    public void setId(UUID id)                              { this.id = id; }
    public void setMode(Mode mode)                          { this.mode = mode; }
    public void setEnabledCategoryIds(Set<String> v)        { this.enabledCategoryIds = v; }
    public void setUpdatedBy(String v)                      { this.updatedBy = v; }
    public void setCreatedAt(Instant v)                     { this.createdAt = v; }
    public void setUpdatedAt(Instant v)                     { this.updatedAt = v; }
}
