package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Terms;

public interface SaveTermsUseCase {

    record Command(String adminId, String title, String body, Terms.Status status) {}

    Terms execute(Command command);
}
