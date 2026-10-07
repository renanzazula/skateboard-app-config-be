package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateGuestApplicationConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadGuestApplicationConfigPort;
import com.skateboard.appconfig.application.port.out.SaveGuestApplicationConfigPort;
import com.skateboard.appconfig.domain.model.GuestApplicationConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateGuestApplicationConfigService implements UpdateGuestApplicationConfigUseCase {

    private final LoadGuestApplicationConfigPort loadGuestApplicationConfigPort;
    private final SaveGuestApplicationConfigPort saveGuestApplicationConfigPort;

    public UpdateGuestApplicationConfigService(LoadGuestApplicationConfigPort loadGuestApplicationConfigPort,
                                                SaveGuestApplicationConfigPort saveGuestApplicationConfigPort) {
        this.loadGuestApplicationConfigPort = loadGuestApplicationConfigPort;
        this.saveGuestApplicationConfigPort = saveGuestApplicationConfigPort;
    }

    @Override
    @Transactional
    public GuestApplicationConfig execute(Command command) {
        GuestApplicationConfig config = loadGuestApplicationConfigPort.getOrCreate();
        config.update(command.enabled(), command.recipientIds(), command.adminId());
        return saveGuestApplicationConfigPort.save(config);
    }
}
