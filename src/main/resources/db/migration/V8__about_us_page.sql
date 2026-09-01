-- The About Us page — a singleton, application-wide content page managed by
-- admins (FUNC_ABOUT_US_MANAGE). One row for the whole application, mirroring
-- home_video_category_config / app_config.
--
-- `blocks` holds the ordered content-block array as a JSON string; this
-- service treats it opaquely (same as skateboard-podcast-be does for a post's
-- blocks), so it is stored as text rather than jsonb — no server-side querying
-- into it, and it keeps the H2-backed tests simple.
CREATE TABLE about_us_page (
    id          UUID          PRIMARY KEY,
    title       VARCHAR(200)  NOT NULL DEFAULT '',
    subtitle    VARCHAR(300),
    status      VARCHAR(20)   NOT NULL DEFAULT 'draft',
    blocks      TEXT          NOT NULL DEFAULT '[]',
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ
);
