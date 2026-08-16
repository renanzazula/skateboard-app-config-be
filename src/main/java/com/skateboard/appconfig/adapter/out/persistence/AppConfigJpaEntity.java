package com.skateboard.appconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_config")
public class AppConfigJpaEntity {

    @Id
    private UUID id;

    @Column(name = "login_background_key")
    private String loginBackgroundKey;

    @Column(name = "login_background_version", nullable = false)
    private int loginBackgroundVersion;

    @Column(name = "login_background_updated_at")
    private Instant loginBackgroundUpdatedAt;

    @Column(name = "app_logo_key")
    private String appLogoKey;

    @Column(name = "app_logo_version", nullable = false)
    private int appLogoVersion;

    @Column(name = "app_logo_updated_at")
    private Instant appLogoUpdatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AppConfigJpaEntity() {}

    public UUID getId()                          { return id; }
    public String getLoginBackgroundKey()        { return loginBackgroundKey; }
    public int getLoginBackgroundVersion()       { return loginBackgroundVersion; }
    public Instant getLoginBackgroundUpdatedAt() { return loginBackgroundUpdatedAt; }
    public String getAppLogoKey()                { return appLogoKey; }
    public int getAppLogoVersion()               { return appLogoVersion; }
    public Instant getAppLogoUpdatedAt()         { return appLogoUpdatedAt; }
    public String getUpdatedBy()                 { return updatedBy; }
    public Instant getCreatedAt()                { return createdAt; }
    public Instant getUpdatedAt()                { return updatedAt; }

    public void setId(UUID id)                                    { this.id = id; }
    public void setLoginBackgroundKey(String v)                   { this.loginBackgroundKey = v; }
    public void setLoginBackgroundVersion(int v)                  { this.loginBackgroundVersion = v; }
    public void setLoginBackgroundUpdatedAt(Instant v)            { this.loginBackgroundUpdatedAt = v; }
    public void setAppLogoKey(String v)                           { this.appLogoKey = v; }
    public void setAppLogoVersion(int v)                          { this.appLogoVersion = v; }
    public void setAppLogoUpdatedAt(Instant v)                    { this.appLogoUpdatedAt = v; }
    public void setUpdatedBy(String v)                            { this.updatedBy = v; }
    public void setCreatedAt(Instant v)                           { this.createdAt = v; }
    public void setUpdatedAt(Instant v)                           { this.updatedAt = v; }
}
