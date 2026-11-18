-- ==============================================================================
-- Flyway Migration V8: Clean All Mock / Sample Student Records from Database
-- ==============================================================================

-- Remove attendance summary & record rows
DELETE FROM attendance_records;
DELETE FROM attendance_summary;

-- Remove all student profile references
DELETE FROM student_profiles;

-- Remove all student records
DELETE FROM students;

-- Remove user authentication accounts belonging to students
DELETE FROM users WHERE role = 'STUDENT';
