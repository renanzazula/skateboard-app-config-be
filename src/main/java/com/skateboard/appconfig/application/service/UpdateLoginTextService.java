package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateLoginTextUseCase;
import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.application.port.out.SaveAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateLoginTextService implements UpdateLoginTextUseCase {

    private final LoadAppConfigPort loadAppConfigPort;
    private final SaveAppConfigPort saveAppConfigPort;

    public UpdateLoginTextService(LoadAppConfigPort loadAppConfigPort, SaveAppConfigPort saveAppConfigPort) {
        this.loadAppConfigPort = loadAppConfigPort;
        this.saveAppConfigPort = saveAppConfigPort;
    }

    @Override
    @Transactional
    public AppConfig execute(Command command) {
        AppConfig config = loadAppConfigPort.getOrCreate();
        config.updateLoginText(command.title(), command.message());
        config.touch(command.adminId());
        return saveAppConfigPort.save(config);
    }
}
