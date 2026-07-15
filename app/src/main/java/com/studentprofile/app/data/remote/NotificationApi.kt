package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.MarkAllReadResponse
import com.studentprofile.app.domain.models.MobileNotificationResponse
import com.studentprofile.app.domain.models.NotificationListResponse
import com.studentprofile.app.domain.models.UnreadCountResponse
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("/api/v1/mobile/notifications/")
    suspend fun listNotifications(
        @Query("unread_only") unreadOnly: Boolean? = null,
        @Query("skip") skip: Int? = null,
        @Query("limit") limit: Int? = null
    ): NotificationListResponse

    @GET("/api/v1/mobile/notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @PATCH("/api/v1/mobile/notifications/{notificationId}/read")
    suspend fun markRead(@Path("notificationId") notificationId: Int): MobileNotificationResponse

    @POST("/api/v1/mobile/notifications/mark-all-read")
    suspend fun markAllRead(): MarkAllReadResponse
}
