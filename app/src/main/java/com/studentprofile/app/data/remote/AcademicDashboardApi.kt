package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.StudentClassRankResponse
import com.studentprofile.app.domain.models.StudentPerformanceTrendResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// tag "academic" - unlike GradesApi/AttendanceApi (tag "students"), auth scope for a parent-role
// token here is unconfirmed. Callers should treat failures as "field unavailable" and degrade
// gracefully rather than failing the whole screen - see StudentClassRankResponse doc comment.
interface AcademicDashboardApi {
    @GET("/api/v1/academic/student-dashboard/{student_id}/class-rank")
    suspend fun getClassRank(
        @Path("student_id") studentId: Int,
        @Query("grade_level") gradeLevel: String,
        @Query("section") section: String,
        @Query("academic_year") academicYear: String
    ): StudentClassRankResponse

    @GET("/api/v1/academic/student-dashboard/{student_id}/performance-trend")
    suspend fun getPerformanceTrend(
        @Path("student_id") studentId: Int,
        @Query("academic_year") academicYear: String
    ): StudentPerformanceTrendResponse
}

// Centralizes the "fetch class rank, treat any failure as unavailable" pattern shared by
// AverageScoreViewModel and SubjectPerformanceViewModel, so both screens degrade the same way
// (and only one place needs updating if that degradation policy ever changes) instead of each
// carrying its own copy-pasted precondition + try/catch.
suspend fun AcademicDashboardApi.getClassRankOrNull(
    studentId: Int,
    gradeLevel: String,
    section: String?,
    academicYear: String
): StudentClassRankResponse? {
    if (section.isNullOrBlank() || gradeLevel.isBlank() || academicYear.isBlank()) return null
    return try {
        getClassRank(studentId, gradeLevel, section, academicYear)
    } catch (e: Exception) {
        null
    }
}
