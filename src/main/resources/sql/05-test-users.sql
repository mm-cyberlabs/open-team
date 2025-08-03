-- Test users with proper BCrypt password hashes
-- Run this after the sample data to set up test credentials

SET search_path TO team_comm;

-- Update existing users with proper BCrypt hashes
-- Password: "admin123" for all admin users
-- Password: "password" for all regular users

UPDATE users SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeL5V8hN7fJ8LRNl.3k8HYE8.M.FQWu3e' WHERE username = 'sys_admin';      -- Password: admin123
UPDATE users SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeL5V8hN7fJ8LRNl.3k8HYE8.M.FQWu3e' WHERE username = 'eng_admin';     -- Password: admin123
UPDATE users SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeL5V8hN7fJ8LRNl.3k8HYE8.M.FQWu3e' WHERE username = 'mkt_admin';     -- Password: admin123
UPDATE users SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeL5V8hN7fJ8LRNl.3k8HYE8.M.FQWu3e' WHERE username = 'ops_admin';     -- Password: admin123

UPDATE users SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.' WHERE username = 'jdoe';         -- Password: password
UPDATE users SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.' WHERE username = 'msmith';       -- Password: password
UPDATE users SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.' WHERE username = 'rjohnson';     -- Password: password
UPDATE users SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.' WHERE username = 'alee';         -- Password: password

-- Verify the updates
SELECT username, full_name, role, 
       CASE WHEN workspace_id IS NULL THEN 'System-wide' ELSE (SELECT name FROM workspaces w WHERE w.id = u.workspace_id) END as workspace
FROM users u 
ORDER BY role DESC, username;