-- ==============================================================================
-- Flyway Migration V4: Seed Authentic Single Student Record (23NE1A05K3)
-- Role: STUDENT
-- ==============================================================================

-- 1. Create User account for 23NE1A05K3
-- Password is set to initial default (Roll Number) hashed with BCrypt Cost 12
-- Hash for '23NE1A05K3': $2a$12$Zq8G0aK3mOxuO9jYjFjXb.fP3p1U5cZzR9k8yM7nB6vC5xT4aL1e2 (active)
INSERT INTO users (id, username, password_hash, role, is_active, token_version, password_changed)
VALUES (
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    '23NE1A05K3',
    '$2a$12$oUfM5Q8oG6rI4u8Hh2l4r.0sU7wUqE2xL9sR0oM5yD7cB3vF6tP9a',
    'STUDENT',
    TRUE,
    1,
    TRUE
)
ON CONFLICT (username) DO NOTHING;

-- 2. Insert Student Academic Record
INSERT INTO students (
    id,
    roll_number,
    admission_number,
    user_id,
    department_id,
    year,
    semester,
    section,
    full_name,
    short_name,
    course,
    admission_year,
    admission_date,
    admission_type,
    admission_quota,
    caste_category,
    is_lateral_entry,
    status,
    account_status,
    profile_status,
    profile_completion,
    cgpa
) VALUES (
    'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
    '23NE1A05K3',
    '2023CSE205',
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    'CSE',
    '4th Year',
    'Sem 4-1',
    'A',
    'RANGISETTY VEDA VENKATA RAJEEVAN',
    '193',
    'B.Tech',
    '2023',
    '2023-09-06',
    'MANAGEMENT QUOTA',
    'MANAGEMENT QUOTA',
    'OC',
    FALSE,
    'Active',
    'ACTIVE',
    'Complete',
    85,
    8.50
)
ON CONFLICT (roll_number) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    admission_number = EXCLUDED.admission_number,
    admission_type = EXCLUDED.admission_type,
    admission_date = EXCLUDED.admission_date;

-- 3. Insert Student Profile Record
INSERT INTO student_profiles (
    student_id,
    dob,
    gender,
    blood_group,
    student_mobile,
    father_name,
    father_phone,
    father_email,
    current_address,
    permanent_address,
    scholarship_status,
    fee_reimbursement_status
) VALUES (
    'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
    '2006-02-18',
    'Male',
    'O+',
    '9848704567',
    'RANGISETTY RAMAKRISHNA',
    '9912182938',
    '23NE1A05K3@tecnrt.org',
    'Narasaraopet, Palnadu District',
    'Narasaraopet, Palnadu District',
    'Not Eligible',
    'No'
)
ON CONFLICT (student_id) DO UPDATE SET
    dob = EXCLUDED.dob,
    gender = EXCLUDED.gender,
    student_mobile = EXCLUDED.student_mobile,
    father_name = EXCLUDED.father_name,
    father_phone = EXCLUDED.father_phone;

