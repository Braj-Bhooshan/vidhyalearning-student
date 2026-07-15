package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.ChildSummary
import com.studentprofile.app.domain.models.LoginResponse
import com.studentprofile.app.domain.models.MpinRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StudentAuthApi {
    @GET("/api/v1/auth/student/my-children")
    suspend fun getMyChildren(): List<ChildSummary>

    @POST("/api/v1/auth/student/mpin/setup")
    suspend fun setupMpin(@Body request: MpinRequest): LoginResponse

    @POST("/api/v1/auth/student/mpin/verify")
    suspend fun verifyMpin(@Body request: MpinRequest): LoginResponse
}
