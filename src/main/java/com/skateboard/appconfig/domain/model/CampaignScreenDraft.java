package com.skateboard.appconfig.domain.model;

/**
 * The admin-supplied content of a single {@link CampaignScreen} — everything
 * except its identity, its ordering position and its uploaded background image
 * (those are assigned by the {@link Campaign} aggregate and the separate image
 * upload). Used as the input to {@link Campaign#addScreen} and
 * {@link Campaign#updateScreen}; nullable fields fall back to sensible defaults
 * in {@link CampaignScreen}.
 */
public record CampaignScreenDraft(
        Integer durationSeconds,
        CampaignLayoutType layoutType,
        String backgroundColor,
        String title,
        String description,
        CampaignTextAlignment textAlignment,
        CampaignTextSize titleSize,
        CampaignTextSize descriptionSize,
        String textColor,
        Double overlayOpacity,
        boolean closeEnabled,
        Integer closeAfterSeconds,
        CampaignActionType actionType,
        String actionLabel,
        String actionTarget) {
}
