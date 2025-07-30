-- Create database and user for Open Team application
-- Run this script as PostgreSQL superuser (postgres)

-- Create database
CREATE DATABASE openteam;

-- Create user
CREATE USER openteam_user WITH PASSWORD 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE openteam TO openteam_user;

-- Connect to the database and grant schema privileges
\c openteam;
GRANT ALL ON SCHEMA public TO openteam_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO openteam_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO openteam_user;