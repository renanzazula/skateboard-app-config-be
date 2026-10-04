package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.GuestApplicationConfig;

public interface LoadGuestApplicationConfigPort {

    /** Returns the singleton row, creating (and persisting) defaults if none exists yet. */
    GuestApplicationConfig getOrCreate();
}
