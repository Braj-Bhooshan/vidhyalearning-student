package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.remote.MessageApi
import com.studentprofile.app.domain.models.ChatMessage
import com.studentprofile.app.domain.models.ConversationMemberCreate
import com.studentprofile.app.domain.models.CreateConversationRequest
import com.studentprofile.app.domain.models.MessageConversation
import com.studentprofile.app.domain.models.SendMessageRequest
import com.studentprofile.app.domain.models.TeacherContact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageApi: MessageApi
) : ViewModel() {

    private val _teachers = MutableStateFlow<List<TeacherContact>>(emptyList())
    val teachers: StateFlow<List<TeacherContact>> = _teachers.asStateFlow()

    private val _conversations = MutableStateFlow<List<MessageConversation>>(emptyList())
    val conversations: StateFlow<List<MessageConversation>> = _conversations.asStateFlow()

    private val _selectedTeacher = MutableStateFlow<TeacherContact?>(null)
    val selectedTeacher: StateFlow<TeacherContact?> = _selectedTeacher.asStateFlow()

    private val _selectedConversation = MutableStateFlow<MessageConversation?>(null)

    private val _isComposing = MutableStateFlow(false)
    val isComposing: StateFlow<Boolean> = _isComposing.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var contactsLoaded = false

    fun loadContactsIfNeeded() {
        if (contactsLoaded) return
        contactsLoaded = true
        refreshContacts()
    }

    fun refreshContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _teachers.value = messageApi.getMessageableTeachers()
                _conversations.value = messageApi.getConversations()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load messages."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Inbox row tapped: conversation already exists, just load its messages. */
    fun openConversation(conversation: MessageConversation) {
        _isComposing.value = false
        _selectedTeacher.value = conversation.teacherMember()?.let { member ->
            TeacherContact(id = member.actorId, fullName = member.fullName)
        }
        _selectedConversation.value = conversation
        _messages.value = emptyList()
        loadMessagesForSelectedConversation()
    }

    /** New Message flow: a teacher was picked from the compose recipient list. */
    fun startConversationWithTeacher(teacher: TeacherContact) {
        _isComposing.value = false
        _selectedTeacher.value = teacher
        _selectedConversation.value = null
        _messages.value = emptyList()
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val conversation = messageApi.createOrGetConversation(
                    CreateConversationRequest(
                        type = "direct",
                        title = teacher.fullName,
                        members = listOf(ConversationMemberCreate(actorType = "teacher", actorId = teacher.id))
                    )
                )
                _selectedConversation.value = conversation
                _isLoading.value = false
                loadMessagesForSelectedConversation()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to start conversation."
                _isLoading.value = false
            }
        }
    }

    private fun loadMessagesForSelectedConversation() {
        val conversation = _selectedConversation.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _messages.value = messageApi.getMessages(conversation.id)
                try {
                    messageApi.markConversationRead(conversation.id)
                } catch (_: Exception) {
                    // best-effort; failing to mark as read shouldn't block viewing the chat
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to open conversation."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startCompose() {
        _isComposing.value = true
    }

    fun cancelCompose() {
        _isComposing.value = false
    }

    fun closeConversation() {
        _selectedTeacher.value = null
        _selectedConversation.value = null
        _messages.value = emptyList()
    }

    fun sendMessage(content: String) {
        val conversation = _selectedConversation.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            _isSending.value = true
            _error.value = null
            try {
                val sent = messageApi.sendMessage(conversation.id, SendMessageRequest(body = content.trim()))
                _messages.value = _messages.value + sent
                refreshContacts()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to send message."
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
