package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.SavePrivacyPolicyUseCase;
import com.skateboard.appconfig.application.port.out.LoadPrivacyPolicyPort;
import com.skateboard.appconfig.application.port.out.SavePrivacyPolicyPort;
import com.skateboard.appconfig.domain.model.PrivacyPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavePrivacyPolicyService implements SavePrivacyPolicyUseCase {

    private final LoadPrivacyPolicyPort loadPrivacyPolicyPort;
    private final SavePrivacyPolicyPort savePrivacyPolicyPort;

    public SavePrivacyPolicyService(LoadPrivacyPolicyPort loadPrivacyPolicyPort, SavePrivacyPolicyPort savePrivacyPolicyPort) {
        this.loadPrivacyPolicyPort = loadPrivacyPolicyPort;
        this.savePrivacyPolicyPort = savePrivacyPolicyPort;
    }

    @Override
    @Transactional
    public PrivacyPolicy execute(Command command) {
        PrivacyPolicy page = loadPrivacyPolicyPort.find().orElseGet(PrivacyPolicy::createEmpty);
        page.update(command.title(), command.body(), command.status(), command.adminId());
        return savePrivacyPolicyPort.save(page);
    }
}
