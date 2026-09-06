package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.CreateCampaignUseCase;
import com.skateboard.appconfig.application.port.in.UpdateCampaignUseCase;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignActionType;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import com.skateboard.appconfig.domain.model.CampaignLayoutType;
import com.skateboard.appconfig.domain.model.CampaignMediaAsset;
import com.skateboard.appconfig.domain.model.CampaignScreen;
import com.skateboard.appconfig.domain.model.CampaignScreenDraft;
import com.skateboard.appconfig.domain.model.CampaignTextAlignment;
import com.skateboard.appconfig.domain.model.CampaignTextSize;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignRequest;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignResponse;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignRuntimeResponse;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignScreenRequest;
import com.skateboard.appconfig.infrastructure.web.dto.CampaignScreenResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Translates between the OpenAPI-generated transport DTOs and the campaign
 * domain / use-case commands, and — like {@code BrandingAdminController} does
 * inline — turns a screen's stored object key into a freshly presigned URL on
 * every read. Domain and DTO enums are named identically but live in different
 * packages; they map one-to-one by constant name.
 */
@Component
public class CampaignWebMapper {

    private final ObjectStoragePort objectStoragePort;

    public CampaignWebMapper(ObjectStoragePort objectStoragePort) {
        this.objectStoragePort = objectStoragePort;
    }

    // ---- request -> use-case input ----

    CreateCampaignUseCase.Command toCreateCommand(String adminId, CampaignRequest r) {
        return new CreateCampaignUseCase.Command(adminId, r.getName(), r.getDescription(),
                toInstant(r.getStartAt()), toInstant(r.getEndAt()), priority(r.getPriority()),
                mapEnum(r.getAudience(), CampaignAudience.class),
                mapEnum(r.getFrequencyType(), CampaignFrequencyType.class),
                r.getMaxDisplaysPerDay());
    }

    UpdateCampaignUseCase.Command toUpdateCommand(String adminId, UUID campaignId, CampaignRequest r) {
        return new UpdateCampaignUseCase.Command(adminId, campaignId, r.getName(), r.getDescription(),
                toInstant(r.getStartAt()), toInstant(r.getEndAt()), priority(r.getPriority()),
                mapEnum(r.getAudience(), CampaignAudience.class),
                mapEnum(r.getFrequencyType(), CampaignFrequencyType.class),
                r.getMaxDisplaysPerDay());
    }

    CampaignScreenDraft toDraft(CampaignScreenRequest r) {
        return new CampaignScreenDraft(
                r.getDurationSeconds(),
                mapEnum(r.getLayoutType(), CampaignLayoutType.class),
                r.getBackgroundColor(),
                r.getTitle(),
                r.getDescription(),
                mapEnum(r.getTextAlignment(), CampaignTextAlignment.class),
                mapEnum(r.getTitleSize(), CampaignTextSize.class),
                mapEnum(r.getDescriptionSize(), CampaignTextSize.class),
                r.getTextColor(),
                r.getOverlayOpacity() == null ? null : r.getOverlayOpacity().doubleValue(),
                Boolean.TRUE.equals(r.getCloseEnabled()),
                r.getCloseAfterSeconds(),
                mapEnum(r.getActionType(), CampaignActionType.class),
                r.getActionLabel(),
                r.getActionTarget());
    }

    // ---- domain -> response ----

    CampaignResponse toResponse(Campaign c) {
        Instant now = Instant.now();
        return new CampaignResponse()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .status(mapEnum(c.runtimeStatus(now),
                        com.skateboard.appconfig.infrastructure.web.dto.CampaignStatus.class))
                .startAt(toOffsetDateTime(c.getStartAt()))
                .endAt(toOffsetDateTime(c.getEndAt()))
                .priority(c.getPriority())
                .audience(mapEnum(c.getAudience(),
                        com.skateboard.appconfig.infrastructure.web.dto.CampaignAudience.class))
                .frequencyType(mapEnum(c.getFrequencyType(),
                        com.skateboard.appconfig.infrastructure.web.dto.CampaignFrequencyType.class))
                .maxDisplaysPerDay(c.getMaxDisplaysPerDay())
                .screens(c.getScreens().stream().map(this::toScreenResponse).toList())
                .createdBy(c.getCreatedBy())
                .createdAt(toOffsetDateTime(c.getCreatedAt()))
                .updatedBy(c.getUpdatedBy())
                .updatedAt(toOffsetDateTime(c.getUpdatedAt()))
                .publishedBy(c.getPublishedBy())
                .publishedAt(toOffsetDateTime(c.getPublishedAt()));
    }

    CampaignRuntimeResponse toRuntimeResponse(Campaign c) {
        return new CampaignRuntimeResponse()
                .id(c.getId())
                .priority(c.getPriority())
                .frequencyType(mapEnum(c.getFrequencyType(),
                        com.skateboard.appconfig.infrastructure.web.dto.CampaignFrequencyType.class))
                .maxDisplaysPerDay(c.getMaxDisplaysPerDay())
                .screens(c.getScreens().stream().map(this::toScreenResponse).toList());
    }

    CampaignScreenResponse toScreenResponse(CampaignScreen s) {
        CampaignScreenResponse response = new CampaignScreenResponse()
                .id(s.getId())
                .position(s.getPosition())
                .durationSeconds(s.getDurationSeconds())
                .layoutType(mapEnum(s.getLayoutType(),
                        com.skateboard.appconfig.infrastructure.web.dto.CampaignLayoutType.class))
                .backgroundColor(s.getBackgroundColor())
                .title(s.getTitle())
                .description(s.getDescription())
                .textAlignment(mapEnum(s.getTextAlignment(),
                        com.skateboard.appconfig.infrastructure.web.dto.CampaignTextAlignment.class))
                .titleSize(mapEnum(s.getTitleSize(),
                        com.skateboard.appconfig.infrastructure.web.dto.CampaignTextSize.class))
                .descriptionSize(mapEnum(s.getDescriptionSize(),
                        com.skateboard.appconfig.infrastructure.web.dto.CampaignTextSize.class))
                .textColor(s.getTextColor())
                .overlayOpacity(toFloat(s.getOverlayOpacity()))
                .closeEnabled(s.isCloseEnabled())
                .closeAfterSeconds(s.getCloseAfterSeconds())
                .actionType(mapEnum(s.getActionType(),
                        com.skateboard.appconfig.infrastructure.web.dto.CampaignActionType.class))
                .actionLabel(s.getActionLabel())
                .actionTarget(s.getActionTarget());

        CampaignMediaAsset background = s.getBackground();
        if (background != null) {
            response.backgroundUrl(objectStoragePort.presignGetUrl(background.getStorageKey()))
                    .focalPointX(toFloat(background.getFocalPointX()))
                    .focalPointY(toFloat(background.getFocalPointY()));
        }
        return response;
    }

    // ---- helpers ----

    private static <T extends Enum<T>> T mapEnum(Enum<?> source, Class<T> target) {
        return source == null ? null : Enum.valueOf(target, source.name());
    }

    private static int priority(Integer value) {
        return value == null ? 0 : value;
    }

    private static Float toFloat(Double value) {
        return value == null ? null : value.floatValue();
    }

    private static Instant toInstant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private static OffsetDateTime toOffsetDateTime(Instant value) {
        return value == null ? null : OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
