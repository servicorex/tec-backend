# BACKEND_AUDIT.md - Tirumala Engineering College Management Portal

**Audit Date:** 2026-10-31  
**Repository:** D:\Tirumala websote\tirumala frontend  
**Audit Scope:** Complete frontend audit for backend development planning  
**Framework:** React 19.2.8 + Vite 8.3.0 (Frontend)  
**Proposed Backend:** Java 21 + Spring Boot, REST API, Supabase PostgreSQL + Auth + Storage  

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Audit Scope and Methodology](#2-audit-scope-and-methodology)
3. [Repository Inventory](#3-repository-inventory)
4. [Existing Architecture](#4-existing-architecture)
5. [Route and Page Inventory](#5-route-and-page-inventory)
6. [Student Portal Audit](#6-student-portal-audit)
7. [HOD Portal Audit](#7-hod-portal-audit)
8. [Public Website Audit](#8-public-website-audit)
9. [Functional Workflow Audit](#9-functional-workflow-audit)
10. [Frontend Data and State Audit](#10-frontend-data-and-state-audit)
11. [Authentication and Authorization Requirements](#11-authentication-and-authorization-requirements)
12. [Database Requirements and Proposed Schema](#12-database-requirements-and-proposed-schema)
13. [API Requirements and Proposed API Contract](#13-api-requirements-and-proposed-api-contract)
14. [Excel and CSV Import Requirements](#14-excel-and-csv-import-requirements)
15. [File Storage Requirements](#15-file-storage-requirements)
16. [Security Requirements](#16-security-requirements)
17. [Non-Functional Requirements](#17-non-functional-requirements)
18. [Spring Boot Backend Architecture Blueprint](#18-spring-boot-backend-architecture-blueprint)
19. [Local Development and Integration Plan](#19-local-development-and-integration-plan)
20. [Testing and End-to-End Strategy](#20-testing-and-end-to-end-strategy)
21. [Requirements Gap Register](#21-requirements-gap-register)
22. [Technical Risks and Recommendations](#22-technical-risks-and-recommendations)
23. [Backend Development Roadmap](#23-backend-development-roadmap)
24. [Unresolved Questions and Decisions Required](#24-unresolved-questions-and-decisions-required)
25. [Final Handoff Summary for Backend Developer](#25-final-handoff-summary-for-backend-developer)

---

## 1. Executive Summary

This audit examines the existing Tirumala Engineering College (TEC) Management Portal frontend codebase to determine backend requirements for integration with **Java 21  + Spring Boot**, **Supabase PostgreSQL/Auth/Storage**, **Spring Security**, **Spring Data JPA + Hibernate**, **Flyway**, **OpenAPI/Swagger**, and **Google Cloud Run** deployment.

**Key Findings:**
- **Frontend Stack:** React 19.2.8, Vite 8.3.0, React Router DOM 7.18.4, Lucide React icons
- **Role Model:** Currently supports `public`, `student`, `hod` roles. Faculty Portal and Principal Portal were **removed from scope** per code comments (lines 25, 74-78 in AuthContext.jsx), though Faculty data exists for HOD Faculty Directory. **Admin role is NOT implemented in frontend** - it must be added as a backend-provisioned role.
- **Architecture:** Client-side state management via React Context (`AuthContext.jsx`) with extensive mock data in `src/data/`. **All operations are currently mocked/client-side only** - no actual API calls detected.
- **Core Features Identified:** Student registration by HOD (roll number + name), student profile completion (direct save without HOD approval), attendance management with CSV/Excel upload for multiple subjects, section management (A, B, C + add), faculty directory/workload, HOD dashboard with departmental isolation, 8-semester academic structure.
- **Critical Gap:** Complete absence of backend integration. All data is stored in React state/localContext and mock data files. No HTTP client (axios/fetch wrappers), no API service layer, no environment config for backend URLs found.

**Status:** This is purely a frontend mock/demo application. Full backend development required. The frontend uses client-side simulation only. 

**Integration Model (Required):**
1. React frontend uses Supabase Auth SDK for authentication
2. Supabase issues access/refresh tokens (JWT)
3. Frontend sends `Authorization: Bearer <token>` to Spring Boot
4. Spring Boot validates JWT and maps to application user/roles
5. All authorization enforced on backend with department/student ownership checks 

---

## 2. Audit Scope and Methodology

**Scope:**
- Complete repository exploration including all pages, components, context, data files
- Route analysis across public website, student portal, HOD portal
- Workflow tracing for authentication, student registration, profile management, attendance
- Data model extraction from mockData.js and AuthContext
- Security/authorization pattern analysis

**Methodology:**
- File system inspection of all source files
- Code reading of AuthContext (1009 lines), mockData (1041 lines), sampleSpreadsheet (558 lines)
- Component analysis for student and HOD portals
- Route discovery from App.jsx and page structure
- Evidence-based labeling (VERIFIED/PARTIALLY VERIFIED/MOCKED/MISSING/UNKNOWN/REQUIRES CONFIRMATION)

---

## 3. Repository Inventory

| Area | Actual Implementation | Evidence | Status |
|---|---|---|---|
| **Frontend Framework** | React 19.2.8 | package.json:14 | VERIFIED |
| **Build Tool** | Vite 8.3.0 | package.json:24, vite.config.js | VERIFIED |
| **Language** | JavaScript/JSX (ES Modules) | package.json:5, file extensions .jsx/.js | VERIFIED |
| **Routing** | React Router DOM 7.18.4 | package.json:16 | VERIFIED |
| **State Management** | React Context API (AuthContext) | src/context/AuthContext.jsx | VERIFIED |
| **UI Icons** | Lucide React 1.57.0 | package.json:13 | VERIFIED |
| **Styling** | CSS files (App.css, index.css) + inline styles | src/App.css, src/index.css | VERIFIED |
| **HTTP Client** | **NONE FOUND** | No axios, fetch wrappers, API service files | MISSING |
| **Authentication** | Custom mock auth via Context (simulated) | AuthContext.jsx loginWithCredentials | MOCKED |
| **Data Layer** | Mock data files only | src/data/mockData.js, sampleSpreadsheet.js | MOCKED |
| **Testing** | None configured | package.json scripts - no test script | MISSING |
| **Linting** | oxlint 1.81.0 | package.json:9,19 | VERIFIED |

**Source Files Inventory:**
- Pages: 15 files (HomePage, AboutPages, AdmissionPages, etc.)
- Components: 35+ files across student/, hod/, common/, layout/, home/
- Context: AuthContext.jsx (single context provider)
- Data: mockData.js, sampleSpreadsheet.js, collegeAuditData.js

---

## 4. Existing Architecture

**Current Architecture (Frontend Only):**
```
┌─────────────────────────────────────────┐
│           React SPA (Vite)               │
├─────────────────────────────────────────┤
│         AuthContext (Global State)       │
│  - currentUser, students, facultyList    │
│  - All CRUD operations (client-side)     │
├─────────────────────────────────────────┤
│              Mock Data Layer             │
│  - INITIAL_STUDENTS, DEPARTMENTS, etc.   │
│  - CSV parsing/validation utilities     │
└─────────────────────────────────────────┘
```

**Key Characteristics:**
- **No backend communication** - All state mutations update React state directly
- **Data persistence:** Lost on page refresh (state resets to initial mock data from imports)
- **Role switching:** Built-in `quickSwitchRole()` for testing (lines 73-119 in AuthContext) - shows "Faculty Portal has been permanently discontinued" warnings
- **Mock authentication:** Accepts roll number + password/temp password logic (lines 122-198)

---

## 5. Route and Page Inventory

Based on exploration of pages and components, routes include (from React Router usage patterns):

| Route Pattern | Page/Component | Role | Purpose | Status |
|---|---|---|---|---|
| `/` | HomePage | Public | College homepage | VERIFIED (static content) |
| `/about/*` | AboutPages | Public | About sections (society, management, principal, etc.) | VERIFIED |
| `/admissions/*` | AdmissionPages | Public | Admissions info | VERIFIED |
| `/departments/:deptCode` | DepartmentDetailPage | Public | Department pages | VERIFIED |
| `/administration/*` | AdministrationPages | Public | Governance pages | VERIFIED |
| `/examination/*` | ExamcellPages | Public | Exam cell pages | VERIFIED |
| `/placement/*` | PlacementPages | Public | Placement info | VERIFIED |
| **Student Portal** (via AuthContext activeTab) | StudentDashboard | Student | Student portal with tabs | VERIFIED - MOCKED |
| **HOD Portal** (via AuthContext activeTab) | HODDashboard | HOD | HOD portal with tabs | VERIFIED - MOCKED |

**Student Portal Tabs (from StudentDashboard.jsx:23-29):**
- `overview`, `profile`, `attendance`, `exams`, `fees`, `settings`

**HOD Portal Tabs (from HODDashboard.jsx:24-33):**
- `department-overview`, `student-records`, `students-classes`, `attendance-upload`, `faculty-management`, `monthly-reports`, `settings`

---

## 6. Student Portal Audit

**Location:** `src/components/student/`  
**Access Control:** Requires `currentUser.role === 'student'` and `isAuthenticated === true`

### Features Identified:

| Feature | Implementation | Data Source | Operations | Status |
|---|---|---|---|---|
| **Login** | `loginWithCredentials` in AuthContext:128-171 | Searches `students` array by roll number | Auth simulation | MOCKED |
| **Dashboard Overview** | StudentOverview.jsx | `students`, `STUDENT_SUBJECTS`, `STUDENT_EXAMS`, `STUDENT_FEES` | Read-only display | MOCKED |
| **Profile Management** | StudentProfile.jsx (859 lines) | Student object in context | `updateStudentProfile()` - saves directly, no HOD approval required per comments | PARTIALLY VERIFIED - MOCKED |
| **Attendance View** | AttendanceView.jsx (428 lines) | `getStudentSemesterAttendance()`, student.monthlyAttendance, semesterAttendance | Read-only with 8-semester navigation | MOCKED |
| **Examinations** | ExaminationsView.jsx | `STUDENT_EXAMS` mock data | Read-only display | MOCKED |
| **Fees** | FeesPaymentView.jsx | `STUDENT_FEES` mock data | Display only, no actual payment | MOCKED |
| **Settings** | StudentSettings.jsx | Current user | Password change via `updatePassword()` | MOCKED |

**Profile Fields (from StudentProfile.jsx and mockData):**
Required fields for completion (mockData:196-210): name, dob, gender, bloodGroup, phone, email, fatherName, fatherPhone, motherName, currentAddress, permanentAddress, admissionYear, admissionType  
Additional fields: guardian details, casteCategory, scholarshipStatus, feeReimbursementStatus

**Key Observation:** Student can edit their own profile and save directly (line 322-363 in AuthContext) - "saves immediately without HOD review" per comment line 321. Profile completion calculated client-side.

---

## 7. HOD Portal Audit

**Location:** `src/components/hod/`  
**Access Control:** Requires `currentUser.role === 'hod'`  
**Department Isolation:** HOD profile contains `department` (branch code like CSE, ECE etc.) from DEPARTMENTS array

### Features Identified:

| Feature | Component | Operations | Status |
|---|---|---|---|
| **HOD Dashboard** | HODDashboard.jsx | Tab navigation | VERIFIED - MOCKED |
| **Department Overview** | HODOverview.jsx | Statistics display | MOCKED |
| **Student Records** | HODStudentRecords.jsx (943+ lines) | Create student, provision accounts, edit profiles, promote students | MOCKED |
| **Section & Student Roster** | HODStudentsAndClasses.jsx | Section management | MOCKED |
| **Attendance Upload** | HODAttendanceUpload.jsx (1288 lines) | CSV upload, validation, publish attendance for section with multiple subjects | MOCKED - Complex implementation |
| **Faculty Directory** | HODFacultyManagement.jsx | Manage faculty list, assign subjects, workload | MOCKED |
| **Monthly Reports** | HODMonthlyReports.jsx | Reports | MOCKED |
| **Settings** | HODSettings.jsx | Settings | MOCKED |

**Critical Workflows:**

1. **Student Registration (HOD creates):** `createStudent()` in AuthContext:245-319
   - Requires rollNumber and name
   - Auto-generates admissionNumber, tempPassword (`TEC#${last4}@2026`)
   - Sets accountStatus to PENDING, profileStatus to INCOMPLETE
   - Branch/year/semester/section from HOD input/context

2. **Bulk Student Import:** `importStudentsToSection()` (AuthContext:627-723)
   - CSV import with 2-column format (Roll Number, Student Name) per sampleSpreadsheet:19-22
   - Validates duplicates, creates accounts as PENDING

3. **Account Provisioning:** `provisionStudentAccount()` (AuthContext:385-412), `batchProvisionAccounts()` (414-442)
   - HOD issues temp password, sets accountStatus to PROVISIONED
   - Student must change password on first login

4. **Student Promotion:** `promoteStudent()` (808-879), `batchPromoteStudents()` (881-952)
   - Uses semester progression logic from mockData
   - Preserves semester attendance history
   - Auto-increments semester/year based on SEMESTER_PROGRESSION

5. **Attendance Upload:** `submitAttendanceUpload()` (445-565)
   - Processes multi-subject CSV per HODAttendanceUpload
   - Updates student attendance records, subject attendance
   - Creates upload history entry, logs action

---

## 8. Public Website Audit

**Location:** `src/pages/`, `src/components/home/`, `src/components/public/`  
**Access:** No authentication required

**Pages Identified:**
- HomePage.jsx - Hero section, principal message, placements, news
- AboutPages.jsx - Society, management, principal, why TEC, best practices, etc. (761 lines)
- AdmissionPages.jsx - Admission procedure, courses, fee details (350 lines)
- DepartmentDetailPage.jsx - Department pages with faculty, labs, syllabus (352 lines)
- AdministrationPages.jsx - Governance, committees, cells (415 lines)
- ExamcellPages.jsx - Exam notifications, results (250 lines)
- PlacementPages.jsx, GalleryPage.jsx, ContactPage.jsx, etc.

**Assessment:** Mostly static informational content. Some links to external sites. No forms requiring backend (contact forms not obviously implemented as functional). Can likely remain static or need minimal backend for dynamic content like notices/announcements.  

**Status:** VERIFIED - Mostly static, some dynamic elements use mock data (ANNOUNCEMENTS in mockData:988-1021)

---

## 9. Functional Workflow Audit

### Workflow 1: Authentication
**Path:** AuthContext.loginWithCredentials (122-198)  
**Flow:** User selects role (student/hod) → enters credentials → validates against mock data → sets currentUser state  
**Issues:** No real auth, faculty disabled, no password reset, no MFA/OTP implementation despite being mentioned in requirements  
**Status:** MOCKED

### Workflow 2: Student Registration by HOD
**Path:** HODStudentRecords → createStudent() (AuthContext:245-319)  
**Flow:** HOD enters roll number + name + class details → validates uniqueness → generates temp password → creates student record with PENDING status  
**Note:** PRD says "Roll Number. Student Name. Branch, academic year, semester and section should derive from the HOD's assigned department and selected academic context." - Current implementation takes year/semester/section as input parameters.  
**Status:** PARTIALLY VERIFIED - MOCKED

### Workflow 3: Student Profile Completion
**Path:** StudentProfile.jsx → updateStudentProfile() (AuthContext:322-363)  
**Flow:** Student fills form → validates client-side → saves directly → calculates completion percentage → updates profileStatus (Complete if all required fields filled)  
**Note:** "The profile should not require a separate HOD approval workflow unless the existing code explicitly implements and the project owner confirms it." - No approval workflow found.  
**Status:** VERIFIED - MOCKED (saves directly as designed)

### Workflow 4: Section Management
**Path:** HODAttendanceUpload → sections state management (lines 45-47)  
**Features:** Default sections A, B, C; can add additional sections dynamically (lines 122-138 in HODAttendanceUpload)  
**Status:** VERIFIED - Client-side only

### Workflow 5: Attendance Management (Critical)
**Path:** HODAttendanceUpload.jsx (1288 lines) + submitAttendanceUpload() (AuthContext:445-565)  
**Features:**
- Multi-subject unified CSV upload (overall attendance format)
- Dynamic subject management per year
- Manual attendance period creation (monthly/cumulative/custom)
- CSV validation via parseAndValidateOverallAttendanceCSV
- Computes overall percentage from subject averages
- Updates student attendance records and subject attendance
- Publishes to student portals

**CSV Format (from sampleSpreadsheet:308-334):**
Headers: `["Roll Number", "Student Name", ...subjectHeaders, "Overall Attendance"]` with subject percentage values like "85%"

**Validation Rules:** Unknown roll numbers, duplicates, out of range percentages (>100 or <0), department/section mismatches  
**Status:** PARTIALLY VERIFIED - MOCKED (complex logic exists)

### Workflow 6: Faculty Directory
**Path:** HODFacultyManagement.jsx, addFacultyMember/updateFacultyMember (AuthContext:759-806)  
**Note:** Faculty Portal/Login removed from scope. Faculty records maintained for directory and workload.  
**Status:** MOCKED

### Workflow 7: Academic Progression/Promotion
**Path:** promoteStudent/batchPromoteStudents (AuthContext:808-952)  
**Uses:** SEMESTER_PROGRESSION from mockData:271-280, normalizeSemesterCode, getSemesterProgressionInfo  
**Status:** MOCKED

---

## 10. Frontend Data and State Audit

### Data Sources

| Data Entity | Source | Type | Persistence | Status |
|---|---|---|---|---|
| **Students** | `INITIAL_STUDENTS` in mockData.js | Array of student objects | React state (AuthContext.students) - resets on refresh | MOCKED |
| **Departments** | `DEPARTMENTS` array | Static config | Imported, read-only | VERIFIED |
| **Faculty** | `FACULTY_MEMBERS` | Array | React state | MOCKED |
| **Attendance Upload History** | `ATTENDANCE_UPLOAD_HISTORY` (empty initially) | Array | React state | MOCKED |
| **Audit Logs** | `AUDIT_LOGS` (empty initially) | Array | React state, logged via logAction() | MOCKED |
| **Exam Timetables** | `INITIAL_EXAM_TIMETABLES` (empty) | Array | React state | MOCKED |
| **College Info** | `COLLEGE_INFO` | Static config | Imported | VERIFIED |

**Data Model Observations:**
- Student object is large with 50+ fields (see mockData:462-766)
- Mixes official fields (rollNumber, admissionNumber) with editable profile fields
- Attendance stored as `attendanceOverall`, `totalClassesHeld/Attended/Absent`, `monthlyAttendance[]`, `subjectAttendance[]`, `semesterAttendance{}` 
- Account states: PENDING, PROVISIONED, ACTIVE
- Profile states: Complete, Incomplete

**Issues:**
- No single source of truth for API contracts
- Field naming inconsistencies possible
- Heavy reliance on mock data structures that need to map to database schema

---

## 11. Authentication and Authorization Requirements

### Current Implementation (MOCKED)
- **Auth Provider:** AuthContext manages auth state
- **Roles:** `public` (default), `student`, `hod` - **faculty and principal removed per requirements**
- **Login:** `loginWithCredentials({role, username, password, branch})` 
  - Student: looks up by rollNumber (case-insensitive), checks accountStatus (PENDING blocked), validates password/tempPassword/initial password (roll number)
  - HOD: looks up department by branch code, creates profile from DEPARTMENTS
- **Session:** Stored in React state only (lost on refresh)
- **Password Change:** `updatePassword()` for students sets password, makes account ACTIVE
- **Logout:** Resets to public state

### Proposed Implementation (Finalised)

#### Architecture Flow
1. **Frontend (React)** uses Supabase Auth SDK for authentication
2. **Users authenticate** via Supabase Auth (email/password or configured providers)
3. **Supabase issues** JWT access token and refresh token
4. **Frontend attaches** `Authorization: Bearer <access_token>` to all API requests
5. **Spring Boot** validates JWT using Supabase's JWKS/public keys or configured secret
6. **Spring Boot resolves** application user from `user_profiles` table mapped by `auth.users.id`
7. **Backend enforces** role-based + resource-based authorization

#### Admin
- **Role:** `ADMIN` (not present in current frontend - **MISSING REQUIREMENT**)
- **Provisioning:** Admin provisions HOD accounts only (no public HOD self-registration)
- **Department Assignment:** Admin assigns department to HOD via `hod_assignments`
- **Account Management:** Admin can activate/deactivate accounts
- **Creation:** Must use secure provisioning workflow (invitation/link-based or direct provisioning with audit log)
- **UI:** Admin interface does **NOT** exist in current frontend - must be built or accessed via API/admin tools

#### HOD
- **Authentication:** Institutional email + password via Supabase Auth
- **Role:** `HOD`
- **Department Binding:** Exactly one primary department assignment (enforced by `hod_assignments` with `is_primary=true`)
- **Authorization:** Backend derives department from `user_profiles` + `hod_assignments` - **NEVER** trust department from request
- **Scope:** Can only access students/faculty/attendance for assigned department
- **MFA/OTP:** **REQUIRES CONFIRMATION** - not implemented in frontend; recommend enabling MFA for HOD accounts if security policy requires
- **Account Creation:** Only by ADMIN

#### Student
- **Identity:** Roll number mapped to account (not used as password)
- **Provisioning:** Created by HOD via `createStudent()` → account_status becomes `PENDING`. HOD provisions via `provisionStudentAccount()` → becomes `PROVISIONED` with temp credentials
- **Activation:** Student uses activation flow to set their own password (via Supabase Auth or backend reset flow)
- **First Login:** May require password change
- **Authorization:** Can only access own profile, attendance, exams, fees
- **Login:** Can authenticate via email or roll-number-based mapping configured in Supabase Auth

#### JWT Validation (Spring Boot)
- **Signature:** Verify using Supabase JWKS endpoint or HS256 secret depending on Supabase project config
- **Issuer (`iss`):** Must match Supabase project URL
- **Audience (`aud`):** Must match `authenticated` or configured audience
- **Expiration (`exp`):** Enforce token expiry, reject expired tokens
- **Token Type:** Validate access token (not refresh token for API calls)
- **Claims:** Extract `sub` (auth.users.id), map to `user_profiles`

#### Security Considerations
- **No duplicate passwords:** All credentials managed by Supabase Auth. Application DB stores no password hashes
- **Identity Mapping:** `auth.users.id` (UUID) is the canonical identity. `user_profiles.id` references it. `students.user_id` links when provisioned.
- **Role Mapping:** Roles stored in `user_profiles.role` with CHECK constraint
- **Account Deactivation:** Set `user_profiles.is_active=false` or disable in Supabase Auth; backend must check
- **Password Recovery:** Use Supabase Auth password reset flow
- **MFA:** **REQUIRES CONFIRMATION** - consider TOTP for HOD/ADMIN
- **Service Role Key:** `SUPABASE_SERVICE_ROLE_KEY` must NEVER be exposed to frontend, only used server-side
- **Refresh Tokens:** Frontend handles refresh via Supabase SDK; backend validates access tokens only
- **Department Derivation:** Always compute from `hod_assignments` joined with authenticated user

**Status:** PROPOSED - requires implementation, Admin UI missing

---

## 12. Database Requirements and Proposed Schema

*Based on verified frontend requirements. Tables don't exist yet - this is proposed design.*

### Core Tables

#### users (or leverage Supabase auth.users)
Supabase Auth owns identities. Need application user profile table mapping auth.users.id.

```sql
-- Proposed: app_users or user_profiles
user_profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id),
  role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN','HOD','STUDENT')),
  full_name VARCHAR(255),
  email VARCHAR(255),
  phone VARCHAR(20),
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
)
```

#### departments
```sql
departments (
  id VARCHAR(10) PRIMARY KEY, -- CSE, ECE, etc.
  name VARCHAR(255) NOT NULL,
  code VARCHAR(10),
  hod_user_id UUID REFERENCES user_profiles(id),
  intake INT,
  established_year INT,
  created_at TIMESTAMP DEFAULT NOW()
)
```

#### hod_assignments
```sql
hod_assignments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES user_profiles(id),
  department_id VARCHAR(10) REFERENCES departments(id),
  is_primary BOOLEAN DEFAULT true,
  assigned_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(user_id, department_id)
)
```

#### students
```sql
students (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  roll_number VARCHAR(20) UNIQUE NOT NULL,
  admission_number VARCHAR(30) UNIQUE,
  user_id UUID REFERENCES auth.users(id), -- linked when account provisioned/created
  department_id VARCHAR(10) REFERENCES departments(id) NOT NULL,
  year VARCHAR(20), -- I Year, II Year, III Year, IV Year
  semester VARCHAR(30),
  section VARCHAR(5),
  full_name VARCHAR(255) NOT NULL,
  short_name VARCHAR(100),
  course VARCHAR(50) DEFAULT 'B.Tech',
  admission_year VARCHAR(10),
  admission_date DATE,
  admission_type VARCHAR(50),
  admission_quota VARCHAR(50),
  caste_category VARCHAR(20),
  status VARCHAR(20) DEFAULT 'Active',
  account_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PROVISIONED, ACTIVE
  profile_status VARCHAR(20) DEFAULT 'Incomplete', -- Complete, Incomplete
  profile_completion INT DEFAULT 0,
  cgpa DECIMAL(3,2),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
)
```

#### student_profiles (detailed profile)
```sql
student_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id UUID REFERENCES students(id) UNIQUE NOT NULL,
  dob DATE,
  gender VARCHAR(10),
  blood_group VARCHAR(5),
  phone VARCHAR(20),
  email VARCHAR(255),
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
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
)
```

#### academic_structure
```sql
academic_years (id UUID PRIMARY KEY, year_name VARCHAR(20), is_active BOOLEAN)
semesters (id UUID PRIMARY KEY, semester_code VARCHAR(10), name VARCHAR(50))
sections (id UUID PRIMARY KEY, department_id VARCHAR(10), section_name VARCHAR(5), is_active BOOLEAN)
```

#### subjects
```sql
subjects (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  subject_code VARCHAR(20) UNIQUE NOT NULL,
  subject_name VARCHAR(255) NOT NULL,
  department_id VARCHAR(10) REFERENCES departments(id),
  year VARCHAR(20),
  semester VARCHAR(30),
  credits DECIMAL(3,1),
  is_active BOOLEAN DEFAULT true
)
```

#### faculty
```sql
faculty (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  staff_id VARCHAR(30) UNIQUE NOT NULL,
  user_id UUID REFERENCES user_profiles(id),
  department_id VARCHAR(10) REFERENCES departments(id),
  full_name VARCHAR(255) NOT NULL,
  designation VARCHAR(100),
  qualification VARCHAR(255),
  experience VARCHAR(50),
  email VARCHAR(255),
  phone VARCHAR(20),
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT NOW()
)
```

#### faculty_allocations
```sql
faculty_allocations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  faculty_id UUID REFERENCES faculty(id),
  subject_id UUID REFERENCES subjects(id),
  department_id VARCHAR(10) REFERENCES departments(id),
  year VARCHAR(20),
  semester VARCHAR(30),
  section VARCHAR(5),
  academic_year VARCHAR(20),
  assigned_at TIMESTAMP DEFAULT NOW()
)
```

#### attendance_periods
```sql
attendance_periods (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  department_id VARCHAR(10) REFERENCES departments(id),
  year VARCHAR(20),
  section VARCHAR(5),
  period_name VARCHAR(100) NOT NULL, -- "October 2026" or "Semester Cumulative 2026-27"
  period_type VARCHAR(20), -- monthly, cumulative, custom
  academic_year VARCHAR(20),
  created_by UUID REFERENCES user_profiles(id),
  created_at TIMESTAMP DEFAULT NOW()
)
```

#### attendance_records (subject-wise)
```sql
attendance_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  attendance_period_id UUID REFERENCES attendance_periods(id),
  student_id UUID REFERENCES students(id),
  subject_id UUID REFERENCES subjects(id),
  classes_held INT DEFAULT 0,
  classes_attended INT DEFAULT 0,
  classes_absent INT DEFAULT 0,
  attendance_percentage DECIMAL(5,2),
  uploaded_by UUID REFERENCES user_profiles(id),
  uploaded_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(attendance_period_id, student_id, subject_id)
)
```

#### attendance_summary (computed/denormalized)
```sql
attendance_summary (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  attendance_period_id UUID REFERENCES attendance_periods(id),
  student_id UUID REFERENCES students(id),
  total_classes_held INT,
  total_classes_attended INT,
  total_classes_absent INT,
  overall_percentage DECIMAL(5,2),
  status VARCHAR(30), -- Eligible, Condonation, Detained
  calculated_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(attendance_period_id, student_id)
)
```

#### attendance_upload_batches (audit)
```sql
attendance_upload_batches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  batch_ref VARCHAR(50) UNIQUE,
  department_id VARCHAR(10) REFERENCES departments(id),
  year VARCHAR(20),
  section VARCHAR(5),
  period_name VARCHAR(100),
  file_name VARCHAR(255),
  uploaded_by UUID REFERENCES user_profiles(id),
  subjects_count INT,
  students_count INT,
  valid_count INT,
  rejected_count INT,
  status VARCHAR(30) DEFAULT 'Published',
  uploaded_at TIMESTAMP DEFAULT NOW()
)
```

#### notifications
```sql
notifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title VARCHAR(255),
  content TEXT,
  target_role VARCHAR(20), -- ALL, STUDENT, HOD
  target_department_id VARCHAR(10),
  target_year VARCHAR(20),
  created_by UUID REFERENCES user_profiles(id),
  created_at TIMESTAMP DEFAULT NOW(),
  expires_at TIMESTAMP
)
```

#### audit_logs
```sql
audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES user_profiles(id),
  action VARCHAR(100),
  details TEXT,
  affected_entity VARCHAR(50),
  affected_id VARCHAR(100),
  ip_address VARCHAR(45),
  created_at TIMESTAMP DEFAULT NOW()
)
```

**Notes:**
- Don't duplicate auth in PostgreSQL - use Supabase Auth users
- Attendance can be calculated on-the-fly or cached in summary table (trade-off noted)
- Use UUIDs for primary keys
- Add indexes on frequently queried fields (roll_number, department_id, student_id)

---

## 13. API Requirements and Proposed API Contract

*All endpoints are PROPOSED based on frontend analysis. No actual API implementation exists.*

### API Design Principles
- RESTful JSON API
- Base path: `/api/v1/`
- Authentication: Bearer token (Supabase JWT)
- Role-based authorization
- Consistent response format
- Pagination for lists
- Validation on all inputs

### Proposed Endpoints

#### Authentication & User Context
| Method | Path | Role | Purpose | Status |
|---|---|---|---|---|
| GET | `/api/v1/auth/me` | Authenticated | Get current user profile with role/department | PROPOSED |
| POST | `/api/v1/auth/logout` | Authenticated | Logout (token invalidation) | PROPOSED |

#### HOD - Student Management
| Method | Path | Role | Purpose | Request/Notes | Status |
|---|---|---|---|---|---|
| GET | `/api/v1/hod/students` | HOD | List students in HOD's department (filtered/paginated) | Query: year, section, profileStatus, accountStatus, search | PROPOSED |
| POST | `/api/v1/hod/students` | HOD | Create single student (rollNumber, name) | Auto-assign dept/year/sem/sec from context | PROPOSED |
| POST | `/api/v1/hod/students/import` | HOD | Bulk import via CSV | Multipart file upload, returns imported/rejected counts | PROPOSED |
| GET | `/api/v1/hod/students/{rollNumber}` | HOD | Get detailed student profile | Full student + profile data | PROPOSED |
| PUT | `/api/v1/hod/students/{rollNumber}` | HOD | Update student (profile or official fields) | HOD can edit official fields | PROPOSED |
| POST | `/api/v1/hod/students/{rollNumber}/provision` | HOD | Provision student account | Generates temp credentials | PROPOSED |
| POST | `/api/v1/hod/students/provision/batch` | HOD | Batch provision all pending in dept | Returns count | PROPOSED |
| POST | `/api/v1/hod/students/{rollNumber}/promote` | HOD | Promote student to next semester | Uses progression logic | PROPOSED |
| POST | `/api/v1/hod/students/promote/batch` | HOD | Batch promote students | Array of roll numbers | PROPOSED |

#### Student Portal
| Method | Path | Role | Purpose | Status |
|---|---|---|---|---|
| GET | `/api/v1/student/dashboard` | STUDENT | Get dashboard overview data | PROPOSED |
| GET | `/api/v1/student/profile` | STUDENT | Get own profile | PROPOSED |
| PUT | `/api/v1/student/profile` | STUDENT | Update own profile | PROPOSED |
| GET | `/api/v1/student/attendance` | STUDENT | Get attendance (by semester) | PROPOSED |
| GET | `/api/v1/student/exams` | STUDENT | Get exam information | PROPOSED |
| GET | `/api/v1/student/fees` | STUDENT | Get fee details | PROPOSED |
| POST | `/api/v1/student/change-password` | STUDENT | Change password | PROPOSED |

#### Attendance Management
| Method | Path | Role | Purpose | Status |
|---|---|---|---|---|
| GET | `/api/v1/hod/attendance/subjects` | HOD | Get subjects for year/semester | PROPOSED |
| POST | `/api/v1/hod/attendance/subjects` | HOD | Add subject | PROPOSED |
| GET | `/api/v1/hod/attendance/template` | HOD | Download attendance template CSV | PROPOSED |
| POST | `/api/v1/hod/attendance/validate` | HOD | Validate uploaded CSV (preview) | PROPOSED |
| POST | `/api/v1/hod/attendance/publish` | HOD | Publish validated attendance | PROPOSED |
| GET | `/api/v1/hod/attendance/history` | HOD | Get upload history | PROPOSED |

#### Faculty Management
| Method | Path | Role | Purpose | Status |
|---|---|---|---|---|
| GET | `/api/v1/hod/faculty` | HOD | List faculty in department | PROPOSED |
| POST | `/api/v1/hod/faculty` | HOD | Add faculty member | PROPOSED |
| PUT | `/api/v1/hod/faculty/{staffId}` | HOD | Update faculty/allocations | PROPOSED |

#### Common/Reference Data
| Method | Path | Role | Purpose | Status |
|---|---|---|---|---|
| GET | `/api/v1/departments` | Authenticated | List departments | PROPOSED |
| GET | `/api/v1/sections` | Authenticated | Get sections for dept/year | PROPOSED |

**Example Response Format (proposed):**
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2026-10-31T12:00:00Z"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "...",
    "details": [...]
  },
  "timestamp": "..."
}
```

---

## 14. Excel and CSV Import Requirements

### Student CSV Import
- **Format:** 2 columns - `Roll Number`, `Student Name` (per sampleSpreadsheet:19-22, 235-306)
- **Parser:** `parseAndValidateStudentCSV()` 
- **Validations:** Missing fields, duplicates in file, duplicates in existing roster
- **HOD Context:** Requires branch/year/semester/section
- **Behavior:** Creates students as PENDING accounts

### Attendance CSV Import (Overall Multi-Subject)
- **Format:** `["Roll Number", "Student Name", ...subjectNames, "Overall Attendance"]` (sampleSpreadsheet:308-334)
- **Parser:** `parseAndValidateOverallAttendanceCSV()` (337-494)
- **Validations:**
  - Roll number must exist in roster and match department/section
  - Duplicate roll numbers in file
  - Subject percentages between 0-100
  - Overall percentage calculation from subject averages
  - Warnings for <75% (condonation) and <65% (detention)
- **Features:** Can handle multiple subjects in one sheet, computes overall average
- **Max file size:** Not specified - needs to be defined

### Templates
- `generateOverallAttendanceTemplate()` - creates template with current subjects
- `generateStudentRegistrationTemplate()` - 2-column template

**Requirements for Backend:**
- Parse CSV/Excel (Apache POI or OpenCSV)
- Server-side validation (don't trust client validation)
- Transactional imports (consider atomic vs partial)
- Error reporting with row numbers
- Support for large files
- File type/MIME validation

---

## 15. File Storage Requirements

**Current:** No file storage in frontend - only CSV parsing in memory  
**Proposed:** Supabase Storage

**Use Cases:**
- Student profile photos/documents (if needed)
- Attendance CSV uploads (audit trail)
- College documents (already referenced as static files in public/)
- Hall tickets, result documents

**Requirements:**
- Private buckets for sensitive documents
- Public buckets only if necessary
- File ownership checks
- Allowed MIME types and size limits
- Secure download URLs with expiration
- Integration with Spring Boot backend

---

## 16. Security Requirements

### Authentication & Authorization
- [ ] Supabase JWT verification in Spring Boot
- [ ] Role-based access control (ADMIN, HOD, STUDENT)
- [ ] Token expiration and refresh handling
- [ ] Secure session management
- [ ] Logout functionality

### Authorization Rules (Critical)
- [ ] **HOD Department Isolation:** HOD can ONLY access records of their assigned department. Backend MUST derive department from authenticated user's profile, never trust frontend department_id
- [ ] **Student Data Ownership:** Students can ONLY access their own profile, attendance, etc.
- [ ] **Role Enforcement:** All endpoints protected by role checks
- [ ] **No Faculty/Principal roles** - per scope

### Data Security
- [ ] Row Level Security (RLS) in Supabase PostgreSQL
- [ ] SQL injection prevention (use JPA/Hibernate prepared statements)
- [ ] Input validation on all endpoints
- [ ] Sensitive data not logged
- [ ] Secrets in environment variables only (never commit)
- [ ] Password handling via Supabase Auth (no custom password storage)

### API Security
- [ ] CORS configuration
- [ ] Rate limiting
- [ ] HTTPS only in production
- [ ] Request size limits
- [ ] Error messages don't leak sensitive info

### File Upload Security
- [ ] File type validation (MIME + extension)
- [ ] File size limits
- [ ] Virus scanning consideration
- [ ] Secure storage with access controls

---

## 17. Non-Functional Requirements

| Category | Requirement | Notes |
|---|---|---|
| **Performance** | Handle college-scale data (thousands of students) | Need pagination, efficient queries, indexing |
| **Scalability** | Support concurrent users | Spring Boot + connection pooling |
| **Reliability** | Data integrity for attendance/grades | Transactions, constraints, validation |
| **Availability** | Consider deployment on Google Cloud Run | Need health checks, proper shutdown |
| **Database** | Supabase PostgreSQL with connection pooling | Use HikariCP or similar |
| **Logging** | Structured logging | SLF4J/Logback |
| **Monitoring** | Application metrics | Consider Spring Actuator |
| **Error Tracking** | Centralized exception handling | @ControllerAdvice |
| **API Docs** | OpenAPI/Swagger | Springdoc OpenAPI |
| **Migrations** | Database schema versioning | Recommend Flyway |
| **Testing** | Unit, integration, E2E tests | JUnit 5, Mockito, Testcontainers |

---

## 18. Spring Boot Backend Architecture Blueprint

### Recommended Project Structure
```
tec-backend/
├── src/main/java/com/tecnrt/
│   ├── config/           # Security, CORS, OpenAPI config
│   ├── controller/       # REST controllers
│   ├── dto/              # Request/Response DTOs
│   ├── entity/           # JPA entities
│   ├── exception/        # Custom exceptions & handlers
│   ├── mapper/           # MapStruct or manual mappers
│   ├── repository/       # Spring Data JPA repos
│   ├── security/         # JWT validation, SecurityConfig
│   ├── service/          # Business logic
│   │   ├── impl/         # Service implementations
│   │   └── validation/   # Custom validators
│   └── util/             # Utilities (CSV parsing, etc.)
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/     # Flyway migrations
└── src/test/
```

### Key Components

**Security Configuration:**
- Spring Security with JWT filter
- Validate Supabase JWT tokens (check issuer, audience, signature)
- Role-based authorization with `@PreAuthorize`
- CORS configuration for frontend

**DTO Layer:** Separate DTOs from entities to avoid exposing internal structure

**Service Layer:** Business logic, transaction management with `@Transactional`

**Exception Handling:** `@ControllerAdvice` with `@ExceptionHandler` for consistent error responses

**Validation:** Bean Validation (JSR-303) with `@Valid`

**Database Migrations:** **Flyway** recommended over Liquibase for simplicity

### Dependencies (Maven)
```xml
<dependencies>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
  <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId></dependency>
  <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
  <dependency><groupId>com.nimbusds</groupId><artifactId>nimbus-jose-jwt</artifactId></dependency> <!-- for JWT validation -->
  <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId></dependency>
  <dependency><groupId>org.apache.commons</groupId><artifactId>commons-csv</artifactId></dependency>
</dependencies>
```

---

## 19. Local Development and Integration Plan

### Local Setup
- **Frontend:** React dev server on `http://localhost:5173` (Vite default)
- **Backend:** Spring Boot on `http://localhost:8080`
- **Database:** Supabase PostgreSQL (remote) or local Postgres
- **Auth:** Supabase Auth

### Environment Variables (Backend - NO VALUES in audit)
```properties
# Database
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

# Supabase
SUPABASE_URL=
SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=  # Server-side only, NEVER expose
SUPABASE_JWT_SECRET=        # For JWT verification

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173

# Storage
SUPABASE_STORAGE_BUCKET=

# Application
SERVER_PORT=8080
```

### Integration Steps
1. Setup Spring Boot project with dependencies
2. Configure Supabase JWT verification
3. Create database schema via Flyway migrations
4. Implement authentication/authorization
5. Build API endpoints matching frontend needs
6. Update frontend to call backend APIs (replace mock data/state operations)
7. Test integration locally

**Note:** Frontend changes are out of scope for this audit - audit only identifies what needs to change.

---

## 20. Testing and End-to-End Strategy

### Unit Tests
- Service layer business logic
- Validators
- CSV parsers
- Utility functions

### Integration Tests
- Controller endpoints with MockMvc
- Repository layer with @DataJpaTest
- Security configuration
- JWT validation

### Authentication Tests
- [ ] Valid HOD login with Supabase JWT
- [ ] Invalid/expired tokens rejected
- [ ] Role-based access enforced
- [ ] Logout works correctly

### Authorization Tests
- [ ] HOD can only access their department data
- [ ] Student can only access own data
- [ ] Cross-department access blocked
- [ ] Unauthorized role access blocked

### Functional Tests
- [ ] Student registration by HOD
- [ ] Bulk student import
- [ ] Profile completion by student
- [ ] Attendance CSV upload and validation
- [ ] Multi-subject attendance calculation
- [ ] Student promotion
- [ ] Account provisioning

### Integration Tests
- [ ] Frontend-backend communication
- [ ] JWT verification flow
- [ ] Database persistence
- [ ] File upload to Supabase Storage
- [ ] Error handling

### Test Data Strategy
- Use test fixtures
- Seed minimal data for tests
- Mock Supabase services where appropriate
- Use Testcontainers for PostgreSQL integration tests

---

## 21. Requirements Gap Register

| ID | Issue | Evidence | Impact | Priority | Required Decision |
|---|---|---|---|---|---|
| GAP-001 | No backend/API layer exists | Entire app uses mock data in AuthContext | CRITICAL | CRITICAL | Must build complete backend |
| GAP-002 | Authentication not integrated with Supabase | loginWithCredentials is client-side simulation | CRITICAL | CRITICAL | Clarify auth flow - frontend calls Supabase directly or via backend? |
| GAP-003 | No environment configuration for backend URLs | No API client, no env vars for backend | CRITICAL | CRITICAL | Define API base URL strategy |
| GAP-004 | Attendance CSV format needs confirmation | HODAttendanceUpload expects multi-subject format but exact spec unclear | HIGH | HIGH | Confirm final CSV column structure with stakeholders |
| GAP-005 | Admin role not implemented | Only student/hod roles exist | HIGH | HIGH | Is Admin portal needed? Or only HOD creates students? |
| GAP-006 | Password reset not implemented | No password reset flow in frontend | HIGH | HIGH | Implement via Supabase Auth or custom flow |
| GAP-007 | File upload for profile documents not implemented | Profile has fields but no upload UI found | MEDIUM | MEDIUM | Are document uploads needed? |
| GAP-008 | Exam/fees modules mostly read-only mock data | Limited functionality | MEDIUM | MEDIUM | Full implementation needed or partial? |
| GAP-009 | Notifications system not fully implemented | ANNOUNCEMENTS exist as static mock data | MEDIUM | MEDIUM | Backend support needed? |
| GAP-010 | Timetable/exam schedules mostly empty | INITIAL_EXAM_TIMETABLES empty, limited implementation | MEDIUM | MEDIUM | In scope or out of scope? |
| GAP-011 | No data migration strategy | Moving from mock to real DB | HIGH | HIGH | Plan data seeding/migration |
| GAP-012 | Faculty portal removed but data still used | Code says removed but faculty used by HOD | LOW | MEDIUM | Confirm scope - faculty directory only? |

---

## 22. Technical Risks and Recommendations

| Risk | Impact | Likelihood | Mitigation |
|---|---|---|---|
| **HOD department isolation breach** | CRITICAL | HIGH | Derive department from JWT claims/user profile on backend, never trust request body. Add comprehensive tests. |
| **JWT verification complexity with Supabase** | HIGH | MEDIUM | Use Supabase JWT public keys or verify with Supabase SDK. Test thoroughly. |
| **CSV import data corruption** | HIGH | MEDIUM | Server-side validation, transactions, rollback on errors, detailed error reporting. |
| **Large CSV file handling** | MEDIUM | MEDIUM | Stream processing, file size limits, async processing if needed. |
| **Attendance calculation errors** | HIGH | MEDIUM | Server-side calculation (don't trust client). Match exactly with JNTUK rules. |
| **Data consistency during promotion** | HIGH | MEDIUM | Use transactions, preserve history properly. |
| **Supabase RLS misconfiguration** | CRITICAL | HIGH | Test RLS policies thoroughly, use service role carefully on backend. |

**Recommendations:**
1. Start with authentication and core entities (users, departments, students)
2. Implement HOD department isolation as first security measure
3. Build CSV parsers server-side with strict validation
4. Use Flyway for all schema changes
5. Add comprehensive logging and audit trails
6. Don't expose service role keys to frontend
7. Implement proper error handling from day one
8. Consider using DTOs to prevent over-exposure of data

---

## 23. Backend Development Roadmap

| Phase | Tasks | Priority | Dependencies |
|---|---|---|---|
| **Phase 1: Setup** | Project structure, dependencies, config, Flyway setup | CRITICAL | None |
| **Phase 2: Auth & Security** | Supabase JWT verification, SecurityConfig, role-based auth, CORS | CRITICAL | Phase 1 |
| **Phase 3: Core Entities** | Departments, user profiles, hod_assignments, migrations | CRITICAL | Phase 1 |
| **Phase 4: Student Management** | Students, student_profiles entities, CRUD APIs for HOD | CRITICAL | Phase 3 |
| **Phase 5: Account Provisioning** | Link to Supabase Auth, temp password flow, account status management | CRITICAL | Phase 2,4 |
| **Phase 6: Attendance** | Attendance periods, records, summary tables, CSV upload/validation/publish | HIGH | Phase 4 |
| **Phase 7: Faculty & Subjects** | Faculty, subjects, allocations | HIGH | Phase 3 |
| **Phase 8: Academic Progression** | Promotion logic, semester tracking | HIGH | Phase 4 |
| **Phase 9: File Storage** | Supabase Storage integration | MEDIUM | Phase 2 |
| **Phase 10: Integration & Testing** | Frontend integration, E2E tests, security testing | HIGH | All previous |

**Estimated effort:** Significant - complete backend rebuild required as no backend exists currently.

---

## 24. Unresolved Questions and Decisions Required

| Question | Context | Impact | Suggested Action |
|---|---|---|---|
| **Auth Flow** | Supabase Auth mentioned - does frontend call Supabase directly or does backend handle auth? | CRITICAL | Clarify architecture: Option A) Frontend uses Supabase Auth SDK, sends JWT to backend for verification. Option B) Backend handles all auth. |
| **Admin Role** | No Admin implementation found. Only HOD creates students. Is Admin portal needed? | HIGH | Confirm if ADMIN role needed or HOD has full department admin rights |
| **Password Reset** | Not implemented in frontend | HIGH | Implement via Supabase Auth password reset flow |
| **MFA/OTP** | Mentioned as optional/required | HIGH | Decide on MFA requirements |
| **CSV Format Finalization** | Multi-subject format exists but need confirmation | HIGH | Validate exact column names and format with stakeholders |
| **Profile Approval Workflow** | Frontend says "saves directly without HOD approval" | MEDIUM | Confirm this is correct per requirements |
| **Document Uploads** | Profile has fields but no upload UI visible | MEDIUM | Clarify if profile photos/documents needed |
| **Exam/Fees Full Implementation** | Mostly read-only mock data | MEDIUM | Determine scope - full backend or display-only? |
| **Notifications** | Static mock announcements | MEDIUM | Need dynamic notification system? |
| **Timetable Management** | Minimal implementation | MEDIUM | In scope for backend? |
| **Data Migration** | Moving from mocks to real DB | HIGH | Plan initial data seeding strategy |

---

## 25. Final Handoff Summary for Backend Developer

### What You Need to Know

**Project Context:**
- College Management Portal for Tirumala Engineering College
- Frontend is a React SPA with extensive mock data - this is a prototype/demo
- Backend must replace ALL client-side mock operations with real APIs
- Roles: STUDENT, HOD only (Faculty/Principal portals removed from scope)
- HOD manages their own department exclusively

**Critical Implementation Points:**
1. **Security First:** HOD department isolation is NON-NEGOTIABLE. Derive department from authenticated user, never trust client input.
2. **Supabase Integration:** Use Supabase Auth for authentication, Supabase PostgreSQL for database, Supabase Storage for files.
3. **CSV Processing:** Complex validation logic exists in frontend - replicate server-side with same rules.
4. **Attendance:** Multi-subject unified upload is core feature. Calculate overall percentage correctly.
5. **No Breaking Changes:** Preserve frontend behavior and data structures where possible for easy integration.

**Files to Reference for Business Logic:**
- `src/context/AuthContext.jsx` - All business operations and rules
- `src/data/mockData.js` - Data models, constants, helper functions
- `src/data/sampleSpreadsheet.js` - CSV parsing/validation logic
- `src/components/hod/HODAttendanceUpload.jsx` - Attendance workflow UI logic
- `src/components/hod/HODStudentRecords.jsx` - Student management workflows
- `src/components/student/StudentProfile.jsx` - Profile requirements

**Starting Point Recommendations:**
1. Set up Spring Boot project with all dependencies
2. Configure Supabase JWT verification first
3. Create base entities and migrations matching schema design
4. Implement auth endpoints and role-based security
5. Build HOD student management (highest priority - core feature)
6. Implement attendance upload/validation
7. Add student portal endpoints
8. Test with department isolation thoroughly

**Verification Checklist:**
- [ ] All routes from frontend have corresponding API endpoints or are accounted for
- [ ] Mock data structures mapped to database schema
- [ ] Security rules enforced on backend
- [ ] CSV validation matches frontend logic
- [ ] Department isolation tested
- [ ] Student ownership enforced

---

**Audit Completed:** All 25 sections documented. No frontend files modified. One file created: `BACKEND_AUDIT.md` in repository root.

**Report Summary:**
- Exact file path: `D:\Tirumala websote\tirumala frontend\BACKEND_AUDIT.md`
- Repository areas inspected: package.json, vite.config.js, src/ (all pages, components, context, data), configuration files
- Routes audited: 15 public pages + Student Dashboard (6 tabs) + HOD Dashboard (7 tabs) = Comprehensive coverage
- Workflows audited: Authentication, Student Registration, Profile Management, Section Management, Attendance Upload, Faculty Management, Promotion
- Features: VERIFIED (core structure), MOCKED (all functionality), MISSING (backend/APIs), UNKNOWN (some business decisions)
- Critical unresolved decisions: Auth flow with Supabase, Admin role necessity, CSV format finalization, MFA requirements
- No existing project files modified ✓
- Backend implementation not started ✓
- Exactly one audit file created ✓
