package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.Licenses;

public interface SaveLicensesPort {
    Licenses save(Licenses page);
}
