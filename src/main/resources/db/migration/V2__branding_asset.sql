CREATE TABLE branding_asset (
    id            UUID          PRIMARY KEY,
    name          VARCHAR(150)  NOT NULL,
    object_key    TEXT          NOT NULL,
    content_type  VARCHAR(100)  NOT NULL,
    version       INT           NOT NULL DEFAULT 1,
    updated_by    VARCHAR(100),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_branding_asset_name UNIQUE (name)
);
