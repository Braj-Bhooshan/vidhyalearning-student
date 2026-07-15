package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors the backend's ParentScheduleItem - these mobile/parent endpoints are keyed by an
// explicit student_id since a parent account acts on behalf of a specific linked child,
// which is this app's actual login model (see AuthViewModel.selectStudent/completeLogin).
@Serializable
data class ParentScheduleItemDto(
    val id: Int,
    val date: String,
    val subject: String,
    @SerialName("grade_level") val gradeLevel: String = "",
    val section: String? = null,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("teacher_name") val teacherName: String? = null,
    @SerialName("chapter_title") val chapterTitle: String? = null,
    @SerialName("chapter_topic_title") val chapterTopicTitle: String? = null,
    @SerialName("topic_id") val topicId: Int? = null,
    @SerialName("video_title") val videoTitle: String? = null,
    @SerialName("content_id") val contentId: Int? = null,
    val status: String
) {
    val className: String get() = if (!section.isNullOrBlank()) "Grade $gradeLevel – $section" else "Grade $gradeLevel"
    val timeRange: String get() = if (startTime.isNotBlank() && endTime.isNotBlank()) "$startTime – $endTime" else ""
    val isTaught: Boolean get() = status.equals("taught", ignoreCase = true)
}

@Serializable
data class DailyScheduleResponse(
    val date: String,
    val total: Int = 0,
    @SerialName("taught_count") val taughtCount: Int = 0,
    @SerialName("pending_count") val pendingCount: Int = 0,
    val items: List<ParentScheduleItemDto> = emptyList()
)

@Serializable
data class TaughtClassesResponse(
    val total: Int = 0,
    val items: List<ParentScheduleItemDto> = emptyList()
)
