package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.EmailTemplate;

import java.util.List;

public interface GetEmailTemplatesUseCase {
    List<EmailTemplate> execute();
}
