package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * A reusable, named tenant-wide image (header, illustration, etc.), distinct
 * from the singleton login-background/app-logo fields on {@link AppConfig}
 * because there can be many of these, each independently managed.
 */
public class BrandingAsset {

    private final UUID id;
    private String name;
    private String objectKey;
    private String contentType;
    private int version;
    private final Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    private BrandingAsset(UUID id, String name, String objectKey, String contentType, int version,
                           Instant createdAt, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.name = name;
        this.objectKey = objectKey;
        this.contentType = contentType;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static BrandingAsset create(UUID id, String name, String objectKey, String contentType, String updatedBy) {
        Instant now = Instant.now();
        return new BrandingAsset(id, name, objectKey, contentType, 1, now, now, updatedBy);
    }

    public static BrandingAsset reconstitute(UUID id, String name, String objectKey, String contentType, int version,
                                              Instant createdAt, Instant updatedAt, String updatedBy) {
        return new BrandingAsset(id, name, objectKey, contentType, version, createdAt, updatedAt, updatedBy);
    }

    public void replace(String objectKey, String contentType, String updatedBy) {
        this.objectKey = objectKey;
        this.contentType = contentType;
        this.version++;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    public UUID getId()             { return id; }
    public String getName()         { return name; }
    public String getObjectKey()    { return objectKey; }
    public String getContentType()  { return contentType; }
    public int getVersion()         { return version; }
    public Instant getCreatedAt()   { return createdAt; }
    public Instant getUpdatedAt()   { return updatedAt; }
    public String getUpdatedBy()    { return updatedBy; }
}
