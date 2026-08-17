package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateHomeVideoCategoryConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadHomeVideoCategoryConfigPort;
import com.skateboard.appconfig.application.port.out.SaveHomeVideoCategoryConfigPort;
import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateHomeVideoCategoryConfigService implements UpdateHomeVideoCategoryConfigUseCase {

    private final LoadHomeVideoCategoryConfigPort loadHomeVideoCategoryConfigPort;
    private final SaveHomeVideoCategoryConfigPort saveHomeVideoCategoryConfigPort;

    public UpdateHomeVideoCategoryConfigService(LoadHomeVideoCategoryConfigPort loadHomeVideoCategoryConfigPort,
                                                 SaveHomeVideoCategoryConfigPort saveHomeVideoCategoryConfigPort) {
        this.loadHomeVideoCategoryConfigPort = loadHomeVideoCategoryConfigPort;
        this.saveHomeVideoCategoryConfigPort = saveHomeVideoCategoryConfigPort;
    }

    @Override
    @Transactional
    public HomeVideoCategoryConfig execute(Command command) {
        HomeVideoCategoryConfig config = loadHomeVideoCategoryConfigPort.getOrCreate();
        config.updateCategories(command.mode(), command.enabledCategoryIds());
        config.touch(command.adminId());
        return saveHomeVideoCategoryConfigPort.save(config);
    }
}
