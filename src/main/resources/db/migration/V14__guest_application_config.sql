-- Guest Application feature settings — a singleton, application-wide row
-- (no tenant scoping), mirroring home_featured_player_config /
-- home_video_category_config. Recipient IDs are Keycloak user IDs, stored
-- as-is: this service does not call skateboard-user-be to validate them
-- (see api/openapi.yaml's updateGuestApplicationSettings description).
CREATE TABLE guest_application_config (
    id                   UUID          PRIMARY KEY,
    enabled              BOOLEAN       NOT NULL DEFAULT FALSE,
    confirmation_subject VARCHAR(200)  NOT NULL DEFAULT '',
    confirmation_body    TEXT          NOT NULL DEFAULT '',
    updated_by           VARCHAR(100),
    created_at           TIMESTAMPTZ   NOT NULL,
    updated_at           TIMESTAMPTZ
);

CREATE TABLE guest_application_config_recipient (
    config_id     UUID          NOT NULL REFERENCES guest_application_config (id) ON DELETE CASCADE,
    recipient_id  UUID          NOT NULL,
    PRIMARY KEY (config_id, recipient_id)
);
