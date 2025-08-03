-- Create tables for Open Team application
-- Run after connecting to openteam database and creating schema

-- Set search path
SET search_path TO team_comm;
-- Workspaces table for multi-tenant support
CREATE TABLE workspaces (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Users table for tracking who updates records
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'USER')),
    workspace_id BIGINT REFERENCES workspaces(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP WITH TIME ZONE,
    CONSTRAINT user_workspace_role_check CHECK (
        (role = 'SUPER_ADMIN' AND workspace_id IS NULL) OR
        (role IN ('ADMIN', 'USER') AND workspace_id IS NOT NULL)
    )
);

-- User sessions for authentication tracking
CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Team announcements
CREATE TABLE announcements (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT true,
    is_archived BOOLEAN DEFAULT false,
    expiration_date timestamp with time zone
);

-- Important target dates and project milestones
CREATE TABLE target_dates (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
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
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id),
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

-- Deployment comments table
CREATE TABLE deployment_comments (
    id BIGSERIAL PRIMARY KEY,
    deployment_id BIGINT NOT NULL REFERENCES deployments(id) ON DELETE CASCADE,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id)
);

-- Create indexes for better performance
-- Workspace indexes
CREATE INDEX idx_workspaces_name ON workspaces(name);
CREATE INDEX idx_workspaces_active ON workspaces(is_active);

-- User indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_workspace_id ON users(workspace_id);
CREATE INDEX idx_users_active ON users(is_active);

-- Session indexes
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_expires ON user_sessions(expires_at);
CREATE INDEX idx_user_sessions_active ON user_sessions(is_active);

-- Workspace-specific indexes for data isolation
CREATE INDEX idx_announcements_workspace_id ON announcements(workspace_id);
CREATE INDEX idx_announcements_created_at ON announcements(created_at DESC);
CREATE INDEX idx_announcements_priority ON announcements(priority);
CREATE INDEX idx_announcements_archived ON announcements(is_archived);

CREATE INDEX idx_target_dates_workspace_id ON target_dates(workspace_id);
CREATE INDEX idx_target_dates_target_date ON target_dates(target_date);
CREATE INDEX idx_target_dates_archived ON target_dates(is_archived);
CREATE INDEX idx_target_dates_status ON target_dates(status);
CREATE INDEX idx_target_dates_project_name ON target_dates(project_name);

CREATE INDEX idx_deployments_workspace_id ON deployments(workspace_id);
CREATE INDEX idx_deployments_datetime ON deployments(deployment_datetime DESC);
CREATE INDEX idx_deployments_status ON deployments(status);
CREATE INDEX idx_deployments_archived ON deployments(is_archived);
CREATE INDEX idx_deployments_ticket_number ON deployments(ticket_number);

CREATE INDEX idx_deployment_comments_deployment_id ON deployment_comments(deployment_id);
CREATE INDEX idx_deployment_comments_created_at ON deployment_comments(created_at DESC);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to all tables
CREATE TRIGGER update_workspaces_updated_at BEFORE UPDATE ON workspaces
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

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