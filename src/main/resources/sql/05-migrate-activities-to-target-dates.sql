-- Migration script to convert activities table to target_dates table
-- Run this on existing databases that have the activities table

SET search_path TO team_comm;

-- Create target_dates table
CREATE TABLE IF NOT EXISTS target_dates (
    id BIGSERIAL PRIMARY KEY,
    project_name VARCHAR(200) NOT NULL,
    task_name VARCHAR(200) NOT NULL,
    target_date TIMESTAMP WITH TIME ZONE NOT NULL,
    driver_user_id BIGINT REFERENCES users(id),
    documentation_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    is_archived BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Migrate existing activities data if the table exists
DO $$
BEGIN
    -- Check if activities table exists
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'team_comm' AND table_name = 'activities') THEN
        -- Migrate data from activities to target_dates
        INSERT INTO target_dates (project_name, task_name, target_date, driver_user_id, status, is_archived, created_at, updated_at, created_by, updated_by)
        SELECT 
            COALESCE(activity_type, 'General') AS project_name,
            title AS task_name,
            COALESCE(scheduled_date, created_at) AS target_date,
            created_by AS driver_user_id, -- Use created_by as driver since activities didn't have driver
            CASE 
                WHEN is_active = false OR is_archived = true THEN 'COMPLETED'
                ELSE 'PENDING'
            END AS status,
            COALESCE(is_archived, false) AS is_archived,
            created_at,
            updated_at,
            created_by,
            updated_by
        FROM activities;
        
        RAISE NOTICE 'Successfully migrated % rows from activities to target_dates', (SELECT COUNT(*) FROM activities);
        
        -- Rename the old table to keep as backup
        ALTER TABLE activities RENAME TO activities_backup_migration;
        RAISE NOTICE 'Renamed activities table to activities_backup_migration';
    ELSE
        RAISE NOTICE 'Activities table does not exist, skipping migration';
    END IF;
END $$;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_target_dates_target_date ON target_dates(target_date);
CREATE INDEX IF NOT EXISTS idx_target_dates_archived ON target_dates(is_archived);
CREATE INDEX IF NOT EXISTS idx_target_dates_status ON target_dates(status);
CREATE INDEX IF NOT EXISTS idx_target_dates_project_name ON target_dates(project_name);

-- Create trigger for updated_at
CREATE TRIGGER update_target_dates_updated_at BEFORE UPDATE ON target_dates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Grant privileges
GRANT ALL PRIVILEGES ON target_dates TO openteam_user;
GRANT ALL PRIVILEGES ON SEQUENCE target_dates_id_seq TO openteam_user;

RAISE NOTICE 'Migration completed successfully';