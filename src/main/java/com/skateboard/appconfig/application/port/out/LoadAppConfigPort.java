package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.AppConfig;

public interface LoadAppConfigPort {
    AppConfig getOrCreate();
}
