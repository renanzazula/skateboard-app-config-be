-- The Open-source Licenses page — a singleton, application-wide content page
-- managed by admins (FUNC_LICENSES_MANAGE). One row for the whole
-- application, mirroring privacy_policy_page. Reading it (GET /api/licenses)
-- requires an authenticated caller, same as terms_page.
CREATE TABLE licenses_page (
    id          UUID          PRIMARY KEY,
    title       VARCHAR(200)  NOT NULL DEFAULT '',
    body        TEXT          NOT NULL DEFAULT '',
    status      VARCHAR(20)   NOT NULL DEFAULT 'draft',
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ
);
