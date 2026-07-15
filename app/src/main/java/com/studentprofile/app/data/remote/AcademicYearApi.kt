package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.AcademicYearListResponse
import retrofit2.http.GET

// Confirmed against the local backend's /openapi.json (tag "academic-years", OAuth2 bearer -
// same AuthInterceptor-injected token/tenant headers as every other call). Not confirmed whether
// a parent-role token is authorized here, same caveat as AttendanceApi/ClassesApi.
interface AcademicYearApi {
    @GET("/api/v1/academic-years/")
    suspend fun listAcademicYears(): AcademicYearListResponse
}
