package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetHomeVideoCategoryConfigUseCase;
import com.skateboard.appconfig.application.port.in.UpdateHomeVideoCategoryConfigUseCase;
import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;
import com.skateboard.appconfig.infrastructure.web.api.HomeApi;
import com.skateboard.appconfig.infrastructure.web.dto.HomeVideoCategoryConfigMode;
import com.skateboard.appconfig.infrastructure.web.dto.HomeVideoCategoryConfigRequest;
import com.skateboard.appconfig.infrastructure.web.dto.HomeVideoCategoryConfigResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * GET is intentionally open to any authenticated caller (no
 * {@code @PreAuthorize}) — it backs both the admin configuration screen and
 * the BFF's runtime read of the effective Home configuration, so it must not
 * require the admin-only FUNC_HOME_CATEGORY_CONFIG permission that gates
 * writes. See PublicConfigController for the (unauthenticated) counterpart
 * used by the login screen.
 */
@RestController
public class HomeVideoCategoryConfigController implements HomeApi {

    private final GetHomeVideoCategoryConfigUseCase getHomeVideoCategoryConfigUseCase;
    private final UpdateHomeVideoCategoryConfigUseCase updateHomeVideoCategoryConfigUseCase;

    public HomeVideoCategoryConfigController(GetHomeVideoCategoryConfigUseCase getHomeVideoCategoryConfigUseCase,
                                              UpdateHomeVideoCategoryConfigUseCase updateHomeVideoCategoryConfigUseCase) {
        this.getHomeVideoCategoryConfigUseCase = getHomeVideoCategoryConfigUseCase;
        this.updateHomeVideoCategoryConfigUseCase = updateHomeVideoCategoryConfigUseCase;
    }

    @Override
    public ResponseEntity<HomeVideoCategoryConfigResponse> getHomeVideoCategoryConfig() {
        HomeVideoCategoryConfig config = getHomeVideoCategoryConfigUseCase.execute();
        return ResponseEntity.ok(toResponse(config));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_HOME_CATEGORY_CONFIG')")
    public ResponseEntity<HomeVideoCategoryConfigResponse> updateHomeVideoCategoryConfig(HomeVideoCategoryConfigRequest request) {
        HomeVideoCategoryConfig updated = updateHomeVideoCategoryConfigUseCase.execute(
                new UpdateHomeVideoCategoryConfigUseCase.Command(currentAdminId(), toDomainMode(request.getMode()),
                        toIdSet(request.getEnabledCategoryIds())));
        return ResponseEntity.ok(toResponse(updated));
    }

    private HomeVideoCategoryConfigResponse toResponse(HomeVideoCategoryConfig config) {
        return new HomeVideoCategoryConfigResponse()
                .mode(toDtoMode(config.getMode()))
                .enabledCategoryIds(List.copyOf(config.getEnabledCategoryIds()))
                .updatedAt(toOffsetDateTime(config.getUpdatedAt()));
    }

    private HomeVideoCategoryConfig.Mode toDomainMode(HomeVideoCategoryConfigMode mode) {
        return HomeVideoCategoryConfig.Mode.valueOf(mode.getValue());
    }

    private HomeVideoCategoryConfigMode toDtoMode(HomeVideoCategoryConfig.Mode mode) {
        return HomeVideoCategoryConfigMode.fromValue(mode.name());
    }

    private Set<String> toIdSet(List<String> ids) {
        return ids == null ? Set.of() : new LinkedHashSet<>(ids);
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
