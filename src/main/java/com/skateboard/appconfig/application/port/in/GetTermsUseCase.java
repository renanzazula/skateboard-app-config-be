package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Terms;

import java.util.Optional;

/**
 * Reads the Terms &amp; Conditions page. {@code includeDraft=false} returns
 * the page only when it is published (the standard viewer);
 * {@code includeDraft=true} returns it whatever its status (the admin
 * editor, gated by FUNC_TERMS_MANAGE at the controller). Empty when no page
 * exists yet.
 */
public interface GetTermsUseCase {
    Optional<Terms> execute(boolean includeDraft);
}
