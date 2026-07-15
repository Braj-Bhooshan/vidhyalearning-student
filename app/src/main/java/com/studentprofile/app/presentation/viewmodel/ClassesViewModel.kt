package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.local.TenantProvider
import com.studentprofile.app.data.remote.ClassesApi
import com.studentprofile.app.data.remote.VideoApi
import com.studentprofile.app.domain.models.ParentScheduleItemDto
import com.studentprofile.app.domain.models.VideoContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Mirrors the parent app's video-launch states: a loading overlay while the content
// record is fetched, then the full-screen player once the URL is ready.
sealed interface ClassVideoState {
    data object Idle : ClassVideoState
    data object Loading : ClassVideoState
    data class Ready(val video: VideoContent) : ClassVideoState
    data class Error(val message: String) : ClassVideoState
}

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val classesApi: ClassesApi,
    private val videoApi: VideoApi,
    private val tenantProvider: TenantProvider
) : ViewModel() {

    private val _todaysClasses = MutableStateFlow<List<ParentScheduleItemDto>>(emptyList())
    val todaysClasses: StateFlow<List<ParentScheduleItemDto>> = _todaysClasses.asStateFlow()

    private val _pastClasses = MutableStateFlow<List<ParentScheduleItemDto>>(emptyList())
    val pastClasses: StateFlow<List<ParentScheduleItemDto>> = _pastClasses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _videoState = MutableStateFlow<ClassVideoState>(ClassVideoState.Idle)
    val videoState: StateFlow<ClassVideoState> = _videoState.asStateFlow()

    fun loadClasses(showPast: Boolean, studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (showPast) {
                    _pastClasses.value = classesApi.getTaughtClasses(studentId).items
                        .sortedByDescending { it.date }
                } else {
                    // Same as the parent app: show the whole day's timeline, taught and
                    // scheduled both - the card itself renders the status.
                    _todaysClasses.value = classesApi.getDailySchedule(studentId).items
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load classes."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAndPlayVideo(contentId: Int) {
        viewModelScope.launch {
            _videoState.value = ClassVideoState.Loading
            try {
                val video = videoApi.getVideoContent(contentId).toVideoContent()
                val resolvedUrl = if (video.url.startsWith("/")) {
                    tenantProvider.getBaseUrl().trimEnd('/') + video.url
                } else video.url
                _videoState.value = ClassVideoState.Ready(video.copy(url = resolvedUrl))
            } catch (e: Exception) {
                _videoState.value = ClassVideoState.Error(e.localizedMessage ?: "Failed to load video")
            }
        }
    }

    fun clearVideoState() {
        _videoState.value = ClassVideoState.Idle
    }
}
