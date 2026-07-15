package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.remote.PerformanceApi
import com.studentprofile.app.domain.models.StudentExaminationMarkItem
import com.studentprofile.app.domain.models.StudentExaminationPerformanceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ALL_TERMS = "All Terms"

@HiltViewModel
class ExaminationViewModel @Inject constructor(
    private val performanceApi: PerformanceApi
) : ViewModel() {

    private val _report = MutableStateFlow<StudentExaminationPerformanceResponse?>(null)
    val report: StateFlow<StudentExaminationPerformanceResponse?> = _report.asStateFlow()

    private val _terms = MutableStateFlow<List<String>>(emptyList())
    val terms: StateFlow<List<String>> = _terms.asStateFlow()

    private val _selectedTerm = MutableStateFlow(ALL_TERMS)
    val selectedTerm: StateFlow<String> = _selectedTerm.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val filteredGrades: StateFlow<List<StudentExaminationMarkItem>>
        get() = _filteredGrades.asStateFlow()

    private val _filteredGrades = MutableStateFlow<List<StudentExaminationMarkItem>>(emptyList())

    fun selectTerm(term: String) {
        _selectedTerm.value = term
        applyTermFilter()
    }

    // studentId is unused - the dedicated performance endpoint resolves the student from the
    // caller's own bearer token, same as /mobile/student/teachers - but kept so callers don't
    // need a LaunchedEffect key change and so this stays consistent with the other Performance
    // tab's loadGradedAssignments(studentId) signature.
    fun loadGrades(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = performanceApi.getExaminationPerformance()
                _report.value = result
                val defaultTerms = listOf("Term 1", "Term 2", "Term 3", "Annual")
                _terms.value = (listOf(ALL_TERMS) + defaultTerms + result.availableTerms).distinct()
                _selectedTerm.value = ALL_TERMS
                applyTermFilter()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load examination results."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyTermFilter() {
        val allGrades = _report.value?.subjectGrades.orEmpty()
        _filteredGrades.value = if (_selectedTerm.value == ALL_TERMS) {
            allGrades
        } else {
            allGrades.filter { it.term == _selectedTerm.value }
        }
    }
}
