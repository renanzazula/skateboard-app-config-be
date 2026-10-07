package com.skateboard.appconfig.application.port.in;

import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.domain.model.EmailTemplateType;

public interface GetEmailTemplateUseCase {

    record Query(EmailTemplateType type, String language) {}

    EmailTemplate execute(Query query);
}
