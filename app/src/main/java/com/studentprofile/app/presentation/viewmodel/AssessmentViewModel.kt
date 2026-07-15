package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.remote.GradesApi
import com.studentprofile.app.domain.models.RecentAssessment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssessmentUiState(
    val totalTests: Int = 0,
    val averageScore: Float = 0f,
    val bestGrade: String = "-",
    val assessments: List<RecentAssessment> = emptyList()
)

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    private val gradesApi: GradesApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentUiState())
    val uiState: StateFlow<AssessmentUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val report = gradesApi.getGrades(studentId)
                val bestSubject = report.subjectGrades.maxByOrNull { it.percentage?.toFloatOrNull() ?: -1f }
                _uiState.value = AssessmentUiState(
                    totalTests = report.subjectGrades.size,
                    averageScore = report.overallPercentage.toFloatOrNull() ?: 0f,
                    bestGrade = bestSubject?.gradeLetter ?: report.overallGrade.ifBlank { "-" },
                    assessments = report.subjectGrades.reversed().map { it.toRecentAssessment() }
                )
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load assessments."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
