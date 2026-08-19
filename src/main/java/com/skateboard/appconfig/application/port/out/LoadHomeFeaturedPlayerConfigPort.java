package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;

public interface LoadHomeFeaturedPlayerConfigPort {
    HomeFeaturedPlayerConfig getOrCreate();
}
