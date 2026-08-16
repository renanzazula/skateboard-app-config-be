package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetPublicConfigUseCase;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.model.AppConfig;
import com.skateboard.appconfig.infrastructure.web.api.PublicApi;
import com.skateboard.appconfig.infrastructure.web.dto.PublicConfigResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
public class PublicConfigController implements PublicApi {

    private final GetPublicConfigUseCase getPublicConfigUseCase;
    private final ObjectStoragePort objectStoragePort;

    public PublicConfigController(GetPublicConfigUseCase getPublicConfigUseCase, ObjectStoragePort objectStoragePort) {
        this.getPublicConfigUseCase = getPublicConfigUseCase;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    public ResponseEntity<PublicConfigResponse> getPublicConfig() {
        AppConfig config = getPublicConfigUseCase.execute();
        PublicConfigResponse response = new PublicConfigResponse()
                .loginBackgroundUrl(objectStoragePort.presignGetUrl(config.getLoginBackgroundKey()))
                .loginBackgroundVersion(config.getLoginBackgroundVersion())
                .loginBackgroundUpdatedAt(toOffsetDateTime(config.getLoginBackgroundUpdatedAt()))
                .appLogoUrl(objectStoragePort.presignGetUrl(config.getAppLogoKey()))
                .appLogoVersion(config.getAppLogoVersion())
                .appLogoUpdatedAt(toOffsetDateTime(config.getAppLogoUpdatedAt()));
        return ResponseEntity.ok(response);
    }

    private OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
        return instant != null ? OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }
}
