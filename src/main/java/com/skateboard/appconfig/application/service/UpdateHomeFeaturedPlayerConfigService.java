package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateHomeFeaturedPlayerConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadHomeFeaturedPlayerConfigPort;
import com.skateboard.appconfig.application.port.out.SaveHomeFeaturedPlayerConfigPort;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateHomeFeaturedPlayerConfigService implements UpdateHomeFeaturedPlayerConfigUseCase {

    private final LoadHomeFeaturedPlayerConfigPort loadHomeFeaturedPlayerConfigPort;
    private final SaveHomeFeaturedPlayerConfigPort saveHomeFeaturedPlayerConfigPort;

    public UpdateHomeFeaturedPlayerConfigService(LoadHomeFeaturedPlayerConfigPort loadHomeFeaturedPlayerConfigPort,
                                                  SaveHomeFeaturedPlayerConfigPort saveHomeFeaturedPlayerConfigPort) {
        this.loadHomeFeaturedPlayerConfigPort = loadHomeFeaturedPlayerConfigPort;
        this.saveHomeFeaturedPlayerConfigPort = saveHomeFeaturedPlayerConfigPort;
    }

    @Override
    @Transactional
    public HomeFeaturedPlayerConfig execute(Command command) {
        HomeFeaturedPlayerConfig config = loadHomeFeaturedPlayerConfigPort.getOrCreate();
        config.update(command.enabled(), command.contentSource(), command.contentId(),
                command.playerType(), command.position(), command.preferredPlatform());
        config.touch(command.adminId());
        return saveHomeFeaturedPlayerConfigPort.save(config);
    }
}
