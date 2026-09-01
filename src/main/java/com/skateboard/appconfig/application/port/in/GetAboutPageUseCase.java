package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.AboutPage;

import java.util.Optional;

/**
 * Reads the About Us page. {@code includeDraft=false} returns the page only
 * when it is published (the standard-user viewer); {@code includeDraft=true}
 * returns it whatever its status (the admin editor, gated by
 * FUNC_ABOUT_US_MANAGE at the controller). Empty when no page exists yet.
 */
public interface GetAboutPageUseCase {
    Optional<AboutPage> execute(boolean includeDraft);
}
