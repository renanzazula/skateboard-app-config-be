package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.CreateCampaignUseCase;
import com.skateboard.appconfig.application.port.in.UpdateCampaignUseCase;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import com.skateboard.appconfig.domain.model.CampaignLayoutType;
import com.skateboard.appconfig.domain.model.CampaignScreenDraft;
import com.skateboard.appconfig.domain.model.CampaignStatus;
import com.skateboard.appconfig.domain.model.CampaignTextAlignment;
import com.skateboard.appconfig.domain.model.CampaignTextSize;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignRequest;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignResponse;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignRuntimeResponse;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignScreenRequest;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignScreenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The REST mapping layer is where two contracts that are easy to break silently
 * live: the API's {@code status} is the schedule-derived runtime value (never
 * the persisted admin one), and a screen's {@code backgroundUrl} is presigned
 * fresh on every read from the stored object key.
 */
class CampaignWebMapperTest {

    private static final Instant START = Instant.parse("2026-03-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-04-01T00:00:00Z");

    @Mock
    private ObjectStoragePort objectStoragePort;

    private CampaignWebMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new CampaignWebMapper(objectStoragePort);
    }

    private static Campaign draftCampaign() {
        return Campaign.create(UUID.randomUUID(), "Spring drop", "desc", START, END, 5,
                CampaignAudience.ALL, CampaignFrequencyType.ALWAYS, null, "admin-1");
    }

    private static CampaignScreenDraft screenDraft(int durationSeconds) {
        return new CampaignScreenDraft(durationSeconds, null, "#101010", "Title", null, null, null, null,
                null, null, false, null, null, null, null);
    }

    // ---- request -> use-case command ----

    @Test
    void toCreateCommandCarriesTheAdminIdAndConvertsTheScheduleToInstants() {
        CampaignRequest request = new CampaignRequest()
                .name("Spring drop")
                .description("desc")
                .startAt(OffsetDateTime.ofInstant(START, ZoneOffset.UTC))
                .endAt(OffsetDateTime.ofInstant(END, ZoneOffset.UTC))
                .priority(7)
                .audience(com.skateboard.appconfig.infrastructure.web.dto.CampaignAudience.AUTHENTICATED)
                .frequencyType(com.skateboard.appconfig.infrastructure.web.dto.CampaignFrequencyType.MAX_PER_DAY)
                .maxDisplaysPerDay(3);

        CreateCampaignUseCase.Command command = mapper.toCreateCommand("admin-1", request);

        assertThat(command.adminId()).isEqualTo("admin-1");
        assertThat(command.name()).isEqualTo("Spring drop");
        assertThat(command.startAt()).isEqualTo(START);
        assertThat(command.endAt()).isEqualTo(END);
        assertThat(command.priority()).isEqualTo(7);
        assertThat(command.audience()).isEqualTo(CampaignAudience.AUTHENTICATED);
        assertThat(command.frequencyType()).isEqualTo(CampaignFrequencyType.MAX_PER_DAY);
        assertThat(command.maxDisplaysPerDay()).isEqualTo(3);
    }

    @Test
    void anOmittedPriorityDefaultsToZeroRatherThanFailingOnUnboxing() {
        CampaignRequest request = new CampaignRequest()
                .name("n")
                .startAt(OffsetDateTime.ofInstant(START, ZoneOffset.UTC))
                .endAt(OffsetDateTime.ofInstant(END, ZoneOffset.UTC))
                .audience(com.skateboard.appconfig.infrastructure.web.dto.CampaignAudience.ALL)
                .frequencyType(com.skateboard.appconfig.infrastructure.web.dto.CampaignFrequencyType.ALWAYS);

        assertThat(mapper.toCreateCommand("a", request).priority()).isZero();
        assertThat(mapper.toUpdateCommand("a", UUID.randomUUID(), request).priority()).isZero();
    }

    @Test
    void toUpdateCommandKeepsTheTargetCampaignId() {
        UUID campaignId = UUID.randomUUID();
        CampaignRequest request = new CampaignRequest()
                .name("n")
                .startAt(OffsetDateTime.ofInstant(START, ZoneOffset.UTC))
                .endAt(OffsetDateTime.ofInstant(END, ZoneOffset.UTC))
                .priority(1)
                .audience(com.skateboard.appconfig.infrastructure.web.dto.CampaignAudience.ANONYMOUS)
                .frequencyType(com.skateboard.appconfig.infrastructure.web.dto.CampaignFrequencyType.ONCE);

        UpdateCampaignUseCase.Command command = mapper.toUpdateCommand("admin-2", campaignId, request);

        assertThat(command.campaignId()).isEqualTo(campaignId);
        assertThat(command.adminId()).isEqualTo("admin-2");
        assertThat(command.audience()).isEqualTo(CampaignAudience.ANONYMOUS);
    }

    @Test
    void toDraftMapsEveryStylingFieldAcrossTheEnumPackages() {
        CampaignScreenRequest request = new CampaignScreenRequest()
                .durationSeconds(4)
                .layoutType(com.skateboard.appconfig.infrastructure.web.dto.CampaignLayoutType.FULL_BACKGROUND)
                .backgroundColor("#000000")
                .title("t")
                .description("d")
                .textAlignment(com.skateboard.appconfig.infrastructure.web.dto.CampaignTextAlignment.LEFT)
                .titleSize(com.skateboard.appconfig.infrastructure.web.dto.CampaignTextSize.SMALL)
                .descriptionSize(com.skateboard.appconfig.infrastructure.web.dto.CampaignTextSize.LARGE)
                .textColor("#ffffff")
                .overlayOpacity(0.25f)
                .closeEnabled(true)
                .closeAfterSeconds(2);

        CampaignScreenDraft draft = mapper.toDraft(request);

        assertThat(draft.durationSeconds()).isEqualTo(4);
        assertThat(draft.layoutType()).isEqualTo(CampaignLayoutType.FULL_BACKGROUND);
        assertThat(draft.textAlignment()).isEqualTo(CampaignTextAlignment.LEFT);
        assertThat(draft.titleSize()).isEqualTo(CampaignTextSize.SMALL);
        assertThat(draft.descriptionSize()).isEqualTo(CampaignTextSize.LARGE);
        assertThat(draft.overlayOpacity()).isEqualTo(0.25d);
        assertThat(draft.closeEnabled()).isTrue();
        assertThat(draft.closeAfterSeconds()).isEqualTo(2);
    }

    @Test
    void anOmittedCloseEnabledIsFalseNotANullUnboxingFailure() {
        CampaignScreenDraft draft = mapper.toDraft(new CampaignScreenRequest().durationSeconds(3));

        assertThat(draft.closeEnabled()).isFalse();
        assertThat(draft.overlayOpacity()).isNull();
        assertThat(draft.layoutType()).isNull();
    }

    // ---- status is derived, never the persisted admin value ----

    @Test
    void aPublishedCampaignWhoseWindowHasNotOpenedIsReportedAsScheduled() {
        Campaign campaign = Campaign.reconstitute(UUID.randomUUID(), "n", null, CampaignStatus.PUBLISHED,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200), 0, CampaignAudience.ALL,
                CampaignFrequencyType.ALWAYS, null, List.of(), "a", Instant.now(), "a", Instant.now(), "a",
                Instant.now());

        assertThat(mapper.toResponse(campaign).getStatus())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.CampaignStatus.SCHEDULED);
    }

    @Test
    void aPublishedCampaignInsideItsWindowIsReportedAsActive() {
        Campaign campaign = Campaign.reconstitute(UUID.randomUUID(), "n", null, CampaignStatus.PUBLISHED,
                Instant.now().minusSeconds(60), Instant.now().plusSeconds(60), 0, CampaignAudience.ALL,
                CampaignFrequencyType.ALWAYS, null, List.of(), "a", Instant.now(), "a", Instant.now(), "a",
                Instant.now());

        assertThat(mapper.toResponse(campaign).getStatus())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.CampaignStatus.ACTIVE);
    }

    @Test
    void aPublishedCampaignPastItsWindowIsReportedAsExpired() {
        Campaign campaign = Campaign.reconstitute(UUID.randomUUID(), "n", null, CampaignStatus.PUBLISHED,
                Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600), 0, CampaignAudience.ALL,
                CampaignFrequencyType.ALWAYS, null, List.of(), "a", Instant.now(), "a", Instant.now(), "a",
                Instant.now());

        assertThat(mapper.toResponse(campaign).getStatus())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.CampaignStatus.EXPIRED);
    }

    @Test
    void nonPublishedCampaignsReportTheirAdminStateVerbatim() {
        Campaign draft = draftCampaign();
        assertThat(mapper.toResponse(draft).getStatus())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.CampaignStatus.DRAFT);

        Campaign archived = draftCampaign();
        archived.archive("a");
        assertThat(mapper.toResponse(archived).getStatus())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.CampaignStatus.ARCHIVED);
    }

    // ---- responses ----

    @Test
    void toResponseCarriesTheGeneralConfigurationAndAuditTrail() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(3), "admin-1");

        CampaignResponse response = mapper.toResponse(campaign);

        assertThat(response.getId()).isEqualTo(campaign.getId());
        assertThat(response.getName()).isEqualTo("Spring drop");
        assertThat(response.getDescription()).isEqualTo("desc");
        assertThat(response.getPriority()).isEqualTo(5);
        assertThat(response.getStartAt().toInstant()).isEqualTo(START);
        assertThat(response.getEndAt().toInstant()).isEqualTo(END);
        assertThat(response.getCreatedBy()).isEqualTo("admin-1");
        assertThat(response.getPublishedAt()).isNull();
        assertThat(response.getScreens()).hasSize(1);
    }

    @Test
    void theRuntimeResponseOmitsAdminOnlyFieldsAndKeepsTheDisplayPolicy() {
        Campaign campaign = Campaign.create(UUID.randomUUID(), "Internal codename", "internal notes", START, END,
                9, CampaignAudience.ALL, CampaignFrequencyType.MAX_PER_DAY, 2, "admin-1");
        campaign.addScreen(screenDraft(3), "admin-1");

        CampaignRuntimeResponse response = mapper.toRuntimeResponse(campaign);

        assertThat(response.getId()).isEqualTo(campaign.getId());
        assertThat(response.getPriority()).isEqualTo(9);
        assertThat(response.getFrequencyType())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.CampaignFrequencyType.MAX_PER_DAY);
        assertThat(response.getMaxDisplaysPerDay()).isEqualTo(2);
        assertThat(response.getScreens()).hasSize(1);
    }

    @Test
    void aScreenBackgroundIsPresignedFreshFromTheStoredKeyOnEveryRead() {
        Campaign campaign = draftCampaign();
        UUID screenId = campaign.addScreen(screenDraft(3), "admin-1").getId();
        campaign.replaceScreenImage(screenId, "campaigns/x/y.webp", "image/webp", null, null, 10L, 0.5, 0.25,
                "admin-1");
        when(objectStoragePort.presignGetUrl("campaigns/x/y.webp")).thenReturn("https://signed.example/one");

        CampaignScreenResponse first = mapper.toResponse(campaign).getScreens().get(0);
        assertThat(first.getBackgroundUrl()).isEqualTo("https://signed.example/one");
        assertThat(first.getFocalPointX()).isEqualTo(0.5f);
        assertThat(first.getFocalPointY()).isEqualTo(0.25f);

        when(objectStoragePort.presignGetUrl("campaigns/x/y.webp")).thenReturn("https://signed.example/two");
        assertThat(mapper.toResponse(campaign).getScreens().get(0).getBackgroundUrl())
                .isEqualTo("https://signed.example/two");

        verify(objectStoragePort, times(2)).presignGetUrl("campaigns/x/y.webp");
    }

    @Test
    void aColourOnlyScreenIsNotPresignedAndCarriesNoBackgroundUrl() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(screenDraft(3), "admin-1");

        CampaignScreenResponse screen = mapper.toResponse(campaign).getScreens().get(0);

        assertThat(screen.getBackgroundUrl()).isNull();
        assertThat(screen.getFocalPointX()).isNull();
        assertThat(screen.getBackgroundColor()).isEqualTo("#101010");
        verify(objectStoragePort, never()).presignGetUrl(anyString());
    }

    @Test
    void screenResponsesKeepPositionDurationAndCtaContent() {
        Campaign campaign = draftCampaign();
        campaign.addScreen(new CampaignScreenDraft(4, null, "#101010", "Title", "Body",
                com.skateboard.appconfig.domain.model.CampaignTextAlignment.LEFT, null, null, "#ffffff", 0.4,
                true, 2, com.skateboard.appconfig.domain.model.CampaignActionType.INTERNAL, "Listen",
                "/podcasts/123"), "admin-1");

        CampaignScreenResponse screen = mapper.toResponse(campaign).getScreens().get(0);

        assertThat(screen.getPosition()).isEqualTo(1);
        assertThat(screen.getDurationSeconds()).isEqualTo(4);
        assertThat(screen.getTitle()).isEqualTo("Title");
        assertThat(screen.getOverlayOpacity()).isEqualTo(0.4f);
        assertThat(screen.getCloseEnabled()).isTrue();
        assertThat(screen.getCloseAfterSeconds()).isEqualTo(2);
        assertThat(screen.getActionType())
                .isEqualTo(com.skateboard.appconfig.infrastructure.web.dto.CampaignActionType.INTERNAL);
        assertThat(screen.getActionLabel()).isEqualTo("Listen");
        assertThat(screen.getActionTarget()).isEqualTo("/podcasts/123");
    }
}
