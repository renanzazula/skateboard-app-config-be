-- Email Template foundation (.docs/README-email-customization.md §6/§9 on
-- skateboard-fe): one admin-configurable row per (type, language)
-- combination, replacing the flat confirmation_subject/confirmation_body
-- pair that used to live on guest_application_config. {name} (single-brace)
-- becomes {{name}} (double-brace, Mustache syntax) — see EmailTemplate's
-- placeholder validation.

CREATE TABLE email_template (
    id          UUID          PRIMARY KEY,
    type        VARCHAR(50)   NOT NULL,
    language    VARCHAR(5)    NOT NULL,
    subject     VARCHAR(200)  NOT NULL,
    body        TEXT          NOT NULL,
    enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ,
    CONSTRAINT uq_email_template_type_language UNIQUE (type, language)
);

-- One-time backfill: preserve whatever an admin already configured for the
-- applicant confirmation email before its columns are dropped below. Every
-- other (type, language) combination is materialized lazily by the
-- application on first read (EmailTemplate.createDefault), same as every
-- other config singleton in this service — this is the one row with real
-- prior data to carry forward.
INSERT INTO email_template (id, type, language, subject, body, enabled, updated_by, created_at, updated_at)
SELECT gen_random_uuid(),
       'GUEST_APPLICATION_RECEIVED',
       'en',
       REPLACE(confirmation_subject, '{name}', '{{name}}'),
       REPLACE(confirmation_body, '{name}', '{{name}}'),
       TRUE,
       updated_by,
       created_at,
       updated_at
FROM guest_application_config;

ALTER TABLE guest_application_config
    DROP COLUMN confirmation_subject,
    DROP COLUMN confirmation_body;
