package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.PrivacyPolicy;

import java.util.Optional;

public interface LoadPrivacyPolicyPort {
    Optional<PrivacyPolicy> find();
}
