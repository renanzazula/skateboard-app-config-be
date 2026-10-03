package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.PrivacyPolicy;

public interface SavePrivacyPolicyPort {
    PrivacyPolicy save(PrivacyPolicy page);
}
