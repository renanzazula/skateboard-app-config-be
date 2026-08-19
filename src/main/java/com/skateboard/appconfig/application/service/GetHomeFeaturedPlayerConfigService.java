package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetHomeFeaturedPlayerConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadHomeFeaturedPlayerConfigPort;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetHomeFeaturedPlayerConfigService implements GetHomeFeaturedPlayerConfigUseCase {

    private final LoadHomeFeaturedPlayerConfigPort loadHomeFeaturedPlayerConfigPort;

    public GetHomeFeaturedPlayerConfigService(LoadHomeFeaturedPlayerConfigPort loadHomeFeaturedPlayerConfigPort) {
        this.loadHomeFeaturedPlayerConfigPort = loadHomeFeaturedPlayerConfigPort;
    }

    @Override
    @Transactional
    public HomeFeaturedPlayerConfig execute() {
        return loadHomeFeaturedPlayerConfigPort.getOrCreate();
    }
}
