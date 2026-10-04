-- The Terms & Conditions page — a singleton, application-wide content page
-- managed by admins (FUNC_TERMS_MANAGE). One row for the whole application,
-- mirroring privacy_policy_page. Unlike privacy_policy_page, reading it
-- (GET /api/terms) still requires an authenticated caller.
CREATE TABLE terms_page (
    id          UUID          PRIMARY KEY,
    title       VARCHAR(200)  NOT NULL DEFAULT '',
    body        TEXT          NOT NULL DEFAULT '',
    status      VARCHAR(20)   NOT NULL DEFAULT 'draft',
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ
);
