-- Add completion_status column to existing past_recordings table
ALTER TABLE past_recordings ADD COLUMN completion_status TEXT;
-- Update existing records with default value
UPDATE past_recordings SET completion_status = 'COMPLETED' WHERE completion_status IS NULL;