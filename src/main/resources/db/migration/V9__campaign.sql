-- Startup Campaigns (spec §14). A campaign is a real multi-row aggregate
-- (unlike the singleton configs): one campaign row, 1–3 ordered screen rows,
-- and one background media-asset row per screen that has an uploaded image.
--
-- Only admin-driven status is stored (DRAFT/PUBLISHED/PAUSED/ARCHIVED); whether
-- a published campaign is currently SCHEDULED/ACTIVE/EXPIRED is derived from
-- start_at/end_at at read time, never written by a job.
--
-- As with branding / About Us, only the bare object key is stored for images;
-- a presigned URL is generated fresh in the REST layer on every read.

CREATE TABLE campaign (
    id                   UUID          PRIMARY KEY,
    name                 VARCHAR(150)  NOT NULL,
    description          VARCHAR(1000),
    status               VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    start_at             TIMESTAMPTZ   NOT NULL,
    end_at               TIMESTAMPTZ   NOT NULL,
    priority             INT           NOT NULL DEFAULT 0,
    audience             VARCHAR(20)   NOT NULL,
    frequency_type       VARCHAR(20)   NOT NULL,
    max_displays_per_day INT,
    created_by           VARCHAR(100),
    created_at           TIMESTAMPTZ   NOT NULL,
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,
    published_by         VARCHAR(100),
    published_at         TIMESTAMPTZ
);

-- Runtime eligibility lookup: published + inside the schedule window.
CREATE INDEX idx_campaign_runtime ON campaign (status, start_at, end_at);

CREATE TABLE campaign_media_asset (
    id            UUID          PRIMARY KEY,
    storage_key   TEXT          NOT NULL,
    version       INT           NOT NULL DEFAULT 1,
    mime_type     VARCHAR(100),
    width         INT,
    height        INT,
    size_bytes    BIGINT,
    focal_point_x DOUBLE PRECISION,
    focal_point_y DOUBLE PRECISION,
    created_at    TIMESTAMPTZ   NOT NULL
);

CREATE TABLE campaign_screen (
    id                  UUID          PRIMARY KEY,
    campaign_id         UUID          NOT NULL REFERENCES campaign (id) ON DELETE CASCADE,
    position            INT           NOT NULL,
    duration_seconds    INT           NOT NULL,
    layout_type         VARCHAR(20)   NOT NULL DEFAULT 'FULL_BACKGROUND',
    -- The screen owns its asset's lifecycle through the app layer (a replaced
    -- image is a new row + a new version); no ON DELETE rule needed because the
    -- app deletes the asset when it detaches or removes the screen.
    background_asset_id UUID          REFERENCES campaign_media_asset (id),
    background_color    VARCHAR(30),
    title               VARCHAR(255),
    description         VARCHAR(1000),
    text_alignment      VARCHAR(10)   NOT NULL DEFAULT 'CENTER',
    title_size          VARCHAR(20)   NOT NULL DEFAULT 'LARGE',
    description_size    VARCHAR(20)   NOT NULL DEFAULT 'MEDIUM',
    text_color          VARCHAR(30),
    overlay_opacity     DOUBLE PRECISION,
    close_enabled       BOOLEAN       NOT NULL DEFAULT FALSE,
    close_after_seconds INT,
    action_type         VARCHAR(20)   NOT NULL DEFAULT 'NONE',
    action_label        VARCHAR(150),
    action_target       VARCHAR(2048)
);

-- Positions are kept unique and contiguous by the domain aggregate; this index
-- is just for the FK join (no UNIQUE constraint on (campaign_id, position) so a
-- reorder does not need deferred-constraint handling).
CREATE INDEX idx_campaign_screen_campaign_id ON campaign_screen (campaign_id);

-- Analytics events (spec §18, implementation plan gap #3). Deliberately has no
-- FK to campaign/screen: a signed-out client may report an event for a campaign
-- that has since been deleted, and such events are logged, not rejected. No PII.
-- The endpoint that writes this table ships in a later step.
CREATE TABLE campaign_event (
    id            UUID          PRIMARY KEY,
    campaign_id   UUID          NOT NULL,
    screen_id     UUID,
    event_type    VARCHAR(40)   NOT NULL,
    occurred_at   TIMESTAMPTZ   NOT NULL,
    platform      VARCHAR(50),
    app_version   VARCHAR(50),
    action_target VARCHAR(2048)
);

CREATE INDEX idx_campaign_event_campaign_id ON campaign_event (campaign_id);
