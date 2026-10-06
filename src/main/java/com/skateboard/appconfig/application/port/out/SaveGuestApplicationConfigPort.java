package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.GuestApplicationConfig;

public interface SaveGuestApplicationConfigPort {

    GuestApplicationConfig save(GuestApplicationConfig config);
}
