package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.GetAboutPageUseCase;
import com.skateboard.appconfig.application.port.out.LoadAboutPagePort;
import com.skateboard.appconfig.domain.model.AboutPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GetAboutPageService implements GetAboutPageUseCase {

    private final LoadAboutPagePort loadAboutPagePort;

    public GetAboutPageService(LoadAboutPagePort loadAboutPagePort) {
        this.loadAboutPagePort = loadAboutPagePort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AboutPage> execute(boolean includeDraft) {
        return loadAboutPagePort.find()
                .filter(page -> includeDraft || page.isPublished());
    }
}
