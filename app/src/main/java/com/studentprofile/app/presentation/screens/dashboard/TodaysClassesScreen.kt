package com.studentprofile.app.presentation.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentprofile.app.domain.models.ParentScheduleItemDto
import com.studentprofile.app.presentation.viewmodel.ClassesViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.StudentTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Parent-app palette (mirrors ParentDashboardScreen's Par* colors) ───────────

private val ParNavy2   = Color(0xFF22307A)
private val ParInk     = Color(0xFF1C2147)
private val ParMuted   = Color(0xFF727894)
private val ParMutedL  = Color(0xFF9A9FB5)
private val ParBorder  = Color(0xFFE8E9F3)
private val ParGreen   = Color(0xFF1EA34B)
private val ParGreenBg = Color(0xFFE2F8E9)
private val ParOrange  = Color(0xFFD98A1C)
private val ParRed     = Color(0xFFE2462F)
private val ParBlue    = Color(0xFF2F6FED)
private val ParBlueBg  = Color(0xFFDDE8FD)
private val ParPurple  = Color(0xFF7C5CF0)

private fun subjectAccentColor(subject: String): Color = when (subject.lowercase()) {
    "english"                      -> ParPurple
    "hindi"                        -> ParOrange
    "mathematics", "maths", "math" -> ParBlue
    "science"                      -> ParGreen
    "social science", "sst"        -> ParRed
    "computer", "computers"        -> Color(0xFF00ACC1)
    "sanskrit"                     -> Color(0xFFFF7043)
    else                           -> ParMuted
}

private fun subjectIconVector(subject: String): ImageVector = when (subject.lowercase()) {
    "english"                      -> Icons.AutoMirrored.Filled.MenuBook
    "hindi"                        -> Icons.Default.Translate
    "mathematics", "maths", "math" -> Icons.Default.Calculate
    "science"                      -> Icons.Default.Science
    "social science", "sst"        -> Icons.Default.Public
    "computer", "computers"        -> Icons.Default.Computer
    "sanskrit"                     -> Icons.Default.HistoryEdu
    else                           -> Icons.Default.Book
}

private fun todayDateString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private fun formatDisplayDate(isoDate: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val d = sdf.parse(isoDate) ?: return isoDate
        SimpleDateFormat("d MMM yyyy, EEEE", Locale.getDefault()).format(d)
    } catch (_: Exception) { isoDate }
}

private fun formatShortDate(isoDate: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val d = sdf.parse(isoDate) ?: return isoDate
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(d)
    } catch (_: Exception) { isoDate }
}

@Composable
fun TodaysClassesScreen(
    viewModel: ClassesViewModel,
    studentId: Int,
    studentName: String,
    classInfo: String,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showPast by remember { mutableStateOf(false) }
    val todaysClasses by viewModel.todaysClasses.collectAsState()
    val pastClasses by viewModel.pastClasses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(showPast, studentId) {
        viewModel.loadClasses(showPast, studentId)
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(StudentColors.BackgroundGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                    Text("Classes", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = ParInk)
                    Text(
                        "Today: ${formatDisplayDate(todayDateString())}",
                        color = ParMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ClassesSubTab(
                        label = "Today's Classes",
                        selected = !showPast,
                        onClick = { showPast = false },
                        modifier = Modifier.weight(1f)
                    )
                    ClassesSubTab(
                        label = "Past Classes",
                        selected = showPast,
                        onClick = { showPast = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when {
                isLoading -> item { ClassesLoading() }

                error != null -> item {
                    ClassesError(error ?: "Failed to load classes.") {
                        viewModel.loadClasses(showPast, studentId)
                    }
                }

                !showPast -> {
                    if (todaysClasses.isEmpty()) {
                        item {
                            ClassesEmptyState(
                                icon = Icons.Default.CalendarMonth,
                                title = "No classes today",
                                subtitle = "There are no classes scheduled for today."
                            )
                        }
                    } else {
                        itemsIndexed(todaysClasses) { index, item ->
                            TodayClassCard(
                                item = item,
                                isLast = index == todaysClasses.lastIndex,
                                onWatchVideo = { item.contentId?.let { viewModel.loadAndPlayVideo(it) } }
                            )
                        }
                    }
                }

                else -> {
                    if (pastClasses.isEmpty()) {
                        item {
                            ClassesEmptyState(
                                icon = Icons.Default.History,
                                title = "No past classes",
                                subtitle = "Classes marked taught by teachers will appear here."
                            )
                        }
                    } else {
                        itemsIndexed(pastClasses) { _, item ->
                            PastClassCard(
                                item = item,
                                onWatchVideo = { item.contentId?.let { viewModel.loadAndPlayVideo(it) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-tab ───────────────────────────────────────────────────────────────────

@Composable
private fun ClassesSubTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) ParNavy2 else Color.White)
            .border(1.dp, if (selected) ParNavy2 else ParBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else ParMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ── Today's Class Card (timeline style, same as the parent app) ────────────────

@Composable
private fun TodayClassCard(
    item: ParentScheduleItemDto,
    isLast: Boolean,
    onWatchVideo: () -> Unit
) {
    val subjectColor = subjectAccentColor(item.subject)
    val subjectIcon = subjectIconVector(item.subject)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Timeline column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Icon(
                imageVector = if (item.isTaught) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (item.isTaught) ParGreen else ParBlue
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (!item.chapterTopicTitle.isNullOrBlank() || !item.videoTitle.isNullOrBlank()) 148.dp else 120.dp)
                        .background(Color(0xFFE0E0E0))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Time label above card
            if (item.timeRange.isNotBlank()) {
                Text(
                    item.timeRange,
                    fontSize = 11.sp,
                    color = ParMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(13.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(
                    width = if (item.isTaught) 1.5.dp else 1.dp,
                    color = if (item.isTaught) ParGreen.copy(alpha = 0.35f) else ParBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    // Header row: icon + subject/class | status badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(subjectColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(subjectIcon, null, tint = subjectColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.subject,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ParInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                item.className,
                                fontSize = 11.sp,
                                color = ParMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (item.isTaught) ParGreenBg else ParBlueBg)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (item.isTaught) "Taught" else if (item.timeRange.isNotBlank()) item.startTime else "Scheduled",
                                fontSize = 10.sp,
                                color = if (item.isTaught) ParGreen else ParBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Chapter + Topic rows
                    if (!item.chapterTitle.isNullOrBlank() || !item.chapterTopicTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        if (!item.chapterTitle.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = ParMutedL, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    item.chapterTitle,
                                    fontSize = 12.sp,
                                    color = ParMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (!item.chapterTopicTitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Subject, null, tint = ParMutedL, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    item.chapterTopicTitle,
                                    fontSize = 12.sp,
                                    color = ParInk,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Video chip — shown when a video is assigned (whether taught or not)
                    if (!item.videoTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ParBlueBg.copy(alpha = 0.7f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayCircle, null, tint = ParBlue, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                item.videoTitle,
                                fontSize = 11.sp,
                                color = ParInk,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Watch Video button — only visible once teacher marks the class as taught
                        if (item.isTaught && item.contentId != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onWatchVideo,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ParBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Watch Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Teacher name (small, bottom)
                    if (!item.teacherName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = ParMutedL, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(item.teacherName, color = ParMutedL, fontSize = 10.5.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Past Class Card (flat list, always taught) ────────────────────────────────

@Composable
private fun PastClassCard(
    item: ParentScheduleItemDto,
    onWatchVideo: () -> Unit
) {
    val subjectColor = subjectAccentColor(item.subject)
    val subjectIcon = subjectIconVector(item.subject)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.5.dp, ParGreen.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(subjectColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(subjectIcon, null, tint = subjectColor, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.subject,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ParInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        item.className,
                        fontSize = 11.sp,
                        color = ParMuted
                    )
                }
                // Date + Completed chip stacked
                Column(horizontalAlignment = Alignment.End) {
                    if (item.date.isNotBlank()) {
                        Text(formatShortDate(item.date), color = ParMutedL, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ParGreenBg)
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text("Completed", color = ParGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Chapter + Topic rows
            if (!item.chapterTitle.isNullOrBlank() || !item.chapterTopicTitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                if (!item.chapterTitle.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = ParMutedL, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            item.chapterTitle,
                            fontSize = 12.sp,
                            color = ParMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (!item.chapterTopicTitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Subject, null, tint = ParMutedL, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            item.chapterTopicTitle,
                            fontSize = 12.sp,
                            color = ParInk,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Time range row
            if (item.timeRange.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = ParMutedL, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item.timeRange, color = ParMuted, fontSize = 11.sp)
                    if (!item.teacherName.isNullOrBlank()) {
                        Text("  ·  ", color = ParMutedL, fontSize = 11.sp)
                        Icon(Icons.Default.Person, null, tint = ParMutedL, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(item.teacherName, color = ParMutedL, fontSize = 11.sp)
                    }
                }
            }

            // Video title chip — shown when teacher recorded a video title on the schedule
            if (!item.videoTitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ParBlueBg.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayCircle, null, tint = ParBlue, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        item.videoTitle,
                        fontSize = 11.sp,
                        color = ParInk,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Watch Video button — shown whenever a content_id is linked, regardless of video title
            if (item.contentId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onWatchVideo,
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ParBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Watch Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun ClassesLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = ParNavy2)
    }
}

@Composable
private fun ClassesError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, null, tint = ParRed, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, color = ParMuted, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun ClassesEmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = ParMutedL, modifier = Modifier.size(44.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(title, color = ParInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, color = ParMuted, fontSize = 12.sp)
    }
}
