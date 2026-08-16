package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.BrandingAsset;

import java.util.UUID;

public interface ReplaceBrandingAssetUseCase {

    record Command(String adminId, UUID assetId, byte[] data, String mimeType) {}

    BrandingAsset execute(Command command);
}
