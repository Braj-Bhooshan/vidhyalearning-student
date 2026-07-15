package com.studentprofile.app.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studentprofile.app.domain.models.ChatMessage
import com.studentprofile.app.domain.models.MessageConversation
import com.studentprofile.app.domain.models.TeacherContact
import com.studentprofile.app.presentation.viewmodel.MessageViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun MessageScreen(
    studentName: String,
    classInfo: String,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: MessageViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadContactsIfNeeded() }

    val teachers by viewModel.teachers.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val selectedTeacher by viewModel.selectedTeacher.collectAsState()
    val isComposing by viewModel.isComposing.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudentColors.NavyPrimary)
        ) {
            StudentTopBar(
                studentName = studentName,
                classInfo = classInfo,
                onMenuClick = onMenuClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                onLogoutClick = onLogoutClick,
                onBackClick = when {
                    selectedTeacher != null -> ({ viewModel.closeConversation() })
                    isComposing -> ({ viewModel.cancelCompose() })
                    else -> null
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StudentColors.BackgroundGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                when {
                    selectedTeacher != null -> MessageChatContent(
                        teacher = selectedTeacher!!,
                        messages = messages,
                        isLoading = isLoading,
                        isSending = isSending,
                        error = error,
                        onSend = viewModel::sendMessage
                    )
                    isComposing -> NewMessageContent(
                        teachers = teachers,
                        isLoading = isLoading,
                        error = error,
                        onSelectTeacher = viewModel::startConversationWithTeacher
                    )
                    else -> MessageInboxContent(
                        conversations = conversations,
                        hasTeachers = teachers.isNotEmpty(),
                        isLoading = isLoading,
                        error = error,
                        onOpenConversation = viewModel::openConversation,
                        onNewMessage = viewModel::startCompose
                    )
                }
            }
        }

        if (selectedTeacher == null && !isComposing) {
            FloatingActionButton(
                onClick = viewModel::startCompose,
                containerColor = StudentColors.NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New message")
            }
        }
    }
}

@Composable
private fun MessageInboxContent(
    conversations: List<MessageConversation>,
    hasTeachers: Boolean,
    isLoading: Boolean,
    error: String?,
    onOpenConversation: (MessageConversation) -> Unit,
    onNewMessage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text("Message", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StudentColors.TextPrimary)
        Text(
            "Your conversations with teachers.",
            color = StudentColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (error != null) {
            Text(
                error,
                color = StudentColors.RedDue,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (isLoading && conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StudentColors.NavyPrimary)
            }
        } else if (conversations.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Message,
                    contentDescription = null,
                    tint = StudentColors.TextTertiary
                )
                Text(
                    "No conversations yet",
                    fontWeight = FontWeight.Bold,
                    color = StudentColors.TextSecondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    if (hasTeachers) {
                        "Tap the + button to message one of your teachers."
                    } else {
                        "No teachers available yet. You might need to send a request for approval to start chatting."
                    },
                    color = StudentColors.TextTertiary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    if (hasTeachers) "Start a conversation" else "Send for approval",
                    color = StudentColors.NavyPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .background(StudentColors.BlueLight, RoundedCornerShape(50))
                        .clickable { onNewMessage() }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)) {
                items(conversations) { conversation ->
                    ConversationRow(conversation = conversation, onClick = { onOpenConversation(conversation) })
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: MessageConversation,
    onClick: () -> Unit
) {
    val teacher = conversation.teacherMember()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(StudentColors.BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (teacher?.fullName ?: conversation.title).take(1).uppercase(),
                    color = StudentColors.NavyPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    teacher?.fullName ?: conversation.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = StudentColors.TextPrimary
                )
                Text(
                    conversation.lastMessagePreview ?: "Tap to open",
                    color = StudentColors.TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(StudentColors.GreenSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conversation.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NewMessageContent(
    teachers: List<TeacherContact>,
    isLoading: Boolean,
    error: String?,
    onSelectTeacher: (TeacherContact) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(teachers, query) {
        if (query.isBlank()) {
            teachers
        } else {
            teachers.filter {
                it.fullName.contains(query, ignoreCase = true) || it.subject?.contains(query, ignoreCase = true) == true
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text("New Message", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StudentColors.TextPrimary)
        Text(
            "Choose a teacher to message. Students can only message their own teachers.",
            color = StudentColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            placeholder = { Text("Search teachers") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        if (error != null) {
            Text(
                error,
                color = StudentColors.RedDue,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (isLoading && teachers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StudentColors.NavyPrimary)
            }
        } else if (filtered.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (teachers.isEmpty()) "No teachers available to message yet." else "No teachers match \"$query\".",
                    color = StudentColors.TextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                if (teachers.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Send for approval",
                        color = StudentColors.NavyPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .background(StudentColors.BlueLight, RoundedCornerShape(50))
                            .clickable { /* This could trigger an approval request flow */ }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)) {
                items(filtered) { teacher ->
                    NewMessageTeacherRow(teacher = teacher, onClick = { onSelectTeacher(teacher) })
                }
            }
        }
    }
}

@Composable
private fun NewMessageTeacherRow(
    teacher: TeacherContact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(StudentColors.BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    teacher.fullName.take(1).uppercase(),
                    color = StudentColors.NavyPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(teacher.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StudentColors.TextPrimary)
                if (!teacher.subject.isNullOrBlank()) {
                    Text(teacher.subject, color = StudentColors.NavyPrimary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun MessageChatContent(
    teacher: TeacherContact,
    messages: List<ChatMessage>,
    isLoading: Boolean,
    isSending: Boolean,
    error: String?,
    onSend: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(StudentColors.BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = StudentColors.NavyPrimary)
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(teacher.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StudentColors.TextPrimary)
                if (!teacher.subject.isNullOrBlank()) {
                    Text(teacher.subject, color = StudentColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }

        if (error != null) {
            Text(
                error,
                color = StudentColors.RedDue,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading && messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StudentColors.NavyPrimary)
                }
            } else if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Send your first message to ${teacher.fullName}.",
                        color = StudentColors.TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") },
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (draft.isNotBlank() && !isSending) {
                        onSend(draft)
                        draft = ""
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = StudentColors.NavyPrimary)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val fromStudent = message.senderActorType.equals("student", ignoreCase = true)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (fromStudent) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (fromStudent) StudentColors.NavyPrimary else Color.White,
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                message.body,
                color = if (fromStudent) Color.White else StudentColors.TextPrimary,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
        }
    }
}
