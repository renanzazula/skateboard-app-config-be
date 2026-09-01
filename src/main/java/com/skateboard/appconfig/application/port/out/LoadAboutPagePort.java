package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.AboutPage;

import java.util.Optional;

public interface LoadAboutPagePort {
    Optional<AboutPage> find();
}
