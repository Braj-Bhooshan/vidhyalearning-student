package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors the backend's MobileNotificationResponse / NotificationListResponse / UnreadCountResponse,
// confirmed against the local backend's /openapi.json - see GET /api/v1/mobile/notifications/.
@Serializable
data class MobileNotificationResponse(
    val id: Int = 0,
    val title: String = "",
    val message: String = "",
    val type: String = "",
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("link_type") val linkType: String? = null,
    @SerialName("link_id") val linkId: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class NotificationListResponse(
    val items: List<MobileNotificationResponse> = emptyList(),
    val total: Int = 0,
    @SerialName("unread_count") val unreadCount: Int = 0,
    val page: Int = 1,
    @SerialName("page_size") val pageSize: Int = 50
)

@Serializable
data class UnreadCountResponse(
    @SerialName("unread_count") val unreadCount: Int = 0
)

@Serializable
data class MarkAllReadResponse(
    val success: Boolean = false
)
