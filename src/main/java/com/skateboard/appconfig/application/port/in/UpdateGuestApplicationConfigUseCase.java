package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.GuestApplicationConfig;

import java.util.Set;
import java.util.UUID;

public interface UpdateGuestApplicationConfigUseCase {

    record Command(String adminId, boolean enabled, Set<UUID> recipientIds, String confirmationSubject,
                    String confirmationBody) {}

    GuestApplicationConfig execute(Command command);
}
