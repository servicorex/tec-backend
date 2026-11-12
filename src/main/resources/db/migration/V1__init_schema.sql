-- ==============================================================================
-- Flyway Migration V1: Institutional Schema Core for Tirumala Engineering College
-- Supported Roles: STUDENT, HOD, ADMIN
-- ==============================================================================

-- 1. Users Table (Central Identity & Authentication)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,      -- Roll Number (STUDENT) or Email/ID (HOD / ADMIN)
    password_hash VARCHAR(255) NOT NULL,       -- BCrypt hash (Cost Factor 12)
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'HOD', 'ADMIN')),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    token_version INT DEFAULT 1 NOT NULL,      -- Incremented on password change for JWT revocation
    password_changed BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- 2. Departments Table
CREATE TABLE departments (
    id VARCHAR(10) PRIMARY KEY,                -- 'CSE', 'ECE', 'MECH', 'CIVIL', 'IT', etc.
    name VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,                 -- e.g. '05'
    established_year INT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. HOD Assignments Table
CREATE TABLE hod_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    department_id VARCHAR(10) NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT TRUE NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_hod_dept UNIQUE (department_id),
    CONSTRAINT uq_hod_user_dept UNIQUE (user_id, department_id)
);

CREATE INDEX idx_hod_assignments_user ON hod_assignments(user_id);

-- 4. Sections Table (Dynamic Section Management)
CREATE TABLE sections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    department_id VARCHAR(10) NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    section_name VARCHAR(5) NOT NULL,          -- 'A', 'B', 'C', 'D'
    academic_year VARCHAR(20),                 -- e.g. '2026-2027'
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_dept_section_year UNIQUE (department_id, section_name, academic_year)
);

CREATE INDEX idx_sections_dept ON sections(department_id);

-- 5. Students Table (Official Academic Identifiers)
CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roll_number VARCHAR(20) UNIQUE NOT NULL,   -- e.g. '21TML0501'
    admission_number VARCHAR(30) UNIQUE,       -- e.g. 'ADM-2021-0501'
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    department_id VARCHAR(10) NOT NULL REFERENCES departments(id),
    year VARCHAR(20) NOT NULL,                 -- '1st Year', '2nd Year', '3rd Year', '4th Year'
    semester VARCHAR(30) NOT NULL,             -- 'Sem 1-1', 'Sem 1-2', ..., 'Sem 4-2'
    section VARCHAR(5) NOT NULL,               -- 'A', 'B', 'C'
    full_name VARCHAR(255) NOT NULL,
    short_name VARCHAR(100),
    course VARCHAR(50) DEFAULT 'B.Tech' NOT NULL,
    admission_year VARCHAR(10),                -- '2021'
    admission_date DATE,
    admission_type VARCHAR(50),                -- 'Convener Quota (EAPCET)'
    admission_quota VARCHAR(50),
    caste_category VARCHAR(20),
    is_lateral_entry BOOLEAN DEFAULT FALSE NOT NULL,
    status VARCHAR(20) DEFAULT 'Active' NOT NULL, -- 'Active', 'Detained', 'Graduated'
    account_status VARCHAR(20) DEFAULT 'PENDING' NOT NULL, -- 'PENDING', 'PROVISIONED', 'ACTIVE'
    profile_status VARCHAR(20) DEFAULT 'Incomplete' NOT NULL, -- 'Complete', 'Incomplete'
    profile_completion INT DEFAULT 0 NOT NULL,
    cgpa DECIMAL(3,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Composite index for fast sub-5ms class roster loads
CREATE INDEX idx_students_dept_year_sec ON students(department_id, year, section);
CREATE INDEX idx_students_roll ON students(roll_number);
CREATE INDEX idx_students_user_id ON students(user_id);

-- 6. Student Profiles Table (Extended Self-Managed Data)
CREATE TABLE student_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID UNIQUE NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    dob DATE,
    gender VARCHAR(10),
    blood_group VARCHAR(5),
    student_mobile VARCHAR(20),
    father_name VARCHAR(255),
    father_phone VARCHAR(20),
    father_email VARCHAR(255),
    father_occupation VARCHAR(255),
    mother_name VARCHAR(255),
    mother_occupation VARCHAR(255),
    guardian_name VARCHAR(255),
    guardian_contact VARCHAR(20),
    current_address TEXT,
    permanent_address TEXT,
    scholarship_status VARCHAR(50),
    fee_reimbursement_status VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 7. Subjects Table (Course Syllabus Master)
CREATE TABLE subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_code VARCHAR(20) UNIQUE NOT NULL,  -- e.g. 'CS701PC'
    subject_name VARCHAR(255) NOT NULL,
    department_id VARCHAR(10) NOT NULL REFERENCES departments(id),
    year VARCHAR(20) NOT NULL,
    semester VARCHAR(30) NOT NULL,
    credits DECIMAL(3,1) DEFAULT 3.0 NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_subjects_dept_sem ON subjects(department_id, semester);

-- 8. Faculty Table (Directory & Teaching Records)
CREATE TABLE faculty (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id VARCHAR(30) UNIQUE NOT NULL,      -- e.g. 'TEC-FAC-042'
    department_id VARCHAR(10) NOT NULL REFERENCES departments(id),
    full_name VARCHAR(255) NOT NULL,
    designation VARCHAR(100),                  -- 'Professor & HOD', 'Associate Professor'
    qualification VARCHAR(255),
    experience VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(20),
    workload_hours INT DEFAULT 16,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_faculty_dept ON faculty(department_id);

-- 9. Faculty Subject Allocations Table
CREATE TABLE faculty_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    faculty_id UUID NOT NULL REFERENCES faculty(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    department_id VARCHAR(10) NOT NULL REFERENCES departments(id),
    year VARCHAR(20) NOT NULL,
    semester VARCHAR(30) NOT NULL,
    section VARCHAR(5) NOT NULL,
    academic_year VARCHAR(20),
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 10. Attendance Periods Table (Upload Batches & Concurrency Locks)
CREATE TABLE attendance_periods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    department_id VARCHAR(10) NOT NULL REFERENCES departments(id),
    year VARCHAR(20) NOT NULL,
    section VARCHAR(5) NOT NULL,
    period_name VARCHAR(100) NOT NULL,         -- 'October 2026' or 'Semester 4-1'
    period_type VARCHAR(20) DEFAULT 'monthly', -- 'monthly', 'cumulative', 'custom'
    academic_year VARCHAR(20),
    status VARCHAR(20) DEFAULT 'DRAFT' NOT NULL, -- 'DRAFT', 'PUBLISHED'
    file_name VARCHAR(255),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_dept_year_sec_period UNIQUE (department_id, year, section, period_name)
);

CREATE INDEX idx_attendance_periods_dept ON attendance_periods(department_id);

-- 11. Attendance Records Table (Granular Subject-Wise Matrix)
CREATE TABLE attendance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendance_period_id UUID NOT NULL REFERENCES attendance_periods(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    classes_held INT DEFAULT 0 NOT NULL,
    classes_attended INT DEFAULT 0 NOT NULL,
    classes_absent INT DEFAULT 0 NOT NULL,
    attendance_percentage DECIMAL(5,2) NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_period_student_subject UNIQUE (attendance_period_id, student_id, subject_id)
);

CREATE INDEX idx_attendance_records_student ON attendance_records(student_id);

-- 12. Attendance Summary Table (Pre-Calculated for O(1) Dashboard Read)
CREATE TABLE attendance_summary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendance_period_id UUID NOT NULL REFERENCES attendance_periods(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    total_classes_held INT DEFAULT 0 NOT NULL,
    total_classes_attended INT DEFAULT 0 NOT NULL,
    total_classes_absent INT DEFAULT 0 NOT NULL,
    overall_percentage DECIMAL(5,2) NOT NULL,
    status VARCHAR(30) DEFAULT 'ELIGIBLE' NOT NULL, -- 'ELIGIBLE', 'CONDONATION', 'DETAINED', 'ELIGIBLE_BY_OVERRIDE'
    is_overridden BOOLEAN DEFAULT FALSE NOT NULL,
    override_reason TEXT,
    overridden_by UUID REFERENCES users(id),
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_period_student_summary UNIQUE (attendance_period_id, student_id)
);

CREATE INDEX idx_attendance_summary_student ON attendance_summary(student_id);

-- 13. Notifications Table (In-App Header Bell Alerts)
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    department_id VARCHAR(10) REFERENCES departments(id),
    target_year VARCHAR(20),                   -- 'IV Year' or 'ALL'
    target_section VARCHAR(5),                 -- 'A' or 'ALL'
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_notifications_dept ON notifications(department_id);

-- 14. Audit Logs Table (Immutable Paper Trail)
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    role VARCHAR(20),
    action VARCHAR(100) NOT NULL,              -- 'ATTENDANCE_PUBLISH', 'DETENTION_OVERRIDE', 'STUDENT_PROVISION'
    details TEXT,
    affected_entity VARCHAR(50),
    affected_id VARCHAR(100),
    ip_address VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Database-level trigger to enforce immutable audit records
CREATE OR REPLACE FUNCTION prevent_audit_log_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit log records are immutable and cannot be updated or deleted.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_protect_audit_logs
BEFORE UPDATE OR DELETE ON audit_logs
FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_modification();

