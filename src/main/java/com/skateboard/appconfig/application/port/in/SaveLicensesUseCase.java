package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Licenses;

public interface SaveLicensesUseCase {

    record Command(String adminId, String title, String body, Licenses.Status status) {}

    Licenses execute(Command command);
}
