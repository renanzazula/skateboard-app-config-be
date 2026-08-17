package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;

public interface LoadHomeVideoCategoryConfigPort {
    HomeVideoCategoryConfig getOrCreate();
}
