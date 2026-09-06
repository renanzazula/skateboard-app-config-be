package com.skateboard.appconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * The uploaded background image of a {@link CampaignScreenJpaEntity}. Its own
 * row (not inline columns) so V2 can add resized variants without a schema
 * rewrite. Only the object key is stored.
 */
@Entity
@Table(name = "campaign_media_asset")
public class CampaignMediaAssetJpaEntity {

    @Id
    private UUID id;

    @Column(name = "storage_key", nullable = false, columnDefinition = "text")
    private String storageKey;

    @Column(nullable = false)
    private int version;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "focal_point_x")
    private Double focalPointX;

    @Column(name = "focal_point_y")
    private Double focalPointY;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CampaignMediaAssetJpaEntity() {}

    public UUID getId()            { return id; }
    public String getStorageKey()  { return storageKey; }
    public int getVersion()        { return version; }
    public String getMimeType()    { return mimeType; }
    public Integer getWidth()      { return width; }
    public Integer getHeight()     { return height; }
    public Long getSizeBytes()     { return sizeBytes; }
    public Double getFocalPointX() { return focalPointX; }
    public Double getFocalPointY() { return focalPointY; }
    public Instant getCreatedAt()  { return createdAt; }

    public void setId(UUID v)            { this.id = v; }
    public void setStorageKey(String v)  { this.storageKey = v; }
    public void setVersion(int v)        { this.version = v; }
    public void setMimeType(String v)    { this.mimeType = v; }
    public void setWidth(Integer v)      { this.width = v; }
    public void setHeight(Integer v)     { this.height = v; }
    public void setSizeBytes(Long v)     { this.sizeBytes = v; }
    public void setFocalPointX(Double v) { this.focalPointX = v; }
    public void setFocalPointY(Double v) { this.focalPointY = v; }
    public void setCreatedAt(Instant v)  { this.createdAt = v; }
}
