package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.SaveLicensesUseCase;
import com.skateboard.appconfig.application.port.out.LoadLicensesPort;
import com.skateboard.appconfig.application.port.out.SaveLicensesPort;
import com.skateboard.appconfig.domain.model.Licenses;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveLicensesService implements SaveLicensesUseCase {

    private final LoadLicensesPort loadLicensesPort;
    private final SaveLicensesPort saveLicensesPort;

    public SaveLicensesService(LoadLicensesPort loadLicensesPort, SaveLicensesPort saveLicensesPort) {
        this.loadLicensesPort = loadLicensesPort;
        this.saveLicensesPort = saveLicensesPort;
    }

    @Override
    @Transactional
    public Licenses execute(Command command) {
        Licenses page = loadLicensesPort.find().orElseGet(Licenses::createEmpty);
        page.update(command.title(), command.body(), command.status(), command.adminId());
        return saveLicensesPort.save(page);
    }
}
