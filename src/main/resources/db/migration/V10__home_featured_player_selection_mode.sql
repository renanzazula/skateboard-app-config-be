-- Existing rows backfill to MANUAL, preserving today's admin-picks-the-video
-- behavior unchanged for every configuration created before this migration.
ALTER TABLE home_featured_player_config
    ADD COLUMN selection_mode VARCHAR(20) NOT NULL DEFAULT 'MANUAL';
