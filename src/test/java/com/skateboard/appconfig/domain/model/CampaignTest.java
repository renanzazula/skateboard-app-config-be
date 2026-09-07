package com.skateboard.appconfig.domain.model;

import com.skateboard.appconfig.domain.exception.CampaignInvalidStateTransitionException;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.exception.CampaignScreenLimitExceededException;
import com.skateboard.appconfig.domain.exception.CampaignScreenNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampaignTest {

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-02-01T00:00:00Z");

    private static Campaign draftCampaign() {
        return Campaign.create(UUID.randomUUID(), "Spring Sponsors", "internal note", START, END,
                10, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, "admin-1");
    }

    private static CampaignScreenDraft screenDraft(int durationSeconds) {
        return new CampaignScreenDraft(durationSeconds, null, "#101010", "Title", null, null, null, null, null, null,
                false, null, CampaignActionType.NONE, null, null);
    }

    // --- creation ---

    @Test
    void createStartsAsAnEmptyDraft() {
        Campaign campaign = draftCampaign();

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.DRAFT);
        assertThat(campaign.getScreens()).isEmpty();
        assertThat(campaign.getCreatedBy()).isEqualTo("admin-1");
        assertThat(campaign.getCreatedAt()).isNotNull();
        assertThat(campaign.getPublishedAt()).isNull();
        assertThat(campaign.isDeletable()).isTrue();
    }

    @Test
    void createRejectsABlankName() {
        assertThatThrownBy(() -> Campaign.create(UUID.randomUUID(), "  ", null, START, END, 1,
                CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRejectsAnEndBeforeStart() {
        assertThatThrownBy(() -> Campaign.create(UUID.randomUUID(), "x", null, END, START, 1,
                CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRequiresMaxDisplaysPerDayWhenFrequencyIsMaxPerDay() {
        assertThatThrownBy(() -> Campaign.create(UUID.randomUUID(), "x", null, START, END, 1,
                CampaignAudience.ALL, CampaignFrequencyType.MAX_PER_DAY, null, "admin-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createDiscardsMaxDisplaysPerDayForOtherFrequencies() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "x", null, START, END, 1,
                CampaignAudience.ALL, CampaignFrequencyType.ONCE_PER_DAY, 5, "admin-1");

        assertThat(campaign.getMaxDisplaysPerDay()).isNull();
    }

    // --- screens ---

    @Test
    void addScreenAppendsAndPositions() {
        Campaign campaign = draftCampaign();

        CampaignScreen first = campaign.addScreen(screenDraft(2), "admin-2");
        CampaignScreen second = campaign.addScreen(screenDraft(3), "admin-2");

        assertThat(first.getPosition()).isEqualTo(1);
        assertThat(second.getPosition()).isEqualTo(2);
        assertThat(campaign.getScreens()).hasSize(2);
        assertThat(campaign.getUpdatedBy()).isEqualTo("admin-2");
    }

    @Test
    void addScreenRejectsAFourthScreen() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(1), "a");
        campaign.addScreen(screenDraft(1), "a");
        campaign.addScreen(screenDraft(1), "a");

        assertThatThrownBy(() -> campaign.addScreen(screenDraft(1), "a"))
                .isInstanceOf(CampaignScreenLimitExceededException.class);
    }

    @Test
    void addScreenRejectsExceedingTheCombinedDurationLimit() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(6), "a");

        assertThatThrownBy(() -> campaign.addScreen(screenDraft(5), "a"))
                .isInstanceOf(CampaignScreenLimitExceededException.class);
    }

    @Test
    void updateScreenReplacesContent() {
        Campaign campaign = draftCampaign();
        CampaignScreen screen = campaign.addScreen(screenDraft(2), "a");

        CampaignScreenDraft updated = new CampaignScreenDraft(4, null, "#ffffff", "New title", "Body",
                CampaignTextAlignment.LEFT, CampaignTextSize.EXTRA_LARGE, CampaignTextSize.SMALL, "#000000", 0.3,
                true, 2, CampaignActionType.INTERNAL, "Open", "/home");

        campaign.updateScreen(screen.getId(), updated, "a");

        CampaignScreen result = campaign.getScreens().get(0);
        assertThat(result.getDurationSeconds()).isEqualTo(4);
        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getTextAlignment()).isEqualTo(CampaignTextAlignment.LEFT);
        assertThat(result.getCloseAfterSeconds()).isEqualTo(2);
        assertThat(result.getActionTarget()).isEqualTo("/home");
    }

    @Test
    void updateScreenRejectsAnUnknownScreenId() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(2), "a");

        assertThatThrownBy(() -> campaign.updateScreen(UUID.randomUUID(), screenDraft(2), "a"))
                .isInstanceOf(CampaignScreenNotFoundException.class)
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void updateScreenRejectsPushingCombinedDurationOverTheLimit() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(4), "a");
        CampaignScreen second = campaign.addScreen(screenDraft(4), "a");

        assertThatThrownBy(() -> campaign.updateScreen(second.getId(), screenDraft(7), "a"))
                .isInstanceOf(CampaignScreenLimitExceededException.class);
    }

    @Test
    void removeScreenRenumbersTheRemaining() {
        Campaign campaign = draftCampaign();
        CampaignScreen first = campaign.addScreen(screenDraft(1), "a");
        campaign.addScreen(screenDraft(1), "a");
        campaign.addScreen(screenDraft(1), "a");

        campaign.removeScreen(first.getId(), "a");

        assertThat(campaign.getScreens()).hasSize(2);
        assertThat(campaign.getScreens().get(0).getPosition()).isEqualTo(1);
        assertThat(campaign.getScreens().get(1).getPosition()).isEqualTo(2);
    }

    @Test
    void removeScreenRejectsAnUnknownScreenId() {
        Campaign campaign = draftCampaign();

        assertThatThrownBy(() -> campaign.removeScreen(UUID.randomUUID(), "a"))
                .isInstanceOf(CampaignScreenNotFoundException.class);
    }

    @Test
    void reorderScreensReordersAndRenumbers() {
        Campaign campaign = draftCampaign();
        CampaignScreen a = campaign.addScreen(screenDraft(1), "x");
        CampaignScreen b = campaign.addScreen(screenDraft(1), "x");
        CampaignScreen c = campaign.addScreen(screenDraft(1), "x");

        campaign.reorderScreens(List.of(c.getId(), a.getId(), b.getId()), "x");

        assertThat(campaign.getScreens()).extracting(CampaignScreen::getId)
                .containsExactly(c.getId(), a.getId(), b.getId());
        assertThat(campaign.getScreens()).extracting(CampaignScreen::getPosition)
                .containsExactly(1, 2, 3);
    }

    @Test
    void reorderScreensRejectsAnIdSetThatIsNotAPermutation() {
        Campaign campaign = draftCampaign();
        CampaignScreen a = campaign.addScreen(screenDraft(1), "x");
        campaign.addScreen(screenDraft(1), "x");

        assertThatThrownBy(() -> campaign.reorderScreens(List.of(a.getId(), UUID.randomUUID()), "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replaceScreenImageBumpsTheAssetVersion() {
        Campaign campaign = draftCampaign();
        CampaignScreen screen = campaign.addScreen(screenDraft(2), "a");

        campaign.replaceScreenImage(screen.getId(), "campaigns/c/s.webp", "image/webp",
                1080, 1920, 400L, 0.5, 0.5, "a");
        assertThat(screen.getBackground().getVersion()).isEqualTo(1);

        campaign.replaceScreenImage(screen.getId(), "campaigns/c/s.webp", "image/webp",
                1080, 1920, 400L, 0.5, 0.5, "a");
        assertThat(screen.getBackground().getVersion()).isEqualTo(2);
    }

    // --- lifecycle ---

    @Test
    void publishFromDraftSetsPublishAudit() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(3), "a");

        campaign.publish("publisher-1");

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.PUBLISHED);
        assertThat(campaign.getPublishedBy()).isEqualTo("publisher-1");
        assertThat(campaign.getPublishedAt()).isNotNull();
        assertThat(campaign.isDeletable()).isFalse();
    }

    @Test
    void publishRejectsACampaignWithNoScreens() {
        Campaign campaign = draftCampaign();

        assertThatThrownBy(() -> campaign.publish("p"))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);
    }

    @Test
    void publishRejectsAScreenWithNeitherImageNorFallbackColour() {
        Campaign campaign = draftCampaign();
        CampaignScreenDraft noBackground = new CampaignScreenDraft(3, null, null, "t", null, null, null, null, null,
                null, false, null, CampaignActionType.NONE, null, null);
        campaign.addScreen(noBackground, "a");

        assertThatThrownBy(() -> campaign.publish("p"))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);
    }

    @Test
    void publishAcceptsAScreenBackedOnlyByAnUploadedImage() {
        Campaign campaign = draftCampaign();
        CampaignScreenDraft noColour = new CampaignScreenDraft(3, null, null, "t", null, null, null, null, null,
                null, false, null, CampaignActionType.NONE, null, null);
        CampaignScreen screen = campaign.addScreen(noColour, "a");
        campaign.replaceScreenImage(screen.getId(), "campaigns/c/s.webp", "image/webp",
                null, null, null, null, null, "a");

        campaign.publish("p");

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.PUBLISHED);
    }

    @Test
    void publishRejectsAnArchivedCampaign() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(3), "a");
        campaign.archive("a");

        assertThatThrownBy(() -> campaign.publish("p"))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);
    }

    @Test
    void pausedCampaignCanBeRepublished() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(3), "a");
        campaign.publish("p");
        campaign.pause("a");

        campaign.publish("p2");

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.PUBLISHED);
        assertThat(campaign.getPublishedBy()).isEqualTo("p2");
    }

    @Test
    void pauseRequiresAPublishedCampaign() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(3), "a");

        assertThatThrownBy(() -> campaign.pause("a"))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);
    }

    @Test
    void archiveIsAllowedFromAnyLiveStateButNotTwice() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(3), "a");
        campaign.publish("p");

        campaign.archive("a");
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ARCHIVED);

        assertThatThrownBy(() -> campaign.archive("a"))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);
    }

    @Test
    void updateDetailsIsRejectedOnceArchived() {
        Campaign campaign = draftCampaign();
        campaign.archive("a");

        assertThatThrownBy(() -> campaign.updateDetails("new", null, START, END, 1,
                CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, "a"))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);
    }

    @Test
    void addScreenIsRejectedOnceArchived() {
        Campaign campaign = draftCampaign();
        campaign.archive("a");

        assertThatThrownBy(() -> campaign.addScreen(screenDraft(2), "a"))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);
    }

    // --- runtime projections ---

    private static Campaign publishedCampaign(Instant start, Instant end, CampaignAudience audience) {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "c", null, start, end, 1,
                audience, CampaignFrequencyType.ALWAYS, null, "a");
        campaign.addScreen(screenDraft(3), "a");
        campaign.publish("p");
        return campaign;
    }

    @Test
    void isEligibleWhenPublishedInWindowAndAudienceMatches() {
        Instant now = Instant.parse("2026-01-15T00:00:00Z");
        Campaign campaign = publishedCampaign(START, END, CampaignAudience.ALL);

        assertThat(campaign.isEligibleAt(now, true)).isTrue();
        assertThat(campaign.isEligibleAt(now, false)).isTrue();
    }

    @Test
    void isNotEligibleOutsideTheScheduleWindow() {
        Campaign campaign = publishedCampaign(START, END, CampaignAudience.ALL);

        assertThat(campaign.isEligibleAt(START.minus(1, ChronoUnit.DAYS), true)).isFalse();
        assertThat(campaign.isEligibleAt(END, true)).isFalse();
    }

    @Test
    void isNotEligibleWhileStillADraft() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(3), "a");

        assertThat(campaign.isEligibleAt(Instant.parse("2026-01-15T00:00:00Z"), true)).isFalse();
    }

    @Test
    void anonymousAudienceIsHiddenFromAuthenticatedSessions() {
        Instant now = Instant.parse("2026-01-15T00:00:00Z");
        Campaign campaign = publishedCampaign(START, END, CampaignAudience.ANONYMOUS);

        assertThat(campaign.isEligibleAt(now, false)).isTrue();
        assertThat(campaign.isEligibleAt(now, true)).isFalse();
    }

    @Test
    void authenticatedAudienceIsHiddenFromAnonymousSessions() {
        Instant now = Instant.parse("2026-01-15T00:00:00Z");
        Campaign campaign = publishedCampaign(START, END, CampaignAudience.AUTHENTICATED);

        assertThat(campaign.isEligibleAt(now, true)).isTrue();
        assertThat(campaign.isEligibleAt(now, false)).isFalse();
    }

    @Test
    void runtimeStatusIsDerivedFromScheduleForPublishedCampaigns() {
        Campaign campaign = publishedCampaign(START, END, CampaignAudience.ALL);

        assertThat(campaign.runtimeStatus(START.minus(1, ChronoUnit.DAYS))).isEqualTo(Campaign.RuntimeStatus.SCHEDULED);
        assertThat(campaign.runtimeStatus(Instant.parse("2026-01-15T00:00:00Z"))).isEqualTo(Campaign.RuntimeStatus.ACTIVE);
        assertThat(campaign.runtimeStatus(END.plus(1, ChronoUnit.DAYS))).isEqualTo(Campaign.RuntimeStatus.EXPIRED);
    }

    @Test
    void runtimeStatusMirrorsAdminStateWhenNotPublished() {
        Instant now = Instant.parse("2026-01-15T00:00:00Z");
        Campaign draft = draftCampaign();
        assertThat(draft.runtimeStatus(now)).isEqualTo(Campaign.RuntimeStatus.DRAFT);

        draft.addScreen(screenDraft(3), "a");
        draft.publish("p");
        draft.pause("a");
        assertThat(draft.runtimeStatus(now)).isEqualTo(Campaign.RuntimeStatus.PAUSED);

        draft.archive("a");
        assertThat(draft.runtimeStatus(now)).isEqualTo(Campaign.RuntimeStatus.ARCHIVED);
    }

    @Test
    void reconstituteRestoresScreensInPositionOrder() {
        CampaignScreen s2 = CampaignScreen.reconstitute(UUID.randomUUID(), 2, 3, CampaignLayoutType.FULL_BACKGROUND,
                null, "#111", null, null, CampaignTextAlignment.CENTER, CampaignTextSize.LARGE, CampaignTextSize.MEDIUM,
                null, null, false, null, CampaignActionType.NONE, null, null);
        CampaignScreen s1 = CampaignScreen.reconstitute(UUID.randomUUID(), 1, 2, CampaignLayoutType.FULL_BACKGROUND,
                null, "#222", null, null, CampaignTextAlignment.CENTER, CampaignTextSize.LARGE, CampaignTextSize.MEDIUM,
                null, null, false, null, CampaignActionType.NONE, null, null);

        Campaign campaign = Campaign.reconstitute(UUID.randomUUID(), "c", null, CampaignStatus.PUBLISHED, START, END,
                1, CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, List.of(s2, s1),
                "a", START, "a", START, "p", START);

        assertThat(campaign.getScreens()).extracting(CampaignScreen::getPosition).containsExactly(1, 2);
    }
}
