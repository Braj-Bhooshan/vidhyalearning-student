package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors the backend's dedicated Performance-screen endpoints (spec #63) -
// GET /api/v1/mobile/student/performance/examination and .../performance/assignments.
// These are purpose-built for this screen: unlike the general-purpose /students/{id}/grades
// and /mobile/parent/assignments + per-submission calls this screen used to stitch together
// client-side, they merge both the question-by-question submission flow AND the teacher's
// bulk "direct marks" flow, and return a real backend-computed grade_letter (same
// calculate_grade_letter scale used for examinations) so the app never has to guess one.

@Serializable
data class StudentExaminationMarkItem(
    val id: Int,
    @SerialName("examination_id") val examinationId: Int? = null,
    @SerialName("examination_name") val examinationName: String? = null,
    @SerialName("academic_year") val academicYear: String,
    val term: String? = null,
    @SerialName("grade_level") val gradeLevel: String,
    val section: String? = null,
    val subject: String,
    @SerialName("marks_obtained") val marksObtained: Float,
    @SerialName("total_marks") val totalMarks: Float,
    val percentage: Float? = null,
    @SerialName("grade_letter") val gradeLetter: String? = null,
    @SerialName("teacher_name") val teacherName: String? = null,
    val remarks: String? = null
)

@Serializable
data class StudentExaminationPerformanceResponse(
    @SerialName("student_id") val studentId: Int,
    @SerialName("student_name") val studentName: String,
    @SerialName("grade_level") val gradeLevel: String,
    @SerialName("academic_year") val academicYear: String,
    val term: String? = null,
    @SerialName("available_terms") val availableTerms: List<String> = emptyList(),
    @SerialName("overall_percentage") val overallPercentage: Float = 0f,
    @SerialName("overall_grade") val overallGrade: String = "N/A",
    @SerialName("subject_grades") val subjectGrades: List<StudentExaminationMarkItem> = emptyList()
)

@Serializable
data class StudentAssignmentGradeItem(
    @SerialName("assignment_id") val assignmentId: Int,
    val title: String,
    val subject: String,
    val term: String? = null,
    @SerialName("academic_year") val academicYear: String,
    @SerialName("max_marks") val maxMarks: Float? = null,
    @SerialName("obtained_marks") val obtainedMarks: Float,
    val percentage: Float? = null,
    @SerialName("grade_letter") val gradeLetter: String? = null,
    @SerialName("graded_at") val gradedAt: String? = null
)

@Serializable
data class StudentSubjectAssignmentTotal(
    val subject: String,
    @SerialName("assignment_count") val assignmentCount: Int,
    @SerialName("total_obtained_marks") val totalObtainedMarks: Float,
    @SerialName("total_max_marks") val totalMaxMarks: Float,
    val percentage: Float? = null,
    @SerialName("grade_letter") val gradeLetter: String? = null
)

@Serializable
data class StudentAssignmentPerformanceResponse(
    @SerialName("student_id") val studentId: Int,
    val subject: String? = null,
    @SerialName("available_subjects") val availableSubjects: List<String> = emptyList(),
    @SerialName("assignment_grades") val assignmentGrades: List<StudentAssignmentGradeItem> = emptyList(),
    @SerialName("subject_totals") val subjectTotals: List<StudentSubjectAssignmentTotal> = emptyList()
)
