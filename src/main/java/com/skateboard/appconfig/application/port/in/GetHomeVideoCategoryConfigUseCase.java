package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;

/**
 * Reads the effective Home category configuration. Any authenticated user
 * may call this — it backs both the admin configuration screen and the
 * BFF's runtime lookup of which categories are eligible for the Home
 * dashboard, so it deliberately does not require the admin permission that
 * {@link UpdateHomeVideoCategoryConfigUseCase} does.
 */
public interface GetHomeVideoCategoryConfigUseCase {
    HomeVideoCategoryConfig execute();
}
