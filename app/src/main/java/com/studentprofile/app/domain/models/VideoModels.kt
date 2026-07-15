package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Matches items in the List returned by GET /api/v1/videos/{video_id}/watermark
@Serializable
data class WatermarkConfig(
    val id: Int = 0,
    val name: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
    val style: String = "text",
    val text: String? = null,
    @SerialName("text_color") val textColor: String = "#FFFFFF",
    @SerialName("font_size") val fontSize: Int = 24,
    val opacity: Float = 0.5f,
    val position: String = "moving",
    @SerialName("add_user_info") val addUserInfo: Boolean = true,
    @SerialName("add_timestamp") val addTimestamp: Boolean = true,
    @SerialName("video_id") val videoId: Int? = null,
    @SerialName("tenant_id") val tenantId: Int? = null,
)

data class VideoContent(
    val id: String = "",
    val title: String = "",
    val url: String = ""
)

// Matches the backend Content schema returned by GET /api/v1/content/{content_id}
@Serializable
data class ContentApiItem(
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    @SerialName("file_url") val fileUrl: String? = null,
    @SerialName("external_url") val externalUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("content_type") val contentType: String? = null,
) {
    fun toVideoContent() = VideoContent(
        id = id.toString(),
        title = title,
        url = externalUrl ?: fileUrl ?: ""
    )
}
