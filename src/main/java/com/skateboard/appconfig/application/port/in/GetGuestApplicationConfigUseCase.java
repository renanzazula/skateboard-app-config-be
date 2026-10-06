package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.GuestApplicationConfig;

/**
 * Reads the Guest Application settings. Shared by both the admin endpoint
 * (full settings) and the public endpoint (availability only) — the
 * controller projects into two different response DTOs, mirroring
 * GetHomeFeaturedPlayerConfigUseCase's single read backing two callers.
 */
public interface GetGuestApplicationConfigUseCase {
    GuestApplicationConfig execute();
}
