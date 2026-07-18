package com.studentprofile.app.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentprofile.app.domain.models.QuestionItem
import com.studentprofile.app.domain.models.QuestionResponseDetail
import com.studentprofile.app.domain.models.StudentAnswer
import com.studentprofile.app.presentation.viewmodel.AssignmentViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun AssignmentDetailScreen(
    viewModel: AssignmentViewModel,
    gradeLevel: Int,
    studentId: Int,
    studentName: String,
    classInfo: String,
    onBack: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val assignment by viewModel.selectedAssignment.collectAsState()
    val questionBank by viewModel.questionBank.collectAsState()
    val mySubmission by viewModel.mySubmission.collectAsState()
    val noQuestionBank by viewModel.noQuestionBank.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(assignment?.id, studentId) {
        if (assignment != null && studentId != 0 && questionBank == null && !noQuestionBank && !isLoading) {
            viewModel.openAssignment(assignment!!, studentId)
        }
    }

    // Only meaningful for the grade < 4 interactive flow.
    val answers = remember(assignment?.id) { mutableStateMapOf<Int, StudentAnswer>() }

    Column(modifier = Modifier.fillMaxSize().background(StudentColors.NavyPrimary)) {
        StudentTopBar(
            studentName = studentName,
            classInfo = classInfo,
            onMenuClick = { },
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
            onLogoutClick = onLogoutClick,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudentColors.BackgroundGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Assignment", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StudentColors.TextPrimary)

            assignment?.let { a ->
                Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(a.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = StudentColors.TextPrimary)
                        a.description?.let {
                            Text(it, color = StudentColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            Text(a.subject, color = StudentColors.BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            a.dueDate?.let {
                                Text("  •  Due: $it", color = StudentColors.RedDue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            a.maxMarks?.let {
                                Text("  •  $it marks", color = StudentColors.TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            when {
                isLoading -> Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StudentColors.NavyPrimary)
                }

                error != null -> Column(modifier = Modifier.padding(top = 24.dp)) {
                    Text(
                        error ?: "Failed to load questions.",
                        color = StudentColors.RedDue,
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = { assignment?.let { viewModel.openAssignment(it, studentId) } },
                        colors = ButtonDefaults.buttonColors(containerColor = StudentColors.NavyPrimary),
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    ) {
                        Text("Retry")
                    }
                }

                noQuestionBank -> Text(
                    "No questions have been added to this assignment yet.",
                    color = StudentColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 24.dp)
                )

                mySubmission != null -> {
                    // Already completed - same terminal state for every grade. Grade < 4
                    // submissions carry per-question answers; a bare "mark as complete"
                    // submission has none, so fall back to the read-only question view.
                    val submission = mySubmission!!
                    SubmissionSummaryCard(
                        totalObtainedMarks = submission.totalObtainedMarks,
                        isGraded = submission.gradedAt != null,
                        maxMarks = assignment?.maxMarks,
                        hasAnswers = submission.questionResponses.isNotEmpty()
                    )
                    if (submission.questionResponses.isNotEmpty()) {
                        submission.questionResponses.forEach { detail -> SubmittedQuestionCard(detail) }
                    } else {
                        questionBank?.questions?.sortedBy { it.sortOrder }?.forEach { q ->
                            ReadOnlyQuestionCard(q)
                        }
                    }
                }

                gradeLevel >= 4 && questionBank != null -> {
                    // Grade >= 4: parent-style read-only questions, completed with one tap.
                    questionBank!!.questions.sortedBy { it.sortOrder }.forEach { q ->
                        ReadOnlyQuestionCard(q)
                    }
                    Button(
                        onClick = { viewModel.submitAnswers(questionBank!!.id, emptyList()) },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = StudentColors.NavyPrimary),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text(if (isSubmitting) "Completing..." else "Mark as Complete")
                    }
                }

                gradeLevel < 4 &&  questionBank != null -> {
                    // Grade < 4: answer MCQs in-app, then submit to complete.
                    questionBank!!.questions.sortedBy { it.sortOrder }.forEach { q ->
                        InteractiveQuestionCard(
                            question = q,
                            selected = answers[q.id],
                            onAnswer = { answer ->
                                if (answer == null) answers.remove(q.id) else answers[q.id] = answer
                            }
                        )
                    }
                    Button(
                        onClick = { viewModel.submitAnswers(questionBank!!.id, answers.values.toList()) },
                        enabled = !isSubmitting && answers.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = StudentColors.NavyPrimary),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text(if (isSubmitting) "Submitting..." else "Submit & Complete")
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmissionSummaryCard(totalObtainedMarks: Int, isGraded: Boolean, maxMarks: Int?, hasAnswers: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (hasAnswers) "Submitted" else "Completed",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = StudentColors.GreenSuccess
            )
            Text(
                when {
                    isGraded -> "Marks obtained: $totalObtainedMarks${maxMarks?.let { " / $it" } ?: ""}"
                    hasAnswers -> "Awaiting grading"
                    else -> "You have marked this assignment as complete."
                },
                color = StudentColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun QuestionCardShell(question: QuestionItem, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${question.questionNumber ?: ""}  ${question.questionText}".trim(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = StudentColors.TextPrimary
            )
            Text(
                "${question.marks} mark${if (question.marks == 1) "" else "s"}",
                color = StudentColors.TextTertiary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = StudentColors.Divider)
            content()
        }
    }
}

/** Grade >= 4: view-only rendering, matching the parent app's read-only question view. */
@Composable
private fun ReadOnlyQuestionCard(question: QuestionItem) {
    QuestionCardShell(question) {
        when (question.questionType) {
            "multiple_choice" -> {
                val labels = listOf("A", "B", "C", "D")
                question.options?.options?.forEachIndexed { index, option ->
                    Text(
                        "${labels.getOrElse(index) { "${index + 1}" }}. $option",
                        color = StudentColors.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            "card_flip" -> FlipCard(question)
            "long_description" -> AnswerLinesBox(height = 120.dp)
            else -> AnswerLinesBox(height = 64.dp)
        }
    }
}

@Composable
private fun AnswerLinesBox(height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(StudentColors.BackgroundLight, RoundedCornerShape(8.dp))
    )
}

/** Grade < 4, not yet submitted: MCQ is tap-to-select; descriptive questions get a text field when they need one. */
@Composable
private fun InteractiveQuestionCard(question: QuestionItem, selected: StudentAnswer?, onAnswer: (StudentAnswer?) -> Unit) {
    QuestionCardShell(question) {
        when (question.questionType) {
            "multiple_choice" -> {
                question.options?.options?.forEachIndexed { index, option ->
                    val isSelected = selected?.selectedOptionIndex == index
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAnswer(StudentAnswer(questionId = question.id, selectedOptionIndex = index))
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onAnswer(StudentAnswer(questionId = question.id, selectedOptionIndex = index)) },
                            colors = RadioButtonDefaults.colors(selectedColor = StudentColors.NavyPrimary)
                        )
                        Text(option, color = StudentColors.TextPrimary, fontSize = 14.sp)
                    }
                }
            }
            "card_flip" -> FlipCard(question)
            "short_description", "long_description" -> {
                if (question.requiresTextInput) {
                    OutlinedTextField(
                        value = selected?.studentAnswer ?: "",
                        onValueChange = { text ->
                            onAnswer(if (text.isBlank()) null else StudentAnswer(questionId = question.id, studentAnswer = text))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type your answer") },
                        minLines = if (question.questionType == "long_description") 4 else 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudentColors.NavyPrimary)
                    )
                } else {
                    Text(
                        "This question doesn't need an answer from you.",
                        color = StudentColors.TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
            else -> Text(
                "This question doesn't need an answer from you.",
                color = StudentColors.TextTertiary,
                fontSize = 12.sp
            )
        }
    }
}

/** Grade < 4, already submitted: read-only view of the student's own stored answer + marks if graded. */
@Composable
private fun SubmittedQuestionCard(detail: QuestionResponseDetail) {
    val question = detail.question
    if (question == null) {
        Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "A question in your submission could not be loaded.",
                    color = StudentColors.TextTertiary,
                    fontSize = 12.sp
                )
                detail.obtainedMarks?.let {
                    Text(
                        "Marks: $it",
                        color = StudentColors.GreenSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        return
    }
    QuestionCardShell(question) {
        when (question.questionType) {
            "multiple_choice" -> {
                val labels = listOf("A", "B", "C", "D")
                question.options?.options?.forEachIndexed { index, option ->
                    val isYourAnswer = detail.selectedOptionIndex == index
                    Text(
                        "${labels.getOrElse(index) { "${index + 1}" }}. $option${if (isYourAnswer) "  (Your answer)" else ""}",
                        color = if (isYourAnswer) StudentColors.NavyPrimary else StudentColors.TextSecondary,
                        fontWeight = if (isYourAnswer) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            "card_flip" -> FlipCard(question)
            "short_description", "long_description" -> {
                if (!detail.studentAnswer.isNullOrBlank()) {
                    Text(detail.studentAnswer, color = StudentColors.TextPrimary, fontSize = 13.sp)
                } else {
                    Text("Not answered", color = StudentColors.TextTertiary, fontSize = 12.sp)
                }
            }
            else -> Text("Not answered", color = StudentColors.TextTertiary, fontSize = 12.sp)
        }
        detail.obtainedMarks?.let {
            Text(
                "Marks: $it / ${question.marks}",
                color = StudentColors.GreenSuccess,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        detail.teacherNote?.let {
            Text(it, color = StudentColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun FlipCard(question: QuestionItem) {
    var flipped by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudentColors.BackgroundLight, RoundedCornerShape(10.dp))
            .clickable { flipped = !flipped }
            .padding(16.dp)
    ) {
        Text(
            if (flipped) (question.flipBackText ?: question.modelAnswer ?: "") else "Tap to reveal",
            color = StudentColors.TextPrimary,
            fontSize = 13.sp
        )
    }
}
