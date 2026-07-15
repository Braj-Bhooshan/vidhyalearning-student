package com.studentprofile.app.presentation.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studentprofile.app.domain.models.MobileNotificationResponse
import com.studentprofile.app.presentation.viewmodel.NotificationViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    studentName: String,
    classInfo: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadNotifications() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudentColors.NavyPrimary)
    ) {
        StudentTopBar(
            studentName = studentName,
            classInfo = classInfo,
            onMenuClick = { },
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
            onLogoutClick = onLogoutClick,
            onBackClick = onBack,
            unreadCount = unreadCount
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudentColors.BackgroundGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize().padding(top = 80.dp), contentAlignment = Alignment.TopCenter) {
                    CircularProgressIndicator(color = StudentColors.NavyPrimary)
                }
                error != null -> Column(
                    modifier = Modifier.fillMaxSize().padding(top = 80.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error ?: "", color = StudentColors.RedDue, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
                notifications.isEmpty() -> EmptyNotifications()
                else -> Column(modifier = Modifier.fillMaxSize()) {
                    if (unreadCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$unreadCount unread", color = StudentColors.TextSecondary, fontSize = 13.sp)
                            Text(
                                "Mark all as read",
                                color = StudentColors.BlueAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { viewModel.markAllRead() }
                            )
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(notifications, key = { it.id }) { notification ->
                            NotificationRow(notification, onClick = { viewModel.markRead(notification.id) })
                            HorizontalDivider(color = StudentColors.Divider)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            tint = Color(0xFFB0BEC5),
            modifier = Modifier.padding(top = 80.dp)
        )
        Text(
            "No Notifications Yet",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(20.dp),
            color = StudentColors.TextPrimary
        )
        Text(
            "We'll notify you when\nsomething important happens.",
            color = StudentColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun NotificationRow(notification: MobileNotificationResponse, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(if (notification.isRead) Color.Transparent else StudentColors.NotificationBadge)
        )
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                notification.title,
                fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                fontSize = 14.sp,
                color = StudentColors.TextPrimary
            )
            Text(
                notification.message,
                fontSize = 13.sp,
                color = StudentColors.TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                notification.createdAt,
                fontSize = 11.sp,
                color = StudentColors.TextTertiary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
