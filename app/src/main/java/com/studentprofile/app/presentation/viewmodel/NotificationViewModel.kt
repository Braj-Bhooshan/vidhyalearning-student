package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.remote.NotificationApi
import com.studentprofile.app.domain.models.MobileNotificationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationApi: NotificationApi
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<MobileNotificationResponse>>(emptyList())
    val notifications: StateFlow<List<MobileNotificationResponse>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = notificationApi.listNotifications()
                _notifications.value = response.items
                _unreadCount.value = response.unreadCount
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load notifications."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            try {
                _unreadCount.value = notificationApi.getUnreadCount().unreadCount
            } catch (_: Exception) {
            }
        }
    }

    // Optimistic update, mirroring MessageViewModel's best-effort mark-read call - a failure here
    // just means the badge/list is briefly out of sync with the server, not worth surfacing an error.
    fun markRead(notificationId: Int) {
        val target = _notifications.value.firstOrNull { it.id == notificationId } ?: return
        if (target.isRead) return
        _notifications.value = _notifications.value.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
        viewModelScope.launch {
            try {
                notificationApi.markRead(notificationId)
            } catch (_: Exception) {
            }
        }
    }

    fun markAllRead() {
        val previousNotifications = _notifications.value
        val previousUnreadCount = _unreadCount.value
        _notifications.value = previousNotifications.map { it.copy(isRead = true) }
        _unreadCount.value = 0
        viewModelScope.launch {
            try {
                if (!notificationApi.markAllRead().success) {
                    // Server explicitly rejected the request (a 200 with success=false, not an
                    // exception) - revert the optimistic update instead of staying out of sync.
                    _notifications.value = previousNotifications
                    _unreadCount.value = previousUnreadCount
                }
            } catch (_: Exception) {
            }
        }
    }
}
