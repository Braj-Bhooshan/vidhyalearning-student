package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignmentItem(
    val id: Int,
    val title: String,
    val description: String? = null,
    @SerialName("grade_level") val gradeLevel: String,
    val section: String? = null,
    val subject: String,
    @SerialName("academic_year") val academicYear: String,
    val term: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("max_marks") val maxMarks: Int? = null,
    val status: String,
    @SerialName("content_type") val contentType: String,
    @SerialName("submission_count") val submissionCount: Int = 0,
    @SerialName("total_students") val totalStudents: Int = 0,
    @SerialName("teacher_id") val teacherId: Int,
    @SerialName("topic_id") val topicId: Int? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class AssignmentListResponse(
    val items: List<AssignmentItem>,
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("page_size") val pageSize: Int = 50
)

@Serializable
data class MCQOptions(
    val options: List<String> = emptyList(),
    @SerialName("correct_index") val correctIndex: Int = 0
)

@Serializable
data class QuestionItem(
    val id: Int,
    @SerialName("bank_id") val bankId: Int,
    // multiple_choice | short_description | long_description | card_flip
    @SerialName("question_type") val questionType: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    val marks: Int = 1,
    @SerialName("question_text") val questionText: String,
    val options: MCQOptions? = null,
    @SerialName("model_answer") val modelAnswer: String? = null,
    @SerialName("flip_back_text") val flipBackText: String? = null,
    @SerialName("requires_text_input") val requiresTextInput: Boolean = false,
    @SerialName("question_number") val questionNumber: String? = null
)

@Serializable
data class QuestionBankResponse(
    val id: Int,
    @SerialName("topic_id") val topicId: Int,
    @SerialName("content_id") val contentId: Int,
    val title: String,
    val status: String,
    @SerialName("total_marks") val totalMarks: Int,
    val questions: List<QuestionItem> = emptyList()
)

@Serializable
data class StudentAnswer(
    @SerialName("question_id") val questionId: Int,
    @SerialName("student_answer") val studentAnswer: String? = null,
    @SerialName("selected_option_index") val selectedOptionIndex: Int? = null
)

@Serializable
data class AssignmentSubmitRequest(
    @SerialName("bank_id") val bankId: Int,
    val answers: List<StudentAnswer>
)

@Serializable
data class QuestionResponseDetail(
    val id: Int,
    @SerialName("submission_id") val submissionId: Int,
    @SerialName("question_id") val questionId: Int,
    @SerialName("student_answer") val studentAnswer: String? = null,
    @SerialName("selected_option_index") val selectedOptionIndex: Int? = null,
    @SerialName("obtained_marks") val obtainedMarks: Int? = null,
    @SerialName("teacher_note") val teacherNote: String? = null,
    val question: QuestionItem? = null
)

@Serializable
data class AssignmentSubmissionResponse(
    val id: Int,
    @SerialName("assignment_id") val assignmentId: Int,
    @SerialName("student_id") val studentId: Int,
    @SerialName("bank_id") val bankId: Int,
    val status: String,
    @SerialName("total_obtained_marks") val totalObtainedMarks: Int,
    @SerialName("submitted_at") val submittedAt: String? = null,
    @SerialName("graded_at") val gradedAt: String? = null,
    @SerialName("question_responses") val questionResponses: List<QuestionResponseDetail> = emptyList()
)

// View-only model for the Performance > Assignment tab, sourced directly from
// StudentAssignmentGradeItem (GET /mobile/student/performance/assignments). The backend
// merges both the question-by-question submission flow and the teacher's bulk "direct
// marks" flow into this, and computes grade_letter itself (same calculate_grade_letter
// scale used for examinations), so the app never has to guess a grade client-side.
data class GradedAssignment(
    val assignmentId: Int,
    val title: String,
    val subject: String,
    val maxMarks: Float,
    val obtainedMarks: Float,
    val percentage: Float?,
    val gradeLetter: String?,
    val gradedAt: String? = null
)

// Aggregated total score per subject, sourced directly from StudentSubjectAssignmentTotal -
// percentage and gradeLetter are both backend-computed, not derived client-side.
data class SubjectScoreTotal(
    val subject: String,
    val assignmentCount: Int,
    val totalObtainedMarks: Float,
    val totalMaxMarks: Float,
    val percentage: Float?,
    val gradeLetter: String?
)
