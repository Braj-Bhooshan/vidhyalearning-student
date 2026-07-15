package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.AssignmentListResponse
import com.studentprofile.app.domain.models.AssignmentSubmissionResponse
import com.studentprofile.app.domain.models.AssignmentSubmitRequest
import com.studentprofile.app.domain.models.QuestionBankResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// The list endpoint is parent-role, keyed by student_id - same "parent picks a linked child"
// model as ClassesApi/AttendanceApi, since this app never authenticates as the student directly.
// The remaining endpoints stay on the mobile/student/* bearer-token-scoped routes.
interface AssignmentApi {
    @GET("/api/v1/mobile/parent/assignments")
    suspend fun getAssignments(
        @Query("student_id") studentId: Int,
        @Query("status") status: String? = null
    ): AssignmentListResponse

    @GET("/api/v1/mobile/parent/topics/{topicId}/question-bank")
    suspend fun getQuestionBank(
        @Path("topicId") topicId: Int,
        @Query("child_student_id") childStudentId: Int
    ): QuestionBankResponse

    @POST("/api/v1/mobile/student/assignments/{assignmentId}/submit")
    suspend fun submitAssignment(
        @Path("assignmentId") assignmentId: Int,
        @Body request: AssignmentSubmitRequest
    ): AssignmentSubmissionResponse

    @GET("/api/v1/mobile/student/assignments/{assignmentId}/my-submission")
    suspend fun getMySubmission(@Path("assignmentId") assignmentId: Int): AssignmentSubmissionResponse
}
