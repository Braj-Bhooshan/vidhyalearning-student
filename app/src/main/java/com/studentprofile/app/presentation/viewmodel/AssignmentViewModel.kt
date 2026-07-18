package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.local.AssignmentTopicPrefs
import com.studentprofile.app.data.remote.AssignmentApi
import com.studentprofile.app.data.remote.ClassesApi
import com.studentprofile.app.data.remote.MessageApi
import com.studentprofile.app.data.remote.PerformanceApi
import com.studentprofile.app.domain.models.AssignmentItem
import com.studentprofile.app.domain.models.AssignmentSubmissionResponse
import com.studentprofile.app.domain.models.AssignmentSubmitRequest
import com.studentprofile.app.domain.models.GradedAssignment
import com.studentprofile.app.domain.models.QuestionBankResponse
import com.studentprofile.app.domain.models.StudentAnswer
import com.studentprofile.app.domain.models.SubjectScoreTotal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

const val ALL_SUBJECTS = "All Subjects"

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val assignmentApi: AssignmentApi,
    private val classesApi: ClassesApi,
    private val messageApi: MessageApi,
    private val performanceApi: PerformanceApi,
    private val assignmentTopicPrefs: AssignmentTopicPrefs
) : ViewModel() {

    private val _assignments = MutableStateFlow<List<AssignmentItem>>(emptyList())
    val assignments: StateFlow<List<AssignmentItem>> = _assignments.asStateFlow()

    private val _selectedAssignment = MutableStateFlow<AssignmentItem?>(null)
    val selectedAssignment: StateFlow<AssignmentItem?> = _selectedAssignment.asStateFlow()

    private val _questionBank = MutableStateFlow<QuestionBankResponse?>(null)
    val questionBank: StateFlow<QuestionBankResponse?> = _questionBank.asStateFlow()

    private val _mySubmission = MutableStateFlow<AssignmentSubmissionResponse?>(null)
    val mySubmission: StateFlow<AssignmentSubmissionResponse?> = _mySubmission.asStateFlow()

    // True when the assignment has no linked question bank (topicId null, or a 404 from the backend).
    private val _noQuestionBank = MutableStateFlow(false)
    val noQuestionBank: StateFlow<Boolean> = _noQuestionBank.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Performance > Assignment tab: graded assignments, filterable by subject, plus the
    // per-subject score totals derived from the same data.
    private val _gradedAssignments = MutableStateFlow<List<GradedAssignment>>(emptyList())

    private val _gradeSubjects = MutableStateFlow<List<String>>(listOf(ALL_SUBJECTS))
    val gradeSubjects: StateFlow<List<String>> = _gradeSubjects.asStateFlow()

    private val _selectedGradeSubject = MutableStateFlow(ALL_SUBJECTS)
    val selectedGradeSubject: StateFlow<String> = _selectedGradeSubject.asStateFlow()

    private val _filteredGradedAssignments = MutableStateFlow<List<GradedAssignment>>(emptyList())
    val filteredGradedAssignments: StateFlow<List<GradedAssignment>> = _filteredGradedAssignments.asStateFlow()

    private val _subjectScoreTotals = MutableStateFlow<List<SubjectScoreTotal>>(emptyList())
    val subjectScoreTotals: StateFlow<List<SubjectScoreTotal>> = _subjectScoreTotals.asStateFlow()

    private val _isLoadingGrades = MutableStateFlow(false)
    val isLoadingGrades: StateFlow<Boolean> = _isLoadingGrades.asStateFlow()

    private val _gradesError = MutableStateFlow<String?>(null)
    val gradesError: StateFlow<String?> = _gradesError.asStateFlow()

    fun selectGradeSubject(subject: String) {
        _selectedGradeSubject.value = subject
        applyGradeSubjectFilter()
    }

    private fun applyGradeSubjectFilter() {
        _filteredGradedAssignments.value = if (_selectedGradeSubject.value == ALL_SUBJECTS) {
            _gradedAssignments.value
        } else {
            _gradedAssignments.value.filter { it.subject == _selectedGradeSubject.value }
        }
    }

    /**
     * Loads this student's assignment performance from the dedicated backend endpoint
     * (GET /mobile/student/performance/assignments), which already merges both the
     * question-by-question submission flow and the teacher's bulk "direct marks" flow and
     * computes each grade_letter server-side. That's paired with the full set of subjects
     * actually assigned to this student's class/section (via /mobile/student/teachers - the
     * same data the Message tab's teacher contact list uses), so the subject dropdown and
     * the "Total Score by Subject" tab show every assigned subject, not just the subset that
     * happens to have a graded assignment so far - a subject with no grades yet still shows
     * up with an empty/zero row.
     */
    fun loadGradedAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoadingGrades.value = true
            _gradesError.value = null
            try {
                val assignedSubjectsDeferred = async {
                    try {
                        messageApi.getMessageableTeachers()
                            .flatMap { it.subject?.split(",").orEmpty() }
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    } catch (_: Exception) {
                        emptyList()
                    }
                }

                val performance = performanceApi.getAssignmentPerformance()
                val graded = performance.assignmentGrades.map { item ->
                    GradedAssignment(
                        assignmentId = item.assignmentId,
                        title = item.title,
                        subject = item.subject,
                        maxMarks = item.maxMarks ?: 0f,
                        obtainedMarks = item.obtainedMarks,
                        percentage = item.percentage,
                        gradeLetter = item.gradeLetter,
                        gradedAt = item.gradedAt
                    )
                }

                val assignedSubjects = assignedSubjectsDeferred.await()
                // Union in any subject that only shows up via a graded assignment (e.g. the
                // teacher roster changed since it was assigned) so no grade is ever hidden.
                val allSubjects = (assignedSubjects + performance.availableSubjects).distinct().sorted()

                _gradedAssignments.value = graded
                _gradeSubjects.value = listOf(ALL_SUBJECTS) + allSubjects
                _selectedGradeSubject.value = ALL_SUBJECTS
                applyGradeSubjectFilter()

                val totalsBySubject = performance.subjectTotals.associateBy { it.subject }
                _subjectScoreTotals.value = allSubjects.map { subject ->
                    val total = totalsBySubject[subject]
                    SubjectScoreTotal(
                        subject = subject,
                        assignmentCount = total?.assignmentCount ?: 0,
                        totalObtainedMarks = total?.totalObtainedMarks ?: 0f,
                        totalMaxMarks = total?.totalMaxMarks ?: 0f,
                        percentage = total?.percentage,
                        gradeLetter = total?.gradeLetter
                    )
                }
            } catch (e: Exception) {
                _gradesError.value = e.localizedMessage ?: "Failed to load assignment grades."
            } finally {
                _isLoadingGrades.value = false
            }
        }
    }

    fun loadAssignments(studentId: Int, status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _assignments.value = assignmentApi.getAssignments(studentId, status).items
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load assignments."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Opens an assignment's detail view, mirroring the parent app's flow: the parent
     * assignments list carries no topic_id, so it's resolved from the assignment itself,
     * then the local cache, then by title-matching against taught-classes (and cached).
     * The submission is fetched for every grade - grade >= 4 students don't answer
     * questions but still mark assignments complete, which is stored as a submission.
     */
    fun openAssignment(assignment: AssignmentItem, studentId: Int) {
        if (_selectedAssignment.value?.id == assignment.id && (_isLoading.value || _questionBank.value != null)) {
            return
        }
        _selectedAssignment.value = assignment
        _questionBank.value = null
        _mySubmission.value = null
        _noQuestionBank.value = false
        _error.value = null

        viewModelScope.launch {
            _isLoading.value = true
            try {
                try {
                    _mySubmission.value = assignmentApi.getMySubmission(assignment.id)
                } catch (_: Exception) {
                    // No submission yet - expected 404, nothing to show.
                }

                var topicId = assignment.topicId ?: assignmentTopicPrefs.getTopicId(assignment.id)
                if (topicId == null) {
                    try {
                        topicId = classesApi.getTaughtClasses(studentId).items.find {
                            it.chapterTopicTitle?.equals(assignment.title, ignoreCase = true) == true ||
                                it.chapterTitle?.equals(assignment.title, ignoreCase = true) == true
                        }?.topicId
                        if (topicId != null) {
                            assignmentTopicPrefs.putTopicId(assignment.id, topicId)
                        }
                    } catch (_: Exception) {
                        // Fall through to the no-question-bank state below.
                    }
                }

                if (topicId == null) {
                    // No topic link stored for this assignment (created without a curriculum
                    // link) and none could be resolved by title-matching taught classes -
                    // there's nothing to guess here. Guessing topic_id = assignment.id is
                    // unsound: it can collide with an unrelated real ClassTopic id belonging
                    // to a different class/section and gets a 403, not a clean 404.
                    _noQuestionBank.value = true
                } else {
                    try {
                        _questionBank.value = assignmentApi.getQuestionBank(topicId, studentId)
                    } catch (e: HttpException) {
                        if (e.code() == 404 || e.code() == 403) {
                            // 404 = topic has no bank; 403 = resolved topic isn't in this
                            // student's class/section (e.g. a stale cached id) - both mean
                            // there's no bank this student can see, not a retryable error.
                            _noQuestionBank.value = true
                        } else {
                            _error.value = e.localizedMessage ?: "Failed to load questions."
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load this assignment."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitAnswers(bankId: Int, answers: List<StudentAnswer>) {
        val assignment = _selectedAssignment.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            try {
                _mySubmission.value = assignmentApi.submitAssignment(
                    assignment.id,
                    AssignmentSubmitRequest(bankId, answers)
                )
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to submit assignment."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
