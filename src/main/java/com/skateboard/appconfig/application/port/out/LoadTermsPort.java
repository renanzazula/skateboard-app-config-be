package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.Terms;

import java.util.Optional;

public interface LoadTermsPort {
    Optional<Terms> find();
}
