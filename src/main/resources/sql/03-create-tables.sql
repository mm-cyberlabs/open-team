-- Create tables for Open Team application
-- Run after connecting to openteam database and creating schema

-- Set search path
SET search_path TO team_comm;

-- Users table for tracking who updates records
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Team announcements
CREATE TABLE announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT true,
    is_archived BOOLEAN DEFAULT false
);

-- Important target dates and project milestones
CREATE TABLE target_dates (
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

-- Software production deployments
CREATE TABLE deployments (
    id BIGSERIAL PRIMARY KEY,
    release_name VARCHAR(100) NOT NULL,
    version VARCHAR(50) NOT NULL,
    deployment_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    driver_user_id BIGINT REFERENCES users(id),
    release_notes TEXT,
    ticket_number VARCHAR(50),
    documentation_url VARCHAR(500),
    environment VARCHAR(20) DEFAULT 'PRODUCTION' CHECK (environment IN ('DEV', 'STAGING', 'PRODUCTION')),
    status VARCHAR(20) DEFAULT 'PLANNED' CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'ROLLED_BACK')),
    is_archived BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX idx_announcements_created_at ON announcements(created_at DESC);
CREATE INDEX idx_announcements_priority ON announcements(priority);
CREATE INDEX idx_announcements_archived ON announcements(is_archived);
CREATE INDEX idx_target_dates_target_date ON target_dates(target_date);
CREATE INDEX idx_target_dates_archived ON target_dates(is_archived);
CREATE INDEX idx_target_dates_status ON target_dates(status);
CREATE INDEX idx_target_dates_project_name ON target_dates(project_name);
CREATE INDEX idx_deployments_datetime ON deployments(deployment_datetime DESC);
CREATE INDEX idx_deployments_status ON deployments(status);
CREATE INDEX idx_deployments_archived ON deployments(is_archived);
CREATE INDEX idx_deployments_ticket_number ON deployments(ticket_number);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to all tables
CREATE TRIGGER update_announcements_updated_at BEFORE UPDATE ON announcements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_target_dates_updated_at BEFORE UPDATE ON target_dates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_deployments_updated_at BEFORE UPDATE ON deployments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Grant privileges on all created objects
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA team_comm TO openteam_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA team_comm TO openteam_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA team_comm TO openteam_user;