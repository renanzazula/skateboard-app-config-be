package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.AboutPage;

import java.util.List;
import java.util.Map;

public interface SaveAboutPageUseCase {

    record Command(String adminId, String title, String subtitle, AboutPage.Status status,
                   List<Map<String, Object>> blocks) {}

    AboutPage execute(Command command);
}
