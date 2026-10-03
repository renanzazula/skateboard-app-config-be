package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetPrivacyPolicyUseCase;
import com.skateboard.appconfig.application.port.out.LoadPrivacyPolicyPort;
import com.skateboard.appconfig.domain.model.PrivacyPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GetPrivacyPolicyService implements GetPrivacyPolicyUseCase {

    private final LoadPrivacyPolicyPort loadPrivacyPolicyPort;

    public GetPrivacyPolicyService(LoadPrivacyPolicyPort loadPrivacyPolicyPort) {
        this.loadPrivacyPolicyPort = loadPrivacyPolicyPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PrivacyPolicy> execute(boolean includeDraft) {
        return loadPrivacyPolicyPort.find()
                .filter(page -> includeDraft || page.isPublished());
    }
}
