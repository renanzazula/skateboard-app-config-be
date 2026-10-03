-- The Privacy Policy page — a singleton, application-wide content page
-- managed by admins (FUNC_PRIVACY_POLICY_MANAGE). One row for the whole
-- application, mirroring about_us_page. Unlike about_us_page, reading the
-- published row (GET /api/privacy-policy) requires no authentication at
-- all — the App Store / Play Store listing links to it directly.
CREATE TABLE privacy_policy_page (
    id          UUID          PRIMARY KEY,
    title       VARCHAR(200)  NOT NULL DEFAULT '',
    body        TEXT          NOT NULL DEFAULT '',
    status      VARCHAR(20)   NOT NULL DEFAULT 'draft',
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ
);
