package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetGuestApplicationConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadGuestApplicationConfigPort;
import com.skateboard.appconfig.domain.model.GuestApplicationConfig;
import org.springframework.stereotype.Service;

@Service
public class GetGuestApplicationConfigService implements GetGuestApplicationConfigUseCase {

    private final LoadGuestApplicationConfigPort loadGuestApplicationConfigPort;

    public GetGuestApplicationConfigService(LoadGuestApplicationConfigPort loadGuestApplicationConfigPort) {
        this.loadGuestApplicationConfigPort = loadGuestApplicationConfigPort;
    }

    @Override
    public GuestApplicationConfig execute() {
        return loadGuestApplicationConfigPort.getOrCreate();
    }
}
