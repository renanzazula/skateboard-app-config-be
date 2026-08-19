CREATE TABLE home_featured_player_config (
    id             UUID          PRIMARY KEY,
    enabled        BOOLEAN       NOT NULL DEFAULT FALSE,
    content_source VARCHAR(30),
    content_id     VARCHAR(150),
    player_type    VARCHAR(20)   NOT NULL DEFAULT 'MINI',
    position       VARCHAR(20)   NOT NULL DEFAULT 'BOTTOM',
    updated_by     VARCHAR(100),
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ
);
