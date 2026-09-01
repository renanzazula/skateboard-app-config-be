package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.SaveAboutPageUseCase;
import com.skateboard.appconfig.application.port.out.LoadAboutPagePort;
import com.skateboard.appconfig.application.port.out.SaveAboutPagePort;
import com.skateboard.appconfig.domain.model.AboutPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveAboutPageService implements SaveAboutPageUseCase {

    private final LoadAboutPagePort loadAboutPagePort;
    private final SaveAboutPagePort saveAboutPagePort;

    public SaveAboutPageService(LoadAboutPagePort loadAboutPagePort, SaveAboutPagePort saveAboutPagePort) {
        this.loadAboutPagePort = loadAboutPagePort;
        this.saveAboutPagePort = saveAboutPagePort;
    }

    @Override
    @Transactional
    public AboutPage execute(Command command) {
        AboutPage page = loadAboutPagePort.find().orElseGet(AboutPage::createEmpty);
        page.update(command.title(), command.subtitle(), command.status(), command.blocks(), command.adminId());
        return saveAboutPagePort.save(page);
    }
}
