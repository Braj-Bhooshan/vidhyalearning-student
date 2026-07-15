package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.remote.AssignmentApi
import com.studentprofile.app.ui.subjectIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeworkSubjectStat(val subject: String, val submitted: Int, val total: Int, val iconRes: Int)

data class HomeworkStatsUiState(
    val submitted: Int = 0,
    val pending: Int = 0,
    val subjects: List<HomeworkSubjectStat> = emptyList()
)

@HiltViewModel
class HomeworkStatsViewModel @Inject constructor(
    private val assignmentApi: AssignmentApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeworkStatsUiState())
    val uiState: StateFlow<HomeworkStatsUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val assignedResponse = assignmentApi.getAssignments(studentId, status = "assigned")
                val completedResponse = assignmentApi.getAssignments(studentId, status = "completed")
                val subjectStats = (assignedResponse.items + completedResponse.items)
                    .groupBy { it.subject }
                    .map { (subject, items) ->
                        val (iconRes, _) = subjectIcons(subject)
                        HomeworkSubjectStat(
                            subject = subject,
                            submitted = items.count { it.status.equals("completed", ignoreCase = true) },
                            total = items.size,
                            iconRes = iconRes
                        )
                    }
                    .sortedByDescending { it.total }

                _uiState.value = HomeworkStatsUiState(
                    submitted = completedResponse.total,
                    pending = assignedResponse.total,
                    subjects = subjectStats
                )
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load homework stats."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
