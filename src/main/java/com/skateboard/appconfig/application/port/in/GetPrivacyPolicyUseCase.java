package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.PrivacyPolicy;

import java.util.Optional;

/**
 * Reads the Privacy Policy page. {@code includeDraft=false} returns the page
 * only when it is published (the anonymous, pre-auth viewer);
 * {@code includeDraft=true} returns it whatever its status (the admin
 * editor, gated by FUNC_PRIVACY_POLICY_MANAGE at the controller). Empty when
 * no page exists yet.
 */
public interface GetPrivacyPolicyUseCase {
    Optional<PrivacyPolicy> execute(boolean includeDraft);
}
