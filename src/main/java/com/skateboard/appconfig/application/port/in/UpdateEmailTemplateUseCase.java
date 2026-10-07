package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.EmailTemplate;

import java.util.UUID;

public interface UpdateEmailTemplateUseCase {

    record Command(String adminId, UUID id, String subject, String body, boolean enabled) {}

    EmailTemplate execute(Command command);
}
