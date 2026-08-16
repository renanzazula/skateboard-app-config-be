package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.AppConfig;

public interface UploadAppLogoUseCase {

    record Command(String adminId, byte[] data, String mimeType) {}

    AppConfig execute(Command command);
}
