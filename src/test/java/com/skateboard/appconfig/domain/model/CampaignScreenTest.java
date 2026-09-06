package com.skateboard.appconfig.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampaignScreenTest {

    private static CampaignScreenDraft draft(Integer durationSeconds, boolean closeEnabled, Integer closeAfterSeconds,
                                             CampaignActionType actionType, String actionLabel, String actionTarget) {
        return new CampaignScreenDraft(durationSeconds, null, null, null, null, null, null, null, null, null,
                closeEnabled, closeAfterSeconds, actionType, actionLabel, actionTarget);
    }

    private static CampaignScreen screen(CampaignScreenDraft draft) {
        return CampaignScreen.fromDraft(UUID.randomUUID(), 1, draft);
    }

    @Test
    void appliesSensibleDefaultsForOmittedStyling() {
        CampaignScreen s = screen(draft(3, false, null, CampaignActionType.NONE, null, null));

        assertThat(s.getLayoutType()).isEqualTo(CampaignLayoutType.FULL_BACKGROUND);
        assertThat(s.getTextAlignment()).isEqualTo(CampaignTextAlignment.CENTER);
        assertThat(s.getTitleSize()).isEqualTo(CampaignTextSize.LARGE);
        assertThat(s.getDescriptionSize()).isEqualTo(CampaignTextSize.MEDIUM);
        assertThat(s.getActionType()).isEqualTo(CampaignActionType.NONE);
        assertThat(s.getCloseAfterSeconds()).isNull();
    }

    @Test
    void rejectsNonPositiveDuration() {
        assertThatThrownBy(() -> screen(draft(0, false, null, CampaignActionType.NONE, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> screen(draft(null, false, null, CampaignActionType.NONE, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsOverlayOpacityOutsideUnitInterval() {
        CampaignScreenDraft d = new CampaignScreenDraft(3, null, null, null, null, null, null, null, null, 1.5,
                false, null, CampaignActionType.NONE, null, null);
        assertThatThrownBy(() -> screen(d)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void defaultsCloseAfterSecondsToOneWhenEnabledAndOmitted() {
        CampaignScreen s = screen(draft(3, true, null, CampaignActionType.NONE, null, null));
        assertThat(s.getCloseAfterSeconds()).isEqualTo(1);
    }

    @Test
    void clampsDefaultCloseAfterSecondsForOneSecondScreens() {
        CampaignScreen s = screen(draft(1, true, null, CampaignActionType.NONE, null, null));
        assertThat(s.getCloseAfterSeconds()).isEqualTo(0);
    }

    @Test
    void rejectsCloseAfterSecondsNotLessThanDuration() {
        assertThatThrownBy(() -> screen(draft(3, true, 3, CampaignActionType.NONE, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeCloseAfterSeconds() {
        assertThatThrownBy(() -> screen(draft(3, true, -1, CampaignActionType.NONE, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ignoresCloseAfterSecondsWhenCloseDisabled() {
        CampaignScreen s = screen(draft(3, false, 2, CampaignActionType.NONE, null, null));
        assertThat(s.isCloseEnabled()).isFalse();
        assertThat(s.getCloseAfterSeconds()).isNull();
    }

    @Test
    void acceptsAnAllowlistedInternalCtaRoute() {
        CampaignScreen s = screen(draft(3, false, null, CampaignActionType.INTERNAL, "Discover", "/podcasts/123"));
        assertThat(s.getActionTarget()).isEqualTo("/podcasts/123");
        assertThat(s.getActionLabel()).isEqualTo("Discover");
    }

    @Test
    void rejectsAnUnsupportedInternalCtaRoute() {
        assertThatThrownBy(() -> screen(draft(3, false, null, CampaignActionType.INTERNAL, "Go", "/admin/secrets")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAnInternalCtaWithoutATarget() {
        assertThatThrownBy(() -> screen(draft(3, false, null, CampaignActionType.INTERNAL, "Go", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsACtaWithoutALabel() {
        assertThatThrownBy(() -> screen(draft(3, false, null, CampaignActionType.INTERNAL, null, "/home")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsAnAbsoluteHttpsExternalCta() {
        CampaignScreen s = screen(draft(3, false, null, CampaignActionType.EXTERNAL, "Visit", "https://sponsor.example.com/promo"));
        assertThat(s.getActionTarget()).isEqualTo("https://sponsor.example.com/promo");
    }

    @Test
    void rejectsANonUrlExternalCta() {
        assertThatThrownBy(() -> screen(draft(3, false, null, CampaignActionType.EXTERNAL, "Visit", "just some text")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> screen(draft(3, false, null, CampaignActionType.EXTERNAL, "Visit", "/relative/path")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void clearsLabelAndTargetWhenActionTypeIsNone() {
        CampaignScreen s = screen(draft(3, false, null, CampaignActionType.NONE, "leftover", "/home"));
        assertThat(s.getActionLabel()).isNull();
        assertThat(s.getActionTarget()).isNull();
    }
}
