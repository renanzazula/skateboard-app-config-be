package com.skateboard.appconfig.domain.exception;

public class EmailTemplateNotFoundException extends RuntimeException {

    public EmailTemplateNotFoundException(String id) {
        super("Email template not found: " + id);
    }
}
