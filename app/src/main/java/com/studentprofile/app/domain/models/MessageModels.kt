package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// A student may only message the teachers who teach their class - the backend
// scopes /mobile/student/teachers and every /communication endpoint below to
// the caller's own teachers, so no other student or staff member is reachable.
@Serializable
data class TeacherContact(
    val id: Int,
    @SerialName("full_name") val fullName: String,
    val subject: String? = null,
    val department: String? = null
)

@Serializable
data class ConversationMember(
    val id: Int,
    @SerialName("actor_type") val actorType: String,
    @SerialName("actor_id") val actorId: Int,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: String = "member",
    @SerialName("can_send") val canSend: Boolean = true,
    @SerialName("is_muted") val isMuted: Boolean = false
)

@Serializable
data class MessageConversation(
    val id: Int,
    val type: String,
    val title: String,
    val description: String? = null,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("is_locked") val isLocked: Boolean = false,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("last_message_preview") val lastMessagePreview: String? = null,
    val members: List<ConversationMember> = emptyList()
) {
    fun teacherMember(): ConversationMember? = members.firstOrNull { it.actorType == "teacher" }
}

@Serializable
data class ChatMessage(
    val id: Int,
    @SerialName("conversation_id") val conversationId: Int,
    @SerialName("sender_actor_type") val senderActorType: String,
    @SerialName("sender_actor_id") val senderActorId: Int,
    @SerialName("sender_name") val senderName: String,
    val body: String,
    @SerialName("message_type") val messageType: String = "text",
    @SerialName("sent_at") val sentAt: String
)

@Serializable
data class SendMessageRequest(
    val body: String,
    @SerialName("message_type") val messageType: String = "text"
)

@Serializable
data class ConversationMemberCreate(
    @SerialName("actor_type") val actorType: String,
    @SerialName("actor_id") val actorId: Int
)

@Serializable
data class CreateConversationRequest(
    @SerialName("type") val type: String,
    @SerialName("title") val title: String,
    @SerialName("members") val members: List<ConversationMemberCreate>
)

@Serializable
data class MarkReadResponse(
    val success: Boolean = true
)
