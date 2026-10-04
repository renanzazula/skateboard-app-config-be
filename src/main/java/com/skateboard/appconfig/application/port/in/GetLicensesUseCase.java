package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.Licenses;

import java.util.Optional;

/**
 * Reads the Open-source Licenses page. {@code includeDraft=false} returns
 * the page only when it is published (the standard viewer);
 * {@code includeDraft=true} returns it whatever its status (the admin
 * editor, gated by FUNC_LICENSES_MANAGE at the controller). Empty when no
 * page exists yet.
 */
public interface GetLicensesUseCase {
    Optional<Licenses> execute(boolean includeDraft);
}
