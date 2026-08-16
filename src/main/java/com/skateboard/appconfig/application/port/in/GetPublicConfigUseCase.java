package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.AppConfig;

public interface GetPublicConfigUseCase {
    AppConfig execute();
}
