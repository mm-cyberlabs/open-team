-- Insert sample data for Open Team application
-- Run after creating tables

SET search_path TO team_comm;

-- Insert sample workspaces
INSERT INTO workspaces (name, description) VALUES
('Engineering', 'Software development and engineering teams'),
('Marketing', 'Marketing and customer engagement teams'),
('Operations', 'IT operations and infrastructure teams');

-- Insert sample users with authentication data
-- Note: These are example password hashes - in production, use proper password hashing
INSERT INTO users (username, full_name, email, password_hash, role, workspace_id) VALUES
('sys_admin', 'System Administrator', 'admin@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1YqCkbS7C8.example.hash', 'SUPER_ADMIN', NULL),
('eng_admin', 'Engineering Admin', 'eng.admin@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye2YqCkbS7C8.example.hash', 'ADMIN', 1),
('mkt_admin', 'Marketing Admin', 'mkt.admin@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye3YqCkbS7C8.example.hash', 'ADMIN', 2),
('ops_admin', 'Operations Admin', 'ops.admin@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye4YqCkbS7C8.example.hash', 'ADMIN', 3),
('jdoe', 'John Doe', 'john.doe@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye5YqCkbS7C8.example.hash', 'USER', 1),
('msmith', 'Mary Smith', 'mary.smith@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6YqCkbS7C8.example.hash', 'USER', 2),
('rjohnson', 'Robert Johnson', 'robert.johnson@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7YqCkbS7C8.example.hash', 'USER', 3),
('alee', 'Alice Lee', 'alice.lee@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye8YqCkbS7C8.example.hash', 'USER', 1);

-- Insert sample announcements
INSERT INTO announcements (workspace_id, title, content, priority, created_by, updated_by) VALUES
(1, 'System Maintenance Window', 
 'Scheduled maintenance will occur this weekend from Saturday 2:00 AM to Sunday 6:00 AM EST. All systems will be temporarily unavailable during this time. Please plan accordingly and save your work before the maintenance window begins.',
 'HIGH', 2, 2),

(1, 'New Security Policy Implementation', 
 'We are implementing new security guidelines effective immediately. All team members must review the updated security documentation and complete the mandatory training by end of this week. Please check your email for the training link.',
 'NORMAL', 2, 2),

(2, 'Q4 Performance Reviews', 
 'Quarter 4 performance reviews are now open. Please complete your self-assessments by December 15th. Manager reviews will follow, with final meetings scheduled for the last week of December.',
 'NORMAL', 3, 3),

(3, 'Emergency Contact Information Update', 
 'Please update your emergency contact information in the HR system. This is required for all employees and must be completed by the end of this month.',
 'URGENT', 4, 4);

-- Insert sample target dates
INSERT INTO target_dates (workspace_id, project_name, task_name, target_date, driver_user_id, documentation_url, status, created_by, updated_by) VALUES
(1, 'Customer Portal Redesign', 
 'Complete UI/UX mockups and user testing',
 CURRENT_TIMESTAMP + INTERVAL '7 days', 
 5, 
 'https://wiki.company.com/projects/portal-redesign',
 'IN_PROGRESS', 5, 5),

(1, 'Security Compliance Initiative', 
 'Implement multi-factor authentication',
 CURRENT_TIMESTAMP + INTERVAL '14 days', 
 8, 
 'https://docs.company.com/security/mfa-implementation',
 'PENDING', 2, 2),

(3, 'Data Migration Project', 
 'Complete legacy system data migration',
 CURRENT_TIMESTAMP + INTERVAL '21 days', 
 7, 
 'https://confluence.company.com/data-migration',
 'PENDING', 4, 4),

(1, 'Mobile App v2.0', 
 'Beta release for internal testing',
 CURRENT_TIMESTAMP + INTERVAL '10 days', 
 8, 
 'https://github.com/company/mobile-app/milestone/5',
 'IN_PROGRESS', 8, 8),

(2, 'API Documentation Update', 
 'Complete API documentation overhaul',
 CURRENT_TIMESTAMP - INTERVAL '2 days', 
 6, 
 'https://api-docs.company.com/v3',
 'COMPLETED', 6, 6),

(3, 'Performance Optimization', 
 'Database query optimization phase 1',
 CURRENT_TIMESTAMP + INTERVAL '28 days', 
 7, 
 'https://wiki.company.com/performance/db-optimization',
 'PENDING', 7, 7);

-- Insert sample deployments
INSERT INTO deployments (workspace_id, release_name, version, deployment_datetime, driver_user_id, release_notes, environment, status, created_by, updated_by) VALUES
(1, 'Customer Portal', 
 'v2.1.0', 
 CURRENT_TIMESTAMP + INTERVAL '2 days', 
 5, 
 'Major release including bug fixes, performance improvements, and new user dashboard features. This release addresses several critical issues reported by customers and includes enhanced search functionality.',
 'PRODUCTION', 
 'PLANNED', 5, 5),

(1, 'API Gateway', 
 'v1.5.2', 
 CURRENT_TIMESTAMP - INTERVAL '1 day', 
 8, 
 'Security patches and new REST endpoints for mobile app integration. Includes rate limiting improvements and enhanced authentication mechanisms.',
 'PRODUCTION', 
 'COMPLETED', 8, 8),

(2, 'Data Analytics Service', 
 'v3.0.0', 
 CURRENT_TIMESTAMP + INTERVAL '7 days', 
 6, 
 'Complete rewrite of the analytics engine with improved performance and new reporting capabilities. Includes real-time dashboard updates and export functionality.',
 'STAGING', 
 'PLANNED', 6, 6),

(1, 'Mobile App Backend', 
 'v1.2.1', 
 CURRENT_TIMESTAMP - INTERVAL '3 days', 
 5, 
 'Hotfix for critical push notification issues. Resolves problems with notification delivery and improves battery usage optimization.',
 'PRODUCTION', 
 'COMPLETED', 5, 5),

(3, 'Notification Service', 
 'v0.9.0', 
 CURRENT_TIMESTAMP, 
 7, 
 'Beta release of the new notification service with email and SMS capabilities. Currently undergoing final testing before production deployment.',
 'STAGING', 
 'IN_PROGRESS', 7, 7),

(3, 'Payment Processing', 
 'v2.3.1', 
 CURRENT_TIMESTAMP - INTERVAL '7 days', 
 7, 
 'Security updates and compliance improvements for PCI DSS requirements. Includes enhanced fraud detection and transaction monitoring.',
 'PRODUCTION', 
 'COMPLETED', 4, 4);