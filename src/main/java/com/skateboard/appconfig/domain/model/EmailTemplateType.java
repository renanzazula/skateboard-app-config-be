package com.skateboard.appconfig.domain.model;

import java.util.Set;

/**
 * Stable key for every email use case (doc §8:
 * .docs/README-email-customization.md on skateboard-fe). Each constant
 * declares the variables its template may reference — validated by
 * {@link EmailTemplate#update} — and the copy a brand-new (type, language)
 * row is seeded with.
 */
public enum EmailTemplateType {

    GUEST_APPLICATION_RECEIVED(
            Set.of("name"),
            "We've received your podcast guest application",
            "Thank you for your interest in joining our podcast, {{name}}! We've received your application "
                    + "and will contact you as soon as possible."),

    GUEST_APPLICATION_ADMIN_NOTIFICATION(
            Set.of("name", "email", "message"),
            "New podcast guest application from {{name}}",
            "{{name}} ({{email}}) applied to be a podcast guest.\n\n{{message}}\n\nReview it in the admin panel.");

    private final Set<String> allowedVariables;
    private final String defaultSubject;
    private final String defaultBody;

    EmailTemplateType(Set<String> allowedVariables, String defaultSubject, String defaultBody) {
        this.allowedVariables = allowedVariables;
        this.defaultSubject = defaultSubject;
        this.defaultBody = defaultBody;
    }

    public Set<String> allowedVariables() {
        return allowedVariables;
    }

    public String defaultSubject() {
        return defaultSubject;
    }

    public String defaultBody() {
        return defaultBody;
    }
}
