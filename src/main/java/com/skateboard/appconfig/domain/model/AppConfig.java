package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Singleton, app-wide branding configuration — there is exactly one row for
 * the whole application (no tenant scoping). The object-storage key is a
 * stable path, so a monotonically increasing version is the change signal
 * clients use to invalidate cached images.
 */
public class AppConfig {

    private final UUID id;
    private String loginBackgroundKey;
    private int loginBackgroundVersion;
    private Instant loginBackgroundUpdatedAt;
    private String appLogoKey;
    private int appLogoVersion;
    private Instant appLogoUpdatedAt;
    private String loginTitle;
    private String loginMessage;
    private String updatedBy;

    private AppConfig(UUID id, String loginBackgroundKey, int loginBackgroundVersion, Instant loginBackgroundUpdatedAt,
                       String appLogoKey, int appLogoVersion, Instant appLogoUpdatedAt,
                       String loginTitle, String loginMessage, String updatedBy) {
        this.id = id;
        this.loginBackgroundKey = loginBackgroundKey;
        this.loginBackgroundVersion = loginBackgroundVersion;
        this.loginBackgroundUpdatedAt = loginBackgroundUpdatedAt;
        this.appLogoKey = appLogoKey;
        this.appLogoVersion = appLogoVersion;
        this.appLogoUpdatedAt = appLogoUpdatedAt;
        this.loginTitle = loginTitle;
        this.loginMessage = loginMessage;
        this.updatedBy = updatedBy;
    }

    public static AppConfig createDefaults() {
        return new AppConfig(UUID.randomUUID(), null, 0, null, null, 0, null, null, null, null);
    }

    public static AppConfig reconstitute(UUID id, String loginBackgroundKey, int loginBackgroundVersion,
                                          Instant loginBackgroundUpdatedAt, String appLogoKey, int appLogoVersion,
                                          Instant appLogoUpdatedAt, String loginTitle, String loginMessage,
                                          String updatedBy) {
        return new AppConfig(id, loginBackgroundKey, loginBackgroundVersion, loginBackgroundUpdatedAt,
                appLogoKey, appLogoVersion, appLogoUpdatedAt, loginTitle, loginMessage, updatedBy);
    }

    public void updateLoginBackground(String key) {
        this.loginBackgroundKey = key;
        this.loginBackgroundVersion++;
        this.loginBackgroundUpdatedAt = Instant.now();
    }

    public void removeLoginBackground() {
        this.loginBackgroundKey = null;
        this.loginBackgroundVersion++;
        this.loginBackgroundUpdatedAt = Instant.now();
    }

    public void updateAppLogo(String key) {
        this.appLogoKey = key;
        this.appLogoVersion++;
        this.appLogoUpdatedAt = Instant.now();
    }

    public void removeAppLogo() {
        this.appLogoKey = null;
        this.appLogoVersion++;
        this.appLogoUpdatedAt = Instant.now();
    }

    public void updateLoginText(String title, String message) {
        this.loginTitle = title;
        this.loginMessage = message;
    }

    public void touch(String adminId) {
        this.updatedBy = adminId;
    }

    public UUID getId()                          { return id; }
    public String getLoginBackgroundKey()        { return loginBackgroundKey; }
    public int getLoginBackgroundVersion()       { return loginBackgroundVersion; }
    public Instant getLoginBackgroundUpdatedAt() { return loginBackgroundUpdatedAt; }
    public String getAppLogoKey()                { return appLogoKey; }
    public int getAppLogoVersion()               { return appLogoVersion; }
    public Instant getAppLogoUpdatedAt()         { return appLogoUpdatedAt; }
    public String getLoginTitle()                { return loginTitle; }
    public String getLoginMessage()              { return loginMessage; }
    public String getUpdatedBy()                 { return updatedBy; }
}
