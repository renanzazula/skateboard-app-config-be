package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.FeaturedContentSource;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;

public interface UpdateHomeFeaturedPlayerConfigUseCase {

    record Command(String adminId, boolean enabled, FeaturedContentSource contentSource, String contentId,
                    HomeFeaturedPlayerConfig.PlayerType playerType, HomeFeaturedPlayerConfig.Position position,
                    HomeFeaturedPlayerConfig.PreferredPlatform preferredPlatform) {}

    HomeFeaturedPlayerConfig execute(Command command);
}
