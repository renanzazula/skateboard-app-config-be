package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.AppConfig;

public interface SaveAppConfigPort {
    AppConfig save(AppConfig config);
}
