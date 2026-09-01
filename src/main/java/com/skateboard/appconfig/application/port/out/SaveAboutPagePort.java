package com.skateboard.appconfig.application.port.out;

import com.skateboard.appconfig.domain.model.AboutPage;

public interface SaveAboutPagePort {
    AboutPage save(AboutPage page);
}
