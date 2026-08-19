package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;

/**
 * Reads the default Home Featured Player configuration. Any authenticated
 * user may call this — it backs both the admin configuration screen and the
 * BFF's runtime lookup of the Home dashboard's featured content, so it
 * deliberately does not require the admin permission that
 * {@link UpdateHomeFeaturedPlayerConfigUseCase} does.
 */
public interface GetHomeFeaturedPlayerConfigUseCase {
    HomeFeaturedPlayerConfig execute();
}
