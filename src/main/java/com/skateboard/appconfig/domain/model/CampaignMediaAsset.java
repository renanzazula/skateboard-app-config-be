package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * The background image of a single {@link CampaignScreen}, kept as its own
 * value-ish entity (its own row) rather than inline columns so V2 can add
 * resized variants without a schema rewrite (see the V1 implementation plan,
 * gaps #2/#9). V1 stores exactly one uploaded object per screen — no resizing,
 * no CDN — and the client renders it with {@code cover} plus the admin-set
 * focal point.
 * <p>
 * Only the bare object key is stored; a presigned URL is generated fresh in the
 * REST layer on every read and never persisted. {@code version} is bumped on
 * every replacement so clients can treat each version as an immutable, safely
 * cacheable asset (spec §12).
 */
public class CampaignMediaAsset {

    private final UUID id;
    private final String storageKey;
    private final int version;
    private final String mimeType;
    private final Integer width;
    private final Integer height;
    private final Long sizeBytes;
    private final Double focalPointX;
    private final Double focalPointY;
    private final Instant createdAt;

    private CampaignMediaAsset(UUID id, String storageKey, int version, String mimeType, Integer width,
                              Integer height, Long sizeBytes, Double focalPointX, Double focalPointY,
                              Instant createdAt) {
        this.id = id;
        this.storageKey = storageKey;
        this.version = version;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.sizeBytes = sizeBytes;
        this.focalPointX = focalPointX;
        this.focalPointY = focalPointY;
        this.createdAt = createdAt;
    }

    public static CampaignMediaAsset create(UUID id, int version, String storageKey, String mimeType,
                                            Integer width, Integer height, Long sizeBytes,
                                            Double focalPointX, Double focalPointY) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("A media asset needs an object storage key.");
        }
        if (version < 1) {
            throw new IllegalArgumentException("A media asset version must be a positive number.");
        }
        return new CampaignMediaAsset(id, storageKey.strip(), version, mimeType, width, height, sizeBytes,
                validateFocal(focalPointX, "focalPointX"), validateFocal(focalPointY, "focalPointY"),
                Instant.now());
    }

    public static CampaignMediaAsset reconstitute(UUID id, String storageKey, int version, String mimeType,
                                                  Integer width, Integer height, Long sizeBytes,
                                                  Double focalPointX, Double focalPointY, Instant createdAt) {
        return new CampaignMediaAsset(id, storageKey, version, mimeType, width, height, sizeBytes,
                focalPointX, focalPointY, createdAt);
    }

    private static Double validateFocal(Double value, String field) {
        if (value == null) {
            return null;
        }
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(field + " must be between 0 and 1.");
        }
        return value;
    }

    public UUID getId()             { return id; }
    public String getStorageKey()   { return storageKey; }
    public int getVersion()         { return version; }
    public String getMimeType()     { return mimeType; }
    public Integer getWidth()       { return width; }
    public Integer getHeight()      { return height; }
    public Long getSizeBytes()      { return sizeBytes; }
    public Double getFocalPointX()  { return focalPointX; }
    public Double getFocalPointY()  { return focalPointY; }
    public Instant getCreatedAt()   { return createdAt; }
}
