package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.RemoveLoginBackgroundUseCase;
import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.application.port.out.SaveAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveLoginBackgroundService implements RemoveLoginBackgroundUseCase {

    private final LoadAppConfigPort loadAppConfigPort;
    private final SaveAppConfigPort saveAppConfigPort;
    private final ObjectStoragePort objectStoragePort;

    public RemoveLoginBackgroundService(LoadAppConfigPort loadAppConfigPort, SaveAppConfigPort saveAppConfigPort,
                                         ObjectStoragePort objectStoragePort) {
        this.loadAppConfigPort = loadAppConfigPort;
        this.saveAppConfigPort = saveAppConfigPort;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @Transactional
    public AppConfig execute(Command command) {
        AppConfig config = loadAppConfigPort.getOrCreate();
        String previousKey = config.getLoginBackgroundKey();

        config.removeLoginBackground();
        config.touch(command.adminId());
        AppConfig saved = saveAppConfigPort.save(config);

        if (previousKey != null) {
            objectStoragePort.delete(previousKey);
        }
        return saved;
    }
}
