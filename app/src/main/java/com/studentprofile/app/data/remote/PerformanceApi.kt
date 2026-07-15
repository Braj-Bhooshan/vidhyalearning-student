package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.StudentAssignmentPerformanceResponse
import com.studentprofile.app.domain.models.StudentExaminationPerformanceResponse
import retrofit2.http.GET
import retrofit2.http.Query

// Both endpoints are self-scoped: the backend resolves the student from the caller's own
// bearer token (RoleChecker(["student"])), not a student_id path/query param - same as
// MessageApi's /mobile/student/teachers.
interface PerformanceApi {
    @GET("/api/v1/mobile/student/performance/examination")
    suspend fun getExaminationPerformance(
        @Query("term") term: String? = null,
        @Query("academic_year") academicYear: String? = null
    ): StudentExaminationPerformanceResponse

    @GET("/api/v1/mobile/student/performance/assignments")
    suspend fun getAssignmentPerformance(
        @Query("subject") subject: String? = null
    ): StudentAssignmentPerformanceResponse
}
