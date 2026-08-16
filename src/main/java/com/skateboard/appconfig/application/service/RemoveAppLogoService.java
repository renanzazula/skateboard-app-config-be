package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.RemoveAppLogoUseCase;
import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.application.port.out.SaveAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveAppLogoService implements RemoveAppLogoUseCase {

    private final LoadAppConfigPort loadAppConfigPort;
    private final SaveAppConfigPort saveAppConfigPort;
    private final ObjectStoragePort objectStoragePort;

    public RemoveAppLogoService(LoadAppConfigPort loadAppConfigPort, SaveAppConfigPort saveAppConfigPort,
                                 ObjectStoragePort objectStoragePort) {
        this.loadAppConfigPort = loadAppConfigPort;
        this.saveAppConfigPort = saveAppConfigPort;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @Transactional
    public AppConfig execute(Command command) {
        AppConfig config = loadAppConfigPort.getOrCreate();
        String previousKey = config.getAppLogoKey();

        config.removeAppLogo();
        config.touch(command.adminId());
        AppConfig saved = saveAppConfigPort.save(config);

        if (previousKey != null) {
            objectStoragePort.delete(previousKey);
        }
        return saved;
    }
}
