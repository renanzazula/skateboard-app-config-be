CREATE TABLE app_config (
    id                             UUID        PRIMARY KEY,
    login_background_key           TEXT,
    login_background_version       INT         NOT NULL DEFAULT 0,
    login_background_updated_at    TIMESTAMPTZ,
    app_logo_key                   TEXT,
    app_logo_version                INT         NOT NULL DEFAULT 0,
    app_logo_updated_at            TIMESTAMPTZ,
    updated_by                     VARCHAR(100),
    created_at                     TIMESTAMPTZ NOT NULL,
    updated_at                     TIMESTAMPTZ NOT NULL
);
