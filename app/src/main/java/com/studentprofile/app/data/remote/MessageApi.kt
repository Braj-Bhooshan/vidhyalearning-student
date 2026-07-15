package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.ChatMessage
import com.studentprofile.app.domain.models.CreateConversationRequest
import com.studentprofile.app.domain.models.MarkReadResponse
import com.studentprofile.app.domain.models.MessageConversation
import com.studentprofile.app.domain.models.SendMessageRequest
import com.studentprofile.app.domain.models.TeacherContact
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// All endpoints are resolved against the calling student's bearer token. The
// teachers list is scoped server-side to the student's own class, and starting
// a direct conversation is only permitted with a teacher who teaches that
// class - see communication_service._student_can_target on the backend.
interface MessageApi {
    @GET("/api/v1/mobile/student/teachers")
    suspend fun getMessageableTeachers(): List<TeacherContact>

    @GET("/api/v1/communication/conversations")
    suspend fun getConversations(): List<MessageConversation>

    @POST("/api/v1/communication/conversations")
    suspend fun createOrGetConversation(@Body request: CreateConversationRequest): MessageConversation

    @GET("/api/v1/communication/conversations/{conversationId}/messages")
    suspend fun getMessages(@Path("conversationId") conversationId: Int): List<ChatMessage>

    @POST("/api/v1/communication/conversations/{conversationId}/messages")
    suspend fun sendMessage(@Path("conversationId") conversationId: Int, @Body request: SendMessageRequest): ChatMessage

    @POST("/api/v1/communication/conversations/{conversationId}/read")
    suspend fun markConversationRead(@Path("conversationId") conversationId: Int): MarkReadResponse
}
