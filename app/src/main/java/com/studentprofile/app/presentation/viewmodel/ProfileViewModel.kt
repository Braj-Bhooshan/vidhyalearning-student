package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.local.TenantProvider
import com.studentprofile.app.data.remote.ProfileApi
import com.studentprofile.app.domain.models.StudentDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileApi: ProfileApi,
    private val tenantProvider: TenantProvider
) : ViewModel() {

    private val _detail = MutableStateFlow<StudentDetail?>(null)
    val detail: StateFlow<StudentDetail?> = _detail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // AuthInterceptor rewrites the host/port of every outgoing request, so an already-absolute
    // URL doesn't need rewriting here - only a backend-relative path needs the base URL prefixed.
    fun resolvePhotoUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return when {
            raw.startsWith("http://") || raw.startsWith("https://") -> raw
            else -> "${tenantProvider.getBaseUrl().trimEnd('/')}$raw"
        }
    }

    fun load(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _detail.value = profileApi.getStudent(studentId)
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load profile."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
