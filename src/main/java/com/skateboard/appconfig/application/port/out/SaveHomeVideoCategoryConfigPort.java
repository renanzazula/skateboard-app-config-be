package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;

public interface SaveHomeVideoCategoryConfigPort {
    HomeVideoCategoryConfig save(HomeVideoCategoryConfig config);
}
