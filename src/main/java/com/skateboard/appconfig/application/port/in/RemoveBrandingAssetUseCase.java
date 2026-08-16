package com.skateboard.appconfig.application.port.in;

import java.util.UUID;

public interface RemoveBrandingAssetUseCase {

    record Command(UUID assetId) {}

    void execute(Command command);
}
