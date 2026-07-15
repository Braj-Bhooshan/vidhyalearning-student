package com.studentprofile.app.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentprofile.app.domain.models.AssignmentItem
import com.studentprofile.app.presentation.viewmodel.AssignmentViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.StudentTopBar

internal fun subjectColors(subject: String): Pair<Color, Color> = when {
    subject.contains("math", ignoreCase = true) -> StudentColors.MathBg to StudentColors.MathFg
    subject.contains("science", ignoreCase = true) -> StudentColors.ScienceBg to StudentColors.ScienceFg
    subject.contains("english", ignoreCase = true) -> StudentColors.EnglishBg to StudentColors.EnglishFg
    subject.contains("hindi", ignoreCase = true) -> StudentColors.HindiBg to StudentColors.HindiFg
    subject.contains("social", ignoreCase = true) -> StudentColors.SocialBg to StudentColors.SocialFg
    subject.contains("computer", ignoreCase = true) -> StudentColors.ComputerBg to StudentColors.ComputerFg
    subject.contains("sanskrit", ignoreCase = true) -> StudentColors.SanskritBg to StudentColors.SanskritFg
    else -> StudentColors.BlueLight to StudentColors.BlueAccent
}

@Composable
fun AssignmentScreen(
    viewModel: AssignmentViewModel,
    studentId: Int,
    studentName: String,
    classInfo: String,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAssignmentClick: () -> Unit
) {
    var currentTab by remember { mutableStateOf("assigned") }
    val assignments by viewModel.assignments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(currentTab, studentId) {
        viewModel.loadAssignments(studentId, status = currentTab)
    }

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
            onLogoutClick = onLogoutClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudentColors.BackgroundGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Assignment", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StudentColors.TextPrimary)
            Text(
                "View assignments given to you and track their status.",
                color = StudentColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                AssignmentTab("Active", currentTab == "assigned") { currentTab = "assigned" }
                AssignmentTab("Completed", currentTab == "completed", Modifier.padding(start = 20.dp)) { currentTab = "completed" }
            }
            HorizontalDivider(color = StudentColors.Divider, modifier = Modifier.padding(top = 8.dp))

            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = StudentColors.NavyPrimary) }

                assignments.isEmpty() -> Text(
                    "No assignments found.",
                    color = StudentColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 24.dp)
                )

                else -> Column(modifier = Modifier.padding(top = 12.dp)) {
                    assignments.forEach { item ->
                        AssignmentCard(item) {
                            viewModel.openAssignment(item, studentId)
                            onAssignmentClick()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.width(IntrinsicSize.Min).clickable { onClick() }) {
        Text(
            label,
            color = if (selected) StudentColors.NavyPrimary else StudentColors.TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .padding(top = 6.dp)
                .background(if (selected) StudentColors.NavyPrimary else Color.Transparent)
        )
    }
}

@Composable
internal fun AssignmentCard(item: AssignmentItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onClick() },
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (bg, fg) = subjectColors(item.subject)
                Text(
                    item.subject,
                    color = fg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(bg, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Text(
                    "Class ${item.gradeLevel}${item.section?.let { " – $it" } ?: ""}",
                    color = StudentColors.TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(StudentColors.BackgroundLight, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Box(modifier = Modifier.weight(1f))
                val isCompleted = item.status == "completed"
                Text(
                    if (isCompleted) "Completed" else "Published",
                    color = if (isCompleted) StudentColors.BlueAccent else StudentColors.GreenSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = StudentColors.TextPrimary,
                modifier = Modifier.padding(top = 10.dp)
            )
            item.description?.let {
                Text(it, color = StudentColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                item.dueDate?.let { due ->
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = StudentColors.RedDue, modifier = Modifier.height(14.dp))
                    Text(
                        "Due: $due",
                        color = StudentColors.RedDue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                item.maxMarks?.let { marks ->
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = StudentColors.StarGold,
                        modifier = Modifier.height(14.dp).padding(start = 12.dp)
                    )
                    Text("$marks marks", color = StudentColors.TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                }
                Box(modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "View", tint = StudentColors.TextTertiary)
            }
        }
    }
}
