package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetHomeFeaturedPlayerConfigUseCase;
import com.skateboard.appconfig.application.port.in.UpdateHomeFeaturedPlayerConfigUseCase;
import com.skateboard.appconfig.domain.model.FeaturedContentSource;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;
import com.skateboard.appconfig.infrastructure.web.api.HomeFeaturedPlayerApi;
import com.skateboard.appconfig.infrastructure.web.dto.HomeFeaturedPlayerConfigResponse;
import com.skateboard.appconfig.infrastructure.web.dto.HomePlayerPosition;
import com.skateboard.appconfig.infrastructure.web.dto.HomePlayerType;
import com.skateboard.appconfig.infrastructure.web.dto.UpdateHomeFeaturedPlayerConfigRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * GET is intentionally open to any authenticated caller (no
 * {@code @PreAuthorize}) — it backs both the admin configuration screen and
 * the BFF's runtime read of the default Featured Player configuration, so it
 * must not require the admin-only FUNC_HOME_FEATURED_PLAYER_CONFIG
 * permission that gates writes. Mirrors HomeVideoCategoryConfigController.
 */
@RestController
public class HomeFeaturedPlayerConfigController implements HomeFeaturedPlayerApi {

    private final GetHomeFeaturedPlayerConfigUseCase getHomeFeaturedPlayerConfigUseCase;
    private final UpdateHomeFeaturedPlayerConfigUseCase updateHomeFeaturedPlayerConfigUseCase;

    public HomeFeaturedPlayerConfigController(GetHomeFeaturedPlayerConfigUseCase getHomeFeaturedPlayerConfigUseCase,
                                               UpdateHomeFeaturedPlayerConfigUseCase updateHomeFeaturedPlayerConfigUseCase) {
        this.getHomeFeaturedPlayerConfigUseCase = getHomeFeaturedPlayerConfigUseCase;
        this.updateHomeFeaturedPlayerConfigUseCase = updateHomeFeaturedPlayerConfigUseCase;
    }

    @Override
    public ResponseEntity<HomeFeaturedPlayerConfigResponse> getHomeFeaturedPlayerConfig() {
        HomeFeaturedPlayerConfig config = getHomeFeaturedPlayerConfigUseCase.execute();
        return ResponseEntity.ok(toResponse(config));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_HOME_FEATURED_PLAYER_CONFIG')")
    public ResponseEntity<HomeFeaturedPlayerConfigResponse> updateHomeFeaturedPlayerConfig(UpdateHomeFeaturedPlayerConfigRequest request) {
        HomeFeaturedPlayerConfig updated = updateHomeFeaturedPlayerConfigUseCase.execute(
                new UpdateHomeFeaturedPlayerConfigUseCase.Command(
                        currentAdminId(),
                        Boolean.TRUE.equals(request.getEnabled()),
                        toDomainSource(request.getContentSource()),
                        request.getContentId(),
                        toDomainPlayerType(request.getPlayerType()),
                        toDomainPosition(request.getPosition())));
        return ResponseEntity.ok(toResponse(updated));
    }

    private HomeFeaturedPlayerConfigResponse toResponse(HomeFeaturedPlayerConfig config) {
        return new HomeFeaturedPlayerConfigResponse()
                .enabled(config.isEnabled())
                .contentSource(toDtoSource(config.getContentSource()))
                .contentId(config.getContentId())
                .playerType(toDtoPlayerType(config.getPlayerType()))
                .position(toDtoPosition(config.getPosition()))
                .updatedAt(toOffsetDateTime(config.getUpdatedAt()));
    }

    private FeaturedContentSource toDomainSource(com.skateboard.appconfig.infrastructure.web.dto.FeaturedContentSource source) {
        return source != null ? FeaturedContentSource.valueOf(source.getValue()) : null;
    }

    private com.skateboard.appconfig.infrastructure.web.dto.FeaturedContentSource toDtoSource(FeaturedContentSource source) {
        return source != null ? com.skateboard.appconfig.infrastructure.web.dto.FeaturedContentSource.fromValue(source.name()) : null;
    }

    private HomeFeaturedPlayerConfig.PlayerType toDomainPlayerType(HomePlayerType playerType) {
        return playerType != null ? HomeFeaturedPlayerConfig.PlayerType.valueOf(playerType.getValue()) : null;
    }

    private HomePlayerType toDtoPlayerType(HomeFeaturedPlayerConfig.PlayerType playerType) {
        return HomePlayerType.fromValue(playerType.name());
    }

    private HomeFeaturedPlayerConfig.Position toDomainPosition(HomePlayerPosition position) {
        return position != null ? HomeFeaturedPlayerConfig.Position.valueOf(position.getValue()) : null;
    }

    private HomePlayerPosition toDtoPosition(HomeFeaturedPlayerConfig.Position position) {
        return HomePlayerPosition.fromValue(position.name());
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant != null ? OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    private String currentAdminId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
