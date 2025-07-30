-- Create schema for Open Team application
-- Connect to openteam database first: \c openteam;

-- Create schema
CREATE SCHEMA IF NOT EXISTS team_comm;

-- Grant all privileges on schema to application user
GRANT ALL ON SCHEMA team_comm TO openteam_user;
GRANT USAGE ON SCHEMA team_comm TO openteam_user;