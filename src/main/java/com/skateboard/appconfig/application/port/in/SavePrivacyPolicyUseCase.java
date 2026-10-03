package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.PrivacyPolicy;

public interface SavePrivacyPolicyUseCase {

    record Command(String adminId, String title, String body, PrivacyPolicy.Status status) {}

    PrivacyPolicy execute(Command command);
}
