package com.skateboard.appconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "branding_asset")
public class BrandingAssetJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "object_key", nullable = false, columnDefinition = "text")
    private String objectKey;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private int version;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public BrandingAssetJpaEntity() {}

    public UUID getId()            { return id; }
    public String getName()        { return name; }
    public String getObjectKey()   { return objectKey; }
    public String getContentType() { return contentType; }
    public int getVersion()        { return version; }
    public String getUpdatedBy()   { return updatedBy; }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getUpdatedAt()  { return updatedAt; }

    public void setId(UUID id)                  { this.id = id; }
    public void setName(String v)                { this.name = v; }
    public void setObjectKey(String v)           { this.objectKey = v; }
    public void setContentType(String v)         { this.contentType = v; }
    public void setVersion(int v)                { this.version = v; }
    public void setUpdatedBy(String v)           { this.updatedBy = v; }
    public void setCreatedAt(Instant v)          { this.createdAt = v; }
    public void setUpdatedAt(Instant v)          { this.updatedAt = v; }
}
