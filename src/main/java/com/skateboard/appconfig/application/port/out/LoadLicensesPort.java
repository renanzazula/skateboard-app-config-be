package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.Licenses;

import java.util.Optional;

public interface LoadLicensesPort {
    Optional<Licenses> find();
}
