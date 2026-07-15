package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors the backend's GradeResponse / StudentGradeReport, confirmed against the local
// backend's /openapi.json - see GET /api/v1/students/{student_id}/grades. Same "students" tag,
// OAuth2 bearer and parent-token-scope caveat as AttendanceApi/AcademicYearApi.
//
// There is no dedicated "list terms" endpoint on the backend - the term dropdown on the
// Examination tab is populated from the distinct `term` values already present on this
// response's subjectGrades, not a separate backend call.
@Serializable
data class GradeResponse(
    val id: Int = 0,
    @SerialName("student_id") val studentId: Int = 0,
    @SerialName("student_name") val studentName: String = "",
    @SerialName("examination_id") val examinationId: Int? = null,
    @SerialName("examination_name") val examinationName: String? = null,
    @SerialName("academic_year") val academicYear: String = "",
    val term: String? = null,
    @SerialName("grade_level") val gradeLevel: String = "",
    val section: String? = null,
    val subject: String = "",
    @SerialName("marks_obtained") val marksObtained: String = "0",
    @SerialName("total_marks") val totalMarks: String = "0",
    val percentage: String? = null,
    @SerialName("grade_letter") val gradeLetter: String? = null,
    @SerialName("teacher_name") val teacherName: String? = null,
    val remarks: String? = null,
    @SerialName("is_published") val isPublished: Boolean = false
)

@Serializable
data class StudentGradeReport(
    @SerialName("student_id") val studentId: Int = 0,
    @SerialName("student_name") val studentName: String = "",
    @SerialName("grade_level") val gradeLevel: String = "",
    @SerialName("academic_year") val academicYear: String = "",
    @SerialName("overall_percentage") val overallPercentage: String = "0",
    @SerialName("overall_grade") val overallGrade: String = "",
    @SerialName("subject_grades") val subjectGrades: List<GradeResponse> = emptyList()
)
