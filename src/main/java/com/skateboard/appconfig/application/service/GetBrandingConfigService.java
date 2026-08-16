package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetBrandingConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetBrandingConfigService implements GetBrandingConfigUseCase {

    private final LoadAppConfigPort loadAppConfigPort;

    public GetBrandingConfigService(LoadAppConfigPort loadAppConfigPort) {
        this.loadAppConfigPort = loadAppConfigPort;
    }

    @Override
    @Transactional
    public AppConfig execute() {
        return loadAppConfigPort.getOrCreate();
    }
}
