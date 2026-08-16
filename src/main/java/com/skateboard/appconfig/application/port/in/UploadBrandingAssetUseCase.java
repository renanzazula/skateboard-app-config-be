package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.BrandingAsset;

public interface UploadBrandingAssetUseCase {

    record Command(String adminId, String name, byte[] data, String mimeType) {}

    BrandingAsset execute(Command command);
}
