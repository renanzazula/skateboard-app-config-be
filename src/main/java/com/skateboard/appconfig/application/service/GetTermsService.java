package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetTermsUseCase;
import com.skateboard.appconfig.application.port.out.LoadTermsPort;
import com.skateboard.appconfig.domain.model.Terms;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GetTermsService implements GetTermsUseCase {

    private final LoadTermsPort loadTermsPort;

    public GetTermsService(LoadTermsPort loadTermsPort) {
        this.loadTermsPort = loadTermsPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Terms> execute(boolean includeDraft) {
        return loadTermsPort.find()
                .filter(page -> includeDraft || page.isPublished());
    }
}
