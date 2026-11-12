package com.tecnrt.modules.hod.service;

import com.tecnrt.exception.ResourceNotFoundException;
import com.tecnrt.exception.UnauthorizedException;
import com.tecnrt.modules.attendance.entity.AttendanceSummary;
import com.tecnrt.modules.attendance.entity.Subject;
import com.tecnrt.modules.attendance.repository.AttendanceSummaryRepository;
import com.tecnrt.modules.attendance.repository.SubjectRepository;
import com.tecnrt.modules.auth.entity.User;
import com.tecnrt.modules.auth.repository.UserRepository;
import com.tecnrt.modules.hod.dto.*;
import com.tecnrt.modules.hod.entity.Faculty;
import com.tecnrt.modules.hod.entity.Section;
import com.tecnrt.modules.hod.repository.FacultyRepository;
import com.tecnrt.modules.hod.repository.SectionRepository;
import com.tecnrt.modules.student.entity.Student;
import com.tecnrt.modules.student.entity.StudentProfile;
import com.tecnrt.modules.student.repository.StudentProfileRepository;
import com.tecnrt.modules.student.repository.StudentRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class HodService {

    private final HodSecurityService hodSecurityService;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final FacultyRepository facultyRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;

    public HodService(
            HodSecurityService hodSecurityService,
            StudentRepository studentRepository,
            StudentProfileRepository studentProfileRepository,
            UserRepository userRepository,
            SectionRepository sectionRepository,
            FacultyRepository facultyRepository,
            AttendanceSummaryRepository attendanceSummaryRepository,
            SubjectRepository subjectRepository,
            PasswordEncoder passwordEncoder) {
        this.hodSecurityService = hodSecurityService;
        this.studentRepository = studentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.facultyRepository = facultyRepository;
        this.attendanceSummaryRepository = attendanceSummaryRepository;
        this.subjectRepository = subjectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public HodOverviewResponse getOverview() {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        long totalStudents = studentRepository.countByDepartmentId(deptId);
        long totalFaculty = facultyRepository.findByDepartmentIdAndActiveTrueOrderByFullNameAsc(deptId).size();

        List<AttendanceSummary> summaries = attendanceSummaryRepository.findByDepartmentId(deptId);

        long eligible = 0;
        long condonation = 0;
        long detained = 0;
        BigDecimal sumPct = BigDecimal.ZERO;

        for (AttendanceSummary s : summaries) {
            sumPct = sumPct.add(s.getOverallPercentage());
            String st = s.getStatus();
            if ("ELIGIBLE".equalsIgnoreCase(st) || "ELIGIBLE_BY_OVERRIDE".equalsIgnoreCase(st)) {
                eligible++;
            } else if ("CONDONATION".equalsIgnoreCase(st)) {
                condonation++;
            } else if ("DETAINED".equalsIgnoreCase(st)) {
                detained++;
            }
        }

        BigDecimal avgAttendance = summaries.isEmpty()
                ? BigDecimal.ZERO
                : sumPct.divide(BigDecimal.valueOf(summaries.size()), 2, RoundingMode.HALF_UP);

        // Section breakdown
        List<Section> sections = sectionRepository.findByDepartmentIdAndActiveTrueOrderBySectionNameAsc(deptId);
        List<HodOverviewResponse.SectionStatDto> sectionStats = new ArrayList<>();
        for (Section sec : sections) {
            long count = studentRepository.findByDepartmentIdAndYearAndSectionOrderByRollNumberAsc(
                    deptId, "4th Year", sec.getSectionName()).size();
            sectionStats.add(new HodOverviewResponse.SectionStatDto("4th Year", sec.getSectionName(), count, avgAttendance));
        }

        return new HodOverviewResponse(
                deptId,
                deptId + " Department Administration",
                totalStudents,
                totalFaculty,
                avgAttendance,
                eligible,
                condonation,
                detained,
                sectionStats
        );
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> getSections() {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        return sectionRepository.findByDepartmentIdAndActiveTrueOrderBySectionNameAsc(deptId).stream()
                .map(s -> new SectionResponse(s.getId(), s.getDepartmentId(), s.getSectionName(), s.getAcademicYear(), s.isActive(), s.getId() != null ? null : null))
                .toList();
    }

    @Transactional
    public SectionResponse createSection(CreateSectionRequest request) {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        String secName = request.sectionName().trim().toUpperCase();

        if (sectionRepository.existsByDepartmentIdAndSectionName(deptId, secName)) {
            throw new IllegalArgumentException("Section " + secName + " already exists for department " + deptId);
        }

        Section section = new Section(deptId, secName, request.academicYear() != null ? request.academicYear() : "2026-2027");
        Section saved = sectionRepository.save(section);

        String sectionId = saved.getId() != null ? saved.getId().toString() : "N/A";
        hodSecurityService.recordAudit("SECTION_CREATE", "Added section " + secName + " to department " + deptId, "Section", sectionId);

        return new SectionResponse(saved.getId(), saved.getDepartmentId(), saved.getSectionName(), saved.getAcademicYear(), saved.isActive(), null);
    }

    @Transactional(readOnly = true)
    public List<HodStudentResponse> getStudents(String year, String section) {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        List<Student> students;

        if (year != null && !year.isBlank() && section != null && !section.isBlank()) {
            students = studentRepository.findByDepartmentIdAndYearAndSectionOrderByRollNumberAsc(deptId, year, section);
        } else {
            students = studentRepository.findByDepartmentIdOrderByRollNumberAsc(deptId);
        }

        return students.stream()
                .map(s -> new HodStudentResponse(
                        s.getId(),
                        s.getRollNumber(),
                        s.getFullName(),
                        s.getDepartmentId(),
                        s.getYear(),
                        s.getSemester(),
                        s.getSection(),
                        s.isLateralEntry(),
                        s.getStatus(),
                        s.getAccountStatus(),
                        s.getProfileStatus(),
                        s.getProfileCompletion(),
                        s.getCgpa(),
                        s.getUser() != null
                ))
                .toList();
    }

    @Transactional
    public HodStudentResponse createStudent(CreateStudentRequest req) {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        String roll = req.rollNumber().trim().toUpperCase();

        if (studentRepository.existsByRollNumberIgnoreCase(roll)) {
            throw new IllegalArgumentException("Student with Roll Number " + roll + " already exists.");
        }

        Student student = new Student();
        student.setRollNumber(roll);
        student.setFullName(req.fullName().trim());
        student.setDepartmentId(deptId);
        student.setYear(req.year());
        student.setSemester(req.semester());
        student.setSection(req.section().trim().toUpperCase());
        student.setAdmissionYear(req.admissionYear() != null ? req.admissionYear() : "2026");
        student.setAdmissionType(req.admissionType() != null ? req.admissionType() : "Convener Quota (EAPCET)");
        student.setLateralEntry(req.lateralEntry());
        student.setStatus("Active");
        student.setAccountStatus("PENDING");
        student.setProfileStatus("Incomplete");
        student.setProfileCompletion(0);

        Student saved = studentRepository.save(student);

        // Blank profile
        StudentProfile profile = new StudentProfile();
        profile.setStudent(saved);
        studentProfileRepository.save(profile);

        String studentId = saved.getId() != null ? saved.getId().toString() : saved.getRollNumber();
        hodSecurityService.recordAudit("STUDENT_CREATE", "Enrolled student " + roll + " (" + req.fullName() + ")", "Student", studentId);

        return new HodStudentResponse(
                saved.getId(), saved.getRollNumber(), saved.getFullName(), saved.getDepartmentId(),
                saved.getYear(), saved.getSemester(), saved.getSection(), saved.isLateralEntry(),
                saved.getStatus(), saved.getAccountStatus(), saved.getProfileStatus(), saved.getProfileCompletion(),
                saved.getCgpa(), false
        );
    }

    @Transactional
    public ProvisionResultResponse batchProvision(BatchProvisionRequest request) {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        List<String> rolls = request.rollNumbers() != null ? request.rollNumbers() : List.of();

        int provisioned = 0;
        int alreadyExists = 0;
        int failed = 0;
        List<ProvisionResultResponse.ProvisionDetail> details = new ArrayList<>();

        for (String rawRoll : rolls) {
            String roll = rawRoll.trim().toUpperCase();
            Optional<Student> studentOpt = studentRepository.findByRollNumberIgnoreCase(roll);

            if (studentOpt.isEmpty()) {
                failed++;
                details.add(new ProvisionResultResponse.ProvisionDetail(roll, "NOT_FOUND", "Student record does not exist in college roster"));
                continue;
            }

            Student s = studentOpt.get();
            if (!deptId.equalsIgnoreCase(s.getDepartmentId())) {
                failed++;
                details.add(new ProvisionResultResponse.ProvisionDetail(roll, "FORBIDDEN", "Student belongs to department " + s.getDepartmentId() + ", outside your scope"));
                continue;
            }

            if (s.getUser() != null) {
                alreadyExists++;
                details.add(new ProvisionResultResponse.ProvisionDetail(roll, "ALREADY_EXISTS", "User login credentials already provisioned"));
                continue;
            }

            // Create user account with roll number as default username and password
            User newUser = new User();
            newUser.setUsername(roll);
            newUser.setPasswordHash(passwordEncoder.encode(roll)); // Default password is roll number
            newUser.setRole("STUDENT");
            newUser.setActive(true);
            newUser.setPasswordChanged(false); // Must change on first login

            User savedUser = userRepository.save(newUser);
            s.setUser(savedUser);
            s.setAccountStatus("PROVISIONED");
            studentRepository.save(s);

            provisioned++;
            details.add(new ProvisionResultResponse.ProvisionDetail(roll, "PROVISIONED", "Credentials created with default initial password"));
        }

        hodSecurityService.recordAudit("BATCH_PROVISION", "Provisioned " + provisioned + " accounts in " + deptId, "Student", deptId);

        return new ProvisionResultResponse(rolls.size(), provisioned, alreadyExists, failed, details);
    }

    @Transactional
    public BulkImportStudentResult importStudentsCsv(MultipartFile file, String year, String semester, String section) {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file cannot be empty");
        }

        int total = 0;
        int imported = 0;
        int skipped = 0;
        List<BulkImportStudentResult.ImportDetail> details = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : parser) {
                total++;
                String roll = record.isSet(0) ? record.get(0).trim().toUpperCase() : "";
                String name = record.isSet(1) ? record.get(1).trim() : "";

                if (roll.isBlank() || name.isBlank()) {
                    skipped++;
                    details.add(new BulkImportStudentResult.ImportDetail(total, roll, name, "ERROR", "Missing roll number or student name"));
                    continue;
                }

                if (studentRepository.existsByRollNumberIgnoreCase(roll)) {
                    skipped++;
                    details.add(new BulkImportStudentResult.ImportDetail(total, roll, name, "SKIPPED_DUPLICATE", "Roll number already registered"));
                    continue;
                }

                Student student = new Student();
                student.setRollNumber(roll);
                student.setFullName(name);
                student.setDepartmentId(deptId);
                student.setYear(year);
                student.setSemester(semester);
                student.setSection(section.trim().toUpperCase());
                student.setAdmissionYear(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                student.setAdmissionType("Convener Quota (EAPCET)");
                student.setStatus("Active");
                student.setAccountStatus("PENDING");
                student.setProfileStatus("Incomplete");
                student.setProfileCompletion(0);

                Student saved = studentRepository.save(student);

                StudentProfile profile = new StudentProfile();
                profile.setStudent(saved);
                studentProfileRepository.save(profile);

                imported++;
                details.add(new BulkImportStudentResult.ImportDetail(total, roll, name, "IMPORTED", "Registered to " + deptId + " " + year + " Sec " + section));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse student registration CSV: " + e.getMessage(), e);
        }

        hodSecurityService.recordAudit("CSV_STUDENT_IMPORT", "Imported " + imported + " students into " + deptId + " " + year + " " + section, "Student", deptId);

        return new BulkImportStudentResult(total, imported, skipped, details);
    }

    @Transactional(readOnly = true)
    public List<FacultyResponse> getFaculty() {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        return facultyRepository.findByDepartmentIdAndActiveTrueOrderByFullNameAsc(deptId).stream()
                .map(f -> new FacultyResponse(
                        f.getId(), f.getStaffId(), f.getDepartmentId(), f.getFullName(),
                        f.getDesignation(), f.getQualification(), f.getExperience(),
                        f.getEmail(), f.getPhone(), f.getWorkloadHours(), f.isActive()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public StatutoryReportResponse getStatutoryReport(String semester) {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        List<AttendanceSummary> summaries = attendanceSummaryRepository.findByDepartmentId(deptId);

        long eligible = 0;
        long condonation = 0;
        long detained = 0;
        long overridden = 0;
        List<StatutoryReportResponse.StatutoryStudentRow> studentRows = new ArrayList<>();

        for (AttendanceSummary s : summaries) {
            if (s.isOverridden()) overridden++;
            String st = s.getStatus();
            if ("ELIGIBLE".equalsIgnoreCase(st) || "ELIGIBLE_BY_OVERRIDE".equalsIgnoreCase(st)) {
                eligible++;
            } else if ("CONDONATION".equalsIgnoreCase(st)) {
                condonation++;
            } else if ("DETAINED".equalsIgnoreCase(st)) {
                detained++;
            }

            studentRows.add(new StatutoryReportResponse.StatutoryStudentRow(
                    s.getId(),
                    s.getStudent().getRollNumber(),
                    s.getStudent().getFullName(),
                    s.getStudent().getYear(),
                    s.getStudent().getSection(),
                    s.getTotalClassesHeld(),
                    s.getTotalClassesAttended(),
                    s.getOverallPercentage(),
                    s.getStatus(),
                    s.isOverridden(),
                    s.getOverrideReason()
            ));
        }

        return new StatutoryReportResponse(
                deptId,
                "2026-2027",
                semester != null ? semester : "Sem 4-1",
                summaries.size(),
                eligible,
                condonation,
                detained,
                overridden,
                studentRows
        );
    }

    @Transactional
    public StatutoryReportResponse.StatutoryStudentRow applyDetentionOverride(DetentionOverrideRequest req) {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();

        AttendanceSummary summary = attendanceSummaryRepository.findById(req.summaryId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance summary record not found: " + req.summaryId()));

        if (!deptId.equalsIgnoreCase(summary.getPeriod().getDepartmentId())) {
            throw new UnauthorizedException("Cannot override records outside your department scope");
        }

        summary.setOverridden(true);
        summary.setStatus("ELIGIBLE_BY_OVERRIDE");
        summary.setOverrideReason(req.reason().trim());
        AttendanceSummary saved = attendanceSummaryRepository.save(summary);

        hodSecurityService.recordAudit(
                "DETENTION_OVERRIDE",
                "Overrode detention for " + saved.getStudent().getRollNumber() + ": " + req.reason(),
                "AttendanceSummary",
                saved.getId().toString()
        );

        return new StatutoryReportResponse.StatutoryStudentRow(
                saved.getId(),
                saved.getStudent().getRollNumber(),
                saved.getStudent().getFullName(),
                saved.getStudent().getYear(),
                saved.getStudent().getSection(),
                saved.getTotalClassesHeld(),
                saved.getTotalClassesAttended(),
                saved.getOverallPercentage(),
                saved.getStatus(),
                saved.isOverridden(),
                saved.getOverrideReason()
        );
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjects(String semester, String regulation) {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        List<Subject> subjects;

        if (semester != null && !semester.isBlank() && regulation != null && !regulation.isBlank()) {
            subjects = subjectRepository.findByDepartmentIdAndSemesterAndRegulationAndActiveTrue(deptId, semester, regulation);
        } else if (semester != null && !semester.isBlank()) {
            subjects = subjectRepository.findByDepartmentIdAndSemesterAndActiveTrue(deptId, semester);
        } else {
            subjects = subjectRepository.findByDepartmentIdAndActiveTrueOrderBySemesterAscSubjectCodeAsc(deptId);
        }

        return subjects.stream()
                .map(s -> new SubjectResponse(
                        s.getId(),
                        s.getSubjectCode(),
                        s.getSubjectName(),
                        s.getDepartmentId(),
                        s.getYear(),
                        s.getSemester(),
                        s.getRegulation(),
                        s.getSubjectType(),
                        s.getCredits(),
                        s.isActive()
                ))
                .toList();
    }

    @Transactional
    public SubjectResponse saveSubject(SubjectRequest req) {
        String deptId = hodSecurityService.getAuthenticatedHodDepartment();
        String code = req.subjectCode().trim().toUpperCase();
        String reg = req.regulation() != null && !req.regulation().isBlank() ? req.regulation().trim().toUpperCase() : "R20";

        Optional<Subject> existingOpt = subjectRepository.findBySubjectCodeIgnoreCaseAndRegulationIgnoreCase(code, reg);
        Subject subject;

        if (existingOpt.isPresent()) {
            subject = existingOpt.get();
            subject.setSubjectName(req.subjectName().trim());
            subject.setYear(req.year());
            subject.setSemester(req.semester());
            subject.setSubjectType(req.subjectType() != null ? req.subjectType() : "Theory");
            subject.setCredits(req.credits());
            subject.setActive(true);
        } else {
            subject = new Subject(
                    code,
                    req.subjectName().trim(),
                    deptId,
                    req.year(),
                    req.semester(),
                    reg,
                    req.subjectType() != null ? req.subjectType() : "Theory",
                    req.credits()
            );
        }

        Subject saved = subjectRepository.save(subject);

        hodSecurityService.recordAudit(
                "CURRICULUM_UPDATE",
                "Updated course " + code + " (" + reg + ") for " + req.semester(),
                "Subject",
                saved.getId().toString()
        );

        return new SubjectResponse(
                saved.getId(),
                saved.getSubjectCode(),
                saved.getSubjectName(),
                saved.getDepartmentId(),
                saved.getYear(),
                saved.getSemester(),
                saved.getRegulation(),
                saved.getSubjectType(),
                saved.getCredits(),
                saved.isActive()
        );
    }
}

