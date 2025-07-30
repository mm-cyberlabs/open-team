-- Insert sample data for Open Team application
-- Run after creating tables

SET search_path TO team_comm;

-- Insert sample users
INSERT INTO users (username, full_name, email) VALUES
('admin', 'System Administrator', 'admin@company.com'),
('jdoe', 'John Doe', 'john.doe@company.com'),
('msmith', 'Mary Smith', 'mary.smith@company.com'),
('rjohnson', 'Robert Johnson', 'robert.johnson@company.com'),
('alee', 'Alice Lee', 'alice.lee@company.com');

-- Insert sample announcements
INSERT INTO announcements (title, content, priority, created_by, updated_by) VALUES
('System Maintenance Window', 
 'Scheduled maintenance will occur this weekend from Saturday 2:00 AM to Sunday 6:00 AM EST. All systems will be temporarily unavailable during this time. Please plan accordingly and save your work before the maintenance window begins.',
 'HIGH', 1, 1),

('New Security Policy Implementation', 
 'We are implementing new security guidelines effective immediately. All team members must review the updated security documentation and complete the mandatory training by end of this week. Please check your email for the training link.',
 'NORMAL', 1, 1),

('Q4 Performance Reviews', 
 'Quarter 4 performance reviews are now open. Please complete your self-assessments by December 15th. Manager reviews will follow, with final meetings scheduled for the last week of December.',
 'NORMAL', 2, 2),

('Emergency Contact Information Update', 
 'Please update your emergency contact information in the HR system. This is required for all employees and must be completed by the end of this month.',
 'URGENT', 1, 1);

-- Insert sample activities
INSERT INTO activities (title, description, activity_type, scheduled_date, location, created_by, updated_by) VALUES
('Sprint Planning Meeting', 
 'Planning session for the next development sprint. We will review backlog items, estimate story points, and plan the sprint goals.',
 'MEETING', 
 CURRENT_TIMESTAMP + INTERVAL '1 day', 
 'Conference Room A', 2, 2),

('Security Awareness Training', 
 'Mandatory security awareness training for all development team members. This session will cover best practices for secure coding and data handling.',
 'TRAINING', 
 CURRENT_TIMESTAMP + INTERVAL '3 days', 
 'Training Room B', 1, 1),

('Team Building Event', 
 'Annual team building event with activities and lunch. Attendance is optional but highly encouraged.',
 'EVENT', 
 CURRENT_TIMESTAMP + INTERVAL '7 days', 
 'Central Park Pavilion', 3, 3),

('Architecture Review', 
 'Monthly architecture review meeting to discuss system design decisions and technical debt.',
 'MEETING', 
 CURRENT_TIMESTAMP + INTERVAL '14 days', 
 'Virtual Meeting Room', 4, 4),

('Code Review Workshop', 
 'Workshop on effective code review practices and tools. Open to all developers.',
 'TRAINING', 
 CURRENT_TIMESTAMP + INTERVAL '21 days', 
 'Lab 1', 5, 5);

-- Insert sample deployments
INSERT INTO deployments (release_name, version, deployment_datetime, driver_user_id, release_notes, environment, status, created_by, updated_by) VALUES
('Customer Portal', 
 'v2.1.0', 
 CURRENT_TIMESTAMP + INTERVAL '2 days', 
 3, 
 'Major release including bug fixes, performance improvements, and new user dashboard features. This release addresses several critical issues reported by customers and includes enhanced search functionality.',
 'PRODUCTION', 
 'PLANNED', 2, 2),

('API Gateway', 
 'v1.5.2', 
 CURRENT_TIMESTAMP - INTERVAL '1 day', 
 4, 
 'Security patches and new REST endpoints for mobile app integration. Includes rate limiting improvements and enhanced authentication mechanisms.',
 'PRODUCTION', 
 'COMPLETED', 3, 3),

('Data Analytics Service', 
 'v3.0.0', 
 CURRENT_TIMESTAMP + INTERVAL '7 days', 
 5, 
 'Complete rewrite of the analytics engine with improved performance and new reporting capabilities. Includes real-time dashboard updates and export functionality.',
 'STAGING', 
 'PLANNED', 4, 4),

('Mobile App Backend', 
 'v1.2.1', 
 CURRENT_TIMESTAMP - INTERVAL '3 days', 
 2, 
 'Hotfix for critical push notification issues. Resolves problems with notification delivery and improves battery usage optimization.',
 'PRODUCTION', 
 'COMPLETED', 2, 2),

('Notification Service', 
 'v0.9.0', 
 CURRENT_TIMESTAMP, 
 3, 
 'Beta release of the new notification service with email and SMS capabilities. Currently undergoing final testing before production deployment.',
 'STAGING', 
 'IN_PROGRESS', 5, 5),

('Payment Processing', 
 'v2.3.1', 
 CURRENT_TIMESTAMP - INTERVAL '7 days', 
 4, 
 'Security updates and compliance improvements for PCI DSS requirements. Includes enhanced fraud detection and transaction monitoring.',
 'PRODUCTION', 
 'COMPLETED', 1, 1);