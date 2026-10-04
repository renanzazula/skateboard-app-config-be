package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.Terms;

public interface SaveTermsPort {
    Terms save(Terms page);
}
