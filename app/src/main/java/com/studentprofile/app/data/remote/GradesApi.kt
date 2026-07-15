package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.StudentGradeReport
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Confirmed against the local backend's /openapi.json (tag "students", OAuth2 bearer - same
// AuthInterceptor-injected token used by every other call in this app). Not confirmed whether a
// parent-role token is authorized to read a linked child's record here, same caveat as
// AttendanceApi/AcademicYearApi - verify with backend owner.
interface GradesApi {
    @GET("/api/v1/students/{student_id}/grades")
    suspend fun getGrades(
        @Path("student_id") studentId: Int,
        @Query("academic_year") academicYear: String? = null
    ): StudentGradeReport
}
