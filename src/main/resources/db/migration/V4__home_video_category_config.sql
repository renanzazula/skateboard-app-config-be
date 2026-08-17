CREATE TABLE home_video_category_config (
    id            UUID          PRIMARY KEY,
    mode          VARCHAR(20)   NOT NULL DEFAULT 'ALL',
    updated_by    VARCHAR(100),
    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NOT NULL
);

CREATE TABLE home_video_category_config_category (
    config_id     UUID          NOT NULL REFERENCES home_video_category_config (id) ON DELETE CASCADE,
    category_id   VARCHAR(150)  NOT NULL,
    PRIMARY KEY (config_id, category_id)
);
