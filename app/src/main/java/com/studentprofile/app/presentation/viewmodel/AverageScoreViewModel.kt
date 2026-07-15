package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.remote.AcademicDashboardApi
import com.studentprofile.app.data.remote.GradesApi
import com.studentprofile.app.data.remote.getClassRankOrNull
import com.studentprofile.app.domain.models.SubjectPerformance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AverageScoreUiState(
    val overallPercentage: Float = 0f,
    val overallGrade: String = "-",
    val highestScore: Float? = null,
    val lowestScore: Float? = null,
    val totalExams: Int = 0,
    val subjects: List<SubjectPerformance> = emptyList(),
    val classRank: Int? = null,
    val classTotal: Int? = null
)

@HiltViewModel
class AverageScoreViewModel @Inject constructor(
    private val gradesApi: GradesApi,
    private val academicDashboardApi: AcademicDashboardApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AverageScoreUiState())
    val uiState: StateFlow<AverageScoreUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load(studentId: Int, section: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val report = gradesApi.getGrades(studentId)
                val percentages = report.subjectGrades.mapNotNull { it.percentage?.toFloatOrNull() }
                _uiState.update {
                    it.copy(
                        overallPercentage = report.overallPercentage.toFloatOrNull() ?: 0f,
                        overallGrade = report.overallGrade.ifBlank { "-" },
                        highestScore = percentages.maxOrNull(),
                        lowestScore = percentages.minOrNull(),
                        totalExams = report.subjectGrades.size,
                        subjects = report.subjectGrades.map { g -> g.toSubjectPerformance() }
                    )
                }

                val rank = academicDashboardApi.getClassRankOrNull(studentId, report.gradeLevel, section, report.academicYear)
                if (rank?.rank != null && rank.total != null) {
                    _uiState.update { it.copy(classRank = rank.rank, classTotal = rank.total) }
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load average score."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
