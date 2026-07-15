package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.DailyScheduleResponse
import com.studentprofile.app.domain.models.TaughtClassesResponse
import retrofit2.http.GET
import retrofit2.http.Query

// Parent-role endpoints keyed by student_id - the same "parent picks a linked child" model
// this app's login flow already uses, rather than a bearer-token-scoped student endpoint.
interface ClassesApi {
    @GET("/api/v1/mobile/parent/daily-schedule")
    suspend fun getDailySchedule(@Query("student_id") studentId: Int): DailyScheduleResponse

    @GET("/api/v1/mobile/parent/taught-classes")
    suspend fun getTaughtClasses(@Query("student_id") studentId: Int): TaughtClassesResponse
}
