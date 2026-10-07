package com.skateboard.appconfig.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Admin-configurable subject/body for one (type, language) combination (doc
 * §6/§9). Unlike the singleton configs in this service, there are several of
 * these — one per {@link EmailTemplateType} × supported language — but every
 * combination is guaranteed to exist from the caller's perspective:
 * {@code createDefault} materializes a row the first time a combination is
 * read, so there is no separate "create" use case, only get and update.
 *
 * <p>Placeholders use Mustache's {@code {{variable}}} syntax (doc §12), not
 * the single-brace {@code {name}} the old GuestApplicationConfig fields used
 * — skateboard-notification-be's renderer is being switched to Mustache in
 * the same effort this model was introduced for.
 */
public class EmailTemplate {

    public static final List<String> SUPPORTED_LANGUAGES = List.of("en", "es", "pt");

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^{}]*)}}");

    private final UUID id;
    private final EmailTemplateType type;
    private final String language;
    private String subject;
    private String body;
    private boolean enabled;
    private final Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    private EmailTemplate(UUID id, EmailTemplateType type, String language, String subject, String body,
                           boolean enabled, Instant createdAt, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.type = type;
        this.language = language;
        this.subject = subject;
        this.body = body;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    /** A (type, language) combination that has never been configured — seeded with the type's default copy. */
    public static EmailTemplate createDefault(EmailTemplateType type, String language) {
        if (type == null) {
            throw new IllegalArgumentException("An email template type is required.");
        }
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        Instant now = Instant.now();
        return new EmailTemplate(UUID.randomUUID(), type, language, type.defaultSubject(), type.defaultBody(),
                true, now, null, null);
    }

    public static EmailTemplate reconstitute(UUID id, EmailTemplateType type, String language, String subject,
                                              String body, boolean enabled, Instant createdAt, Instant updatedAt,
                                              String updatedBy) {
        return new EmailTemplate(id, type, language, subject, body, enabled, createdAt, updatedAt, updatedBy);
    }

    /**
     * Only the placeholders this template's {@link #type} declares as
     * supported are accepted — any other {@code {{placeholder}}} is
     * rejected, so a later render never hits a variable it can't resolve.
     */
    public void update(String subject, String body, boolean enabled, String adminId) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("An email subject is required.");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("An email body is required.");
        }
        validatePlaceholders(subject);
        validatePlaceholders(body);

        this.subject = subject.strip();
        this.body = body.strip();
        this.enabled = enabled;
        this.updatedAt = Instant.now();
        this.updatedBy = adminId;
    }

    private void validatePlaceholders(String text) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            if (!type.allowedVariables().contains(placeholder)) {
                throw new IllegalArgumentException("Unsupported variable {{" + placeholder + "}} for " + type
                        + " — allowed: " + type.allowedVariables());
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public EmailTemplateType getType() {
        return type;
    }

    public String getLanguage() {
        return language;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
