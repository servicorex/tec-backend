-- ==============================================================================
-- Flyway Migration V3: Add Academic Regulation Support to Subjects Table
-- Supports JNTUK R16, R19, R20, R23 and custom departmental regulations
-- ==============================================================================

ALTER TABLE subjects ADD COLUMN IF NOT EXISTS regulation VARCHAR(20) DEFAULT 'R20' NOT NULL;
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS subject_type VARCHAR(50) DEFAULT 'Theory' NOT NULL;

-- Drop single subject_code unique constraint to allow different regulations to share codes if needed
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS subjects_subject_code_key;
ALTER TABLE subjects ADD CONSTRAINT uq_subject_code_regulation UNIQUE (subject_code, regulation);

CREATE INDEX IF NOT EXISTS idx_subjects_dept_sem_reg ON subjects(department_id, semester, regulation);

