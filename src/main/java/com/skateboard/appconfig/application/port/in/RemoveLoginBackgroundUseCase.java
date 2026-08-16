package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.AppConfig;

public interface RemoveLoginBackgroundUseCase {

    record Command(String adminId) {}

    AppConfig execute(Command command);
}
