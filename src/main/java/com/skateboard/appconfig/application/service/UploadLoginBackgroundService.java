package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UploadLoginBackgroundUseCase;
import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.application.port.out.SaveAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UploadLoginBackgroundService implements UploadLoginBackgroundUseCase {

    private final LoadAppConfigPort loadAppConfigPort;
    private final SaveAppConfigPort saveAppConfigPort;
    private final ObjectStoragePort objectStoragePort;

    public UploadLoginBackgroundService(LoadAppConfigPort loadAppConfigPort, SaveAppConfigPort saveAppConfigPort,
                                         ObjectStoragePort objectStoragePort) {
        this.loadAppConfigPort = loadAppConfigPort;
        this.saveAppConfigPort = saveAppConfigPort;
        this.objectStoragePort = objectStoragePort;
    }

    @Override
    @Transactional
    public AppConfig execute(Command command) {
        String extension = ImageUploadValidator.extensionFor(command.data(), command.mimeType());
        AppConfig config = loadAppConfigPort.getOrCreate();
        String previousKey = config.getLoginBackgroundKey();
        String key = "login/background" + extension;

        objectStoragePort.put(key, command.data(), command.mimeType());
        config.updateLoginBackground(key);
        config.touch(command.adminId());
        AppConfig saved = saveAppConfigPort.save(config);

        if (previousKey != null && !previousKey.equals(key)) {
            objectStoragePort.delete(previousKey);
        }
        return saved;
    }
}
