package com.skateboard.appconfig.domain.model;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * One screen in a {@link Campaign}'s ordered sequence. Not a standalone
 * aggregate: it is created, ordered, mutated and removed only through the
 * {@link Campaign} root, so its mutators are package-private. Its constructor
 * enforces every per-screen invariant from spec §7 — positive duration, a valid
 * close-button delay ({@code 0 <= closeAfterSeconds < durationSeconds}, default
 * 1s), overlay opacity in {@code [0, 1]}, and a CTA that is either {@code NONE}
 * or carries both a label and a validated target.
 */
public class CampaignScreen {

    /** Product default close-button delay when the button is enabled but no value is configured (spec §7). */
    static final int DEFAULT_CLOSE_AFTER_SECONDS = 1;

    /**
     * V1 internal CTA allow-list (implementation plan, gap #6). A plain constant
     * hand-synced with the Admin FE's target picker, not a dynamically fetched
     * route registry. A target is accepted if it equals one of these or sits
     * below it as a path segment (e.g. {@code /podcasts/123}).
     */
    private static final List<String> ALLOWED_INTERNAL_ROUTE_PREFIXES = List.of(
            "/home",
            "/podcasts",
            "/events",
            "/competitions",
            "/settings/about-us");

    private final UUID id;
    private int position;
    private CampaignMediaAsset background;

    private int durationSeconds;
    private CampaignLayoutType layoutType;
    private String backgroundColor;
    private String title;
    private String description;
    private CampaignTextAlignment textAlignment;
    private CampaignTextSize titleSize;
    private CampaignTextSize descriptionSize;
    private String textColor;
    private Double overlayOpacity;
    private boolean closeEnabled;
    private Integer closeAfterSeconds;
    private CampaignActionType actionType;
    private String actionLabel;
    private String actionTarget;

    private CampaignScreen(UUID id, int position, CampaignMediaAsset background) {
        this.id = id;
        this.position = position;
        this.background = background;
    }

    static CampaignScreen fromDraft(UUID id, int position, CampaignScreenDraft draft) {
        CampaignScreen screen = new CampaignScreen(id, position, null);
        screen.applyContent(draft);
        return screen;
    }

    public static CampaignScreen reconstitute(UUID id, int position, int durationSeconds,
                                              CampaignLayoutType layoutType, CampaignMediaAsset background,
                                              String backgroundColor, String title, String description,
                                              CampaignTextAlignment textAlignment, CampaignTextSize titleSize,
                                              CampaignTextSize descriptionSize, String textColor,
                                              Double overlayOpacity, boolean closeEnabled, Integer closeAfterSeconds,
                                              CampaignActionType actionType, String actionLabel, String actionTarget) {
        CampaignScreen screen = new CampaignScreen(id, position, background);
        screen.durationSeconds = durationSeconds;
        screen.layoutType = layoutType;
        screen.backgroundColor = backgroundColor;
        screen.title = title;
        screen.description = description;
        screen.textAlignment = textAlignment;
        screen.titleSize = titleSize;
        screen.descriptionSize = descriptionSize;
        screen.textColor = textColor;
        screen.overlayOpacity = overlayOpacity;
        screen.closeEnabled = closeEnabled;
        screen.closeAfterSeconds = closeAfterSeconds;
        screen.actionType = actionType;
        screen.actionLabel = actionLabel;
        screen.actionTarget = actionTarget;
        return screen;
    }

    // --- mutation, only reachable from the Campaign aggregate ---

    void applyContent(CampaignScreenDraft draft) {
        int duration = requireDuration(draft.durationSeconds());
        this.durationSeconds = duration;
        this.layoutType = draft.layoutType() != null ? draft.layoutType() : CampaignLayoutType.FULL_BACKGROUND;
        this.backgroundColor = blankToNull(draft.backgroundColor());
        this.title = blankToNull(draft.title());
        this.description = blankToNull(draft.description());
        this.textAlignment = draft.textAlignment() != null ? draft.textAlignment() : CampaignTextAlignment.CENTER;
        this.titleSize = draft.titleSize() != null ? draft.titleSize() : CampaignTextSize.LARGE;
        this.descriptionSize = draft.descriptionSize() != null ? draft.descriptionSize() : CampaignTextSize.MEDIUM;
        this.textColor = blankToNull(draft.textColor());
        this.overlayOpacity = validateOpacity(draft.overlayOpacity());
        this.closeEnabled = draft.closeEnabled();
        this.closeAfterSeconds = resolveCloseAfterSeconds(draft.closeEnabled(), draft.closeAfterSeconds(), duration);
        applyAction(draft.actionType(), draft.actionLabel(), draft.actionTarget());
    }

    void assignPosition(int position) {
        this.position = position;
    }

    void attachBackground(CampaignMediaAsset asset) {
        this.background = asset;
    }

    // --- validation helpers ---

    private static int requireDuration(Integer duration) {
        if (duration == null || duration < 1) {
            throw new IllegalArgumentException("Screen duration must be a positive number of seconds.");
        }
        return duration;
    }

    private static Double validateOpacity(Double opacity) {
        if (opacity == null) {
            return null;
        }
        if (opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException("overlayOpacity must be between 0 and 1.");
        }
        return opacity;
    }

    private static Integer resolveCloseAfterSeconds(boolean closeEnabled, Integer configured, int durationSeconds) {
        if (!closeEnabled) {
            return null;
        }
        int value = configured != null ? configured : Math.min(DEFAULT_CLOSE_AFTER_SECONDS, durationSeconds - 1);
        if (value < 0 || value >= durationSeconds) {
            throw new IllegalArgumentException(
                    "closeAfterSeconds must satisfy 0 <= closeAfterSeconds < durationSeconds.");
        }
        return value;
    }

    private void applyAction(CampaignActionType type, String label, String target) {
        CampaignActionType resolved = type != null ? type : CampaignActionType.NONE;
        if (resolved == CampaignActionType.NONE) {
            this.actionType = CampaignActionType.NONE;
            this.actionLabel = null;
            this.actionTarget = null;
            return;
        }
        String cleanLabel = blankToNull(label);
        String cleanTarget = blankToNull(target);
        if (cleanLabel == null) {
            throw new IllegalArgumentException("A CTA label is required when the action type is " + resolved + ".");
        }
        if (cleanTarget == null) {
            throw new IllegalArgumentException("A CTA target is required when the action type is " + resolved + ".");
        }
        if (resolved == CampaignActionType.INTERNAL) {
            validateInternalRoute(cleanTarget);
        } else {
            validateExternalUrl(cleanTarget);
        }
        this.actionType = resolved;
        this.actionLabel = cleanLabel;
        this.actionTarget = cleanTarget;
    }

    private static void validateInternalRoute(String target) {
        boolean allowed = ALLOWED_INTERNAL_ROUTE_PREFIXES.stream()
                .anyMatch(prefix -> target.equals(prefix) || target.startsWith(prefix + "/"));
        if (!allowed) {
            throw new IllegalArgumentException("Unsupported internal CTA route: " + target);
        }
    }

    private static void validateExternalUrl(String target) {
        String scheme;
        String host;
        try {
            URI uri = URI.create(target);
            scheme = uri.getScheme();
            host = uri.getHost();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("External CTA target must be a valid URL: " + target);
        }
        boolean httpScheme = scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        if (!httpScheme || host == null || host.isBlank()) {
            throw new IllegalArgumentException("External CTA target must be an absolute http(s) URL: " + target);
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }

    // --- accessors ---

    public UUID getId()                          { return id; }
    public int getPosition()                     { return position; }
    public CampaignMediaAsset getBackground()    { return background; }
    public int getDurationSeconds()              { return durationSeconds; }
    public CampaignLayoutType getLayoutType()    { return layoutType; }
    public String getBackgroundColor()           { return backgroundColor; }
    public String getTitle()                     { return title; }
    public String getDescription()               { return description; }
    public CampaignTextAlignment getTextAlignment() { return textAlignment; }
    public CampaignTextSize getTitleSize()       { return titleSize; }
    public CampaignTextSize getDescriptionSize() { return descriptionSize; }
    public String getTextColor()                 { return textColor; }
    public Double getOverlayOpacity()            { return overlayOpacity; }
    public boolean isCloseEnabled()              { return closeEnabled; }
    public Integer getCloseAfterSeconds()        { return closeAfterSeconds; }
    public CampaignActionType getActionType()    { return actionType; }
    public String getActionLabel()               { return actionLabel; }
    public String getActionTarget()              { return actionTarget; }
}
