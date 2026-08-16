package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.AppConfig;

public interface UpdateLoginTextUseCase {

    record Command(String adminId, String title, String message) {}

    AppConfig execute(Command command);
}
