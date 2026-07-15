package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.StudentDetail
import retrofit2.http.GET
import retrofit2.http.Path

interface ProfileApi {
    @GET("/api/v1/students/{student_id}")
    suspend fun getStudent(@Path("student_id") studentId: Int): StudentDetail
}
