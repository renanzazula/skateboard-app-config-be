package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetHomeVideoCategoryConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadHomeVideoCategoryConfigPort;
import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetHomeVideoCategoryConfigService implements GetHomeVideoCategoryConfigUseCase {

    private final LoadHomeVideoCategoryConfigPort loadHomeVideoCategoryConfigPort;

    public GetHomeVideoCategoryConfigService(LoadHomeVideoCategoryConfigPort loadHomeVideoCategoryConfigPort) {
        this.loadHomeVideoCategoryConfigPort = loadHomeVideoCategoryConfigPort;
    }

    @Override
    @Transactional
    public HomeVideoCategoryConfig execute() {
        return loadHomeVideoCategoryConfigPort.getOrCreate();
    }
}
