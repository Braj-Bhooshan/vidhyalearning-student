package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.remote.AssignmentApi
import com.studentprofile.app.data.remote.AttendanceApi
import com.studentprofile.app.data.remote.GradesApi
import com.studentprofile.app.domain.models.DashboardSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val attendanceApi: AttendanceApi,
    private val gradesApi: GradesApi,
    private val assignmentApi: AssignmentApi
) : ViewModel() {

    private val _summary = MutableStateFlow(DashboardSummary())
    val summary: StateFlow<DashboardSummary> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // The 4 calls below are independent, so they're fired concurrently rather than one after
    // another - each is wrapped in its own runCatching so one failing endpoint doesn't blank the
    // others, and the dashboard only surfaces an error if everything failed.
    fun loadDashboard(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _summary.value = DashboardSummary()

            val (dateFrom, dateTo) = recentAttendanceDateRange()
            var anySucceeded = false
            var lastError: String? = null

            coroutineScope {
                val attendanceDeferred = async { runCatching { attendanceApi.getAttendance(studentId = studentId, dateFrom = dateFrom, dateTo = dateTo) } }
                val gradesDeferred = async { runCatching { gradesApi.getGrades(studentId) } }
                val assignedDeferred = async { runCatching { assignmentApi.getAssignments(studentId, status = "assigned") } }
                val completedDeferred = async { runCatching { assignmentApi.getAssignments(studentId, status = "completed") } }

                attendanceDeferred.await()
                    .onSuccess { report ->
                        _summary.update {
                            it.copy(
                                attendancePercent = report.summary.attendancePercentage.toFloatOrNull() ?: 0f,
                                presentCount = report.summary.presentCount,
                                absentCount = report.summary.absentCount
                            )
                        }
                        anySucceeded = true
                    }
                    .onFailure { lastError = it.localizedMessage ?: "Failed to load attendance." }

                gradesDeferred.await()
                    .onSuccess { report ->
                        val recent = report.subjectGrades.takeLast(5).reversed().map { it.toRecentAssessment() }
                        _summary.update {
                            it.copy(
                                avgScore = report.overallPercentage.toFloatOrNull() ?: 0f,
                                grade = report.overallGrade.ifBlank { "-" },
                                subjectPerformances = report.subjectGrades.map { g -> g.toSubjectPerformance() },
                                recentAssessments = recent
                            )
                        }
                        anySucceeded = true
                    }
                    .onFailure { lastError = it.localizedMessage ?: "Failed to load grades." }

                val assignedResult = assignedDeferred.await()
                val completedResult = completedDeferred.await()
                if (assignedResult.isSuccess && completedResult.isSuccess) {
                    val assigned = assignedResult.getOrThrow()
                    val completed = completedResult.getOrThrow()
                    _summary.update { it.copy(homeworkSubmitted = completed.total, homeworkPending = assigned.total) }
                    anySucceeded = true
                } else {
                    lastError = assignedResult.exceptionOrNull()?.localizedMessage
                        ?: completedResult.exceptionOrNull()?.localizedMessage
                        ?: "Failed to load assignments."
                }
            }

            if (!anySucceeded) {
                _error.value = lastError ?: "Failed to load dashboard."
            }
            _isLoading.value = false
        }
    }
}
