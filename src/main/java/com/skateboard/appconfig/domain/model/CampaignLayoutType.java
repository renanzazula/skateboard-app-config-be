package com.skateboard.appconfig.domain.model;

/**
 * The visual layout of a {@link CampaignScreen}. V1 only has
 * {@code FULL_BACKGROUND}; the enum exists so future layouts
 * ({@code IMAGE_TOP}, {@code CARD}, {@code VIDEO}) can be added without an
 * API-breaking change (spec §7).
 */
public enum CampaignLayoutType {
    FULL_BACKGROUND
}
