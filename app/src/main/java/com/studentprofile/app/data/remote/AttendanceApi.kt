package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.StudentAttendanceReport
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Confirmed against the local backend's /openapi.json (tag "students", OAuth2 bearer - same
// AuthInterceptor-injected token used by every other call in this app). Not confirmed whether a
// parent-role token is authorized to read a linked child's record here, same caveat as
// ClassesApi's parent/* endpoints - verify with backend owner.
interface AttendanceApi {
    @GET("/api/v1/students/{student_id}/attendance")
    suspend fun getAttendance(
        @Path("student_id") studentId: Int,
        @Query("date_from") dateFrom: String,
        @Query("date_to") dateTo: String
    ): StudentAttendanceReport
}
