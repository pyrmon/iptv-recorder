-- Add remux options to recording_schedules table
ALTER TABLE recording_schedules ADD COLUMN remux_to_mkv INT NOT NULL DEFAULT 0;
ALTER TABLE recording_schedules ADD COLUMN keep_original_ts INT NOT NULL DEFAULT 1;
