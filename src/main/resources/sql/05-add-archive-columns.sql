-- Migration script to add is_archived columns for soft delete functionality
-- Run this script to update existing database schema

SET search_path TO team_comm;

-- Add is_archived column to deployments table
ALTER TABLE deployments ADD COLUMN IF NOT EXISTS is_archived BOOLEAN DEFAULT false;

-- Add is_archived column to announcements table (if not exists - some may have is_active instead)
ALTER TABLE announcements ADD COLUMN IF NOT EXISTS is_archived BOOLEAN DEFAULT false;

-- Add is_archived column to activities table (if not exists - some may have is_active instead)
ALTER TABLE activities ADD COLUMN IF NOT EXISTS is_archived BOOLEAN DEFAULT false;

-- Update existing records to set is_archived = NOT is_active where is_active exists
-- This handles the case where tables have is_active instead of is_archived

-- For announcements
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'team_comm' 
               AND table_name = 'announcements' 
               AND column_name = 'is_active') THEN
        UPDATE announcements SET is_archived = NOT is_active WHERE is_archived IS NULL;
    END IF;
END $$;

-- For activities  
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'team_comm' 
               AND table_name = 'activities' 
               AND column_name = 'is_active') THEN
        UPDATE activities SET is_archived = NOT is_active WHERE is_archived IS NULL;
    END IF;
END $$;

-- Create indexes for better performance on archive queries
CREATE INDEX IF NOT EXISTS idx_announcements_archived ON announcements(is_archived);
CREATE INDEX IF NOT EXISTS idx_activities_archived ON activities(is_archived);
CREATE INDEX IF NOT EXISTS idx_deployments_archived ON deployments(is_archived);

-- Update comments
COMMENT ON COLUMN announcements.is_archived IS 'Soft delete flag - true if record is archived/deleted';
COMMENT ON COLUMN activities.is_archived IS 'Soft delete flag - true if record is archived/deleted';
COMMENT ON COLUMN deployments.is_archived IS 'Soft delete flag - true if record is archived/deleted';

-- Show results
SELECT 'Migration completed successfully. Archive columns added to all tables.' as status;