package com.skateboard.appconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "about_us_page")
public class AboutPageJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String subtitle;

    @Column(nullable = false, length = 20)
    private String status;

    // Opaque JSON array of content blocks — see AboutPage / V8 migration.
    @Column(nullable = false, columnDefinition = "text")
    private String blocks;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public AboutPageJpaEntity() {}

    public UUID getId()           { return id; }
    public String getTitle()      { return title; }
    public String getSubtitle()   { return subtitle; }
    public String getStatus()     { return status; }
    public String getBlocks()     { return blocks; }
    public String getUpdatedBy()  { return updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(UUID id)           { this.id = id; }
    public void setTitle(String v)       { this.title = v; }
    public void setSubtitle(String v)    { this.subtitle = v; }
    public void setStatus(String v)      { this.status = v; }
    public void setBlocks(String v)      { this.blocks = v; }
    public void setUpdatedBy(String v)   { this.updatedBy = v; }
    public void setCreatedAt(Instant v)  { this.createdAt = v; }
    public void setUpdatedAt(Instant v)  { this.updatedAt = v; }
}
