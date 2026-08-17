-- updated_at must reflect only an explicit admin change (mirrors AppConfig's
-- domain-specific *_updated_at columns), not "row last written" — the
-- default-creation save on first GET/PUT was incorrectly stamping this with
-- now(), making a never-configured row appear as if an admin had just saved it.
ALTER TABLE home_video_category_config
    ALTER COLUMN updated_at DROP NOT NULL;
