package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetLicensesUseCase;
import com.skateboard.appconfig.application.port.out.LoadLicensesPort;
import com.skateboard.appconfig.domain.model.Licenses;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GetLicensesService implements GetLicensesUseCase {

    private final LoadLicensesPort loadLicensesPort;

    public GetLicensesService(LoadLicensesPort loadLicensesPort) {
        this.loadLicensesPort = loadLicensesPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Licenses> execute(boolean includeDraft) {
        return loadLicensesPort.find()
                .filter(page -> includeDraft || page.isPublished());
    }
}
