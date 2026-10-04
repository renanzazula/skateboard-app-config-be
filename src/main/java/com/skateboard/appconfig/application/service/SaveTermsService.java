package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.SaveTermsUseCase;
import com.skateboard.appconfig.application.port.out.LoadTermsPort;
import com.skateboard.appconfig.application.port.out.SaveTermsPort;
import com.skateboard.appconfig.domain.model.Terms;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveTermsService implements SaveTermsUseCase {

    private final LoadTermsPort loadTermsPort;
    private final SaveTermsPort saveTermsPort;

    public SaveTermsService(LoadTermsPort loadTermsPort, SaveTermsPort saveTermsPort) {
        this.loadTermsPort = loadTermsPort;
        this.saveTermsPort = saveTermsPort;
    }

    @Override
    @Transactional
    public Terms execute(Command command) {
        Terms page = loadTermsPort.find().orElseGet(Terms::createEmpty);
        page.update(command.title(), command.body(), command.status(), command.adminId());
        return saveTermsPort.save(page);
    }
}
