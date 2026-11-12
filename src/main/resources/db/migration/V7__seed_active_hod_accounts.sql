-- ==============================================================================
-- Flyway Migration V7: Seed Active HOD User Accounts and Assignments
-- ==============================================================================

-- 1. Insert remaining active engineering departments
INSERT INTO departments (id, name, code, established_year)
VALUES
    ('AIML', 'Computer Science and Engineering (AI & ML)', '42', 2020),
    ('AIDS', 'Computer Science and Engineering (AI & DS)', '54', 2021),
    ('EEE', 'Electrical and Electronics Engineering', '02', 2008)
ON CONFLICT (id) DO NOTHING;

-- 2. Insert HOD users into users table
INSERT INTO users (id, username, password_hash, role, is_active, token_version, password_changed)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'hod.cse@tecnrt.org', '$2a$12$gU/PGorcBKbchecH.X8wG.qZZRsnMGTwx18OmbTp.3YWjtVwKR54K', 'HOD', TRUE, 1, TRUE),
    ('b0000000-0000-0000-0000-000000000002', 'hod.aiml@tecnrt.org', '$2a$12$f0hBInCIKCJwk.9FRUXihuuBMDXxwEHhwrV0zU1Hhgmup9163seFa', 'HOD', TRUE, 1, TRUE),
    ('b0000000-0000-0000-0000-000000000003', 'hod.aids@tecnrt.org', '$2a$12$pwquTJn68w0QZSpydH7oreTzCQe4YZC5aRY0o3vpryp.r4KRdinuK', 'HOD', TRUE, 1, TRUE),
    ('b0000000-0000-0000-0000-000000000004', 'hod.it@tecnrt.org', '$2a$12$Jb8ZpCwUUaNqr/u5rHjeG.1wzGPHEoE3vk7aiQKwFpAlJmBuAeqke', 'HOD', TRUE, 1, TRUE),
    ('b0000000-0000-0000-0000-000000000005', 'hod.ece@tecnrt.org', '$2a$12$cdGfJgax/sQ8VMVEXGiN6OnjI9BoikQ/1ipH5CFCoiR/P.Q63KEMm', 'HOD', TRUE, 1, TRUE),
    ('b0000000-0000-0000-0000-000000000006', 'hod.eee@tecnrt.org', '$2a$12$yroDi3XK31nEeWCf1vP0Iu79zVbmMahyHAPjtRx/vCmBF..4p1lhC', 'HOD', TRUE, 1, TRUE)
ON CONFLICT (username) DO NOTHING;

-- 3. Link HOD users to their departments
INSERT INTO hod_assignments (id, user_id, department_id, is_primary)
VALUES
    ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'CSE', TRUE),
    ('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'AIML', TRUE),
    ('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003', 'AIDS', TRUE),
    ('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004', 'IT', TRUE),
    ('c0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000005', 'ECE', TRUE),
    ('c0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000006', 'EEE', TRUE)
ON CONFLICT (department_id) DO NOTHING;

