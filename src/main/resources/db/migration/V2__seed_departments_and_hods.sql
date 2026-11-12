-- ==============================================================================
-- Flyway Migration V2: Seed Baseline Departments for Tirumala Engineering College
-- Idempotent inserts with conflict resolution
-- ==============================================================================

-- 1. Insert Initial Academic Departments
INSERT INTO departments (id, name, code, established_year)
VALUES
    ('CSE', 'Computer Science and Engineering', '05', 2008),
    ('ECE', 'Electronics and Communication Engineering', '04', 2008),
    ('MECH', 'Mechanical Engineering', '03', 2009),
    ('CIVIL', 'Civil Engineering', '01', 2009),
    ('IT', 'Information Technology', '12', 2008)
ON CONFLICT (id) DO UPDATE
SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    established_year = EXCLUDED.established_year;

-- Note on HOD Account Seeding:
-- Real institutional HOD identities, authorized email addresses, and secure initial credentials
-- must be formally provided before inserting into `users` and `hod_assignments`.
-- In accordance with project security policies:
-- 1. No plaintext passwords or dummy/invented credentials are inserted here.
-- 2. HOD accounts will be provisioned once verified institutional email addresses are confirmed.
-- 3. Admin accounts and administrative provisioning functionality are reserved for Phase 4.

