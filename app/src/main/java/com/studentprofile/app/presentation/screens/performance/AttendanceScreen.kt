package com.studentprofile.app.presentation.screens.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentprofile.app.presentation.viewmodel.AttendanceDay
import com.studentprofile.app.presentation.viewmodel.AttendanceDayStatus
import com.studentprofile.app.presentation.viewmodel.AttendanceMonth
import com.studentprofile.app.presentation.viewmodel.AttendanceViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.ProgressRing
import com.studentprofile.app.ui.components.StudentTopBar

private val weekDayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

private fun statusColor(status: AttendanceDayStatus): Color = when (status) {
    AttendanceDayStatus.PRESENT -> StudentColors.GreenSuccess
    AttendanceDayStatus.ABSENT -> Color(0xFFD32F2F)
    AttendanceDayStatus.LATE -> Color(0xFFF57C00)
    AttendanceDayStatus.NONE -> Color.Transparent
}

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    studentId: Int,
    onBack: () -> Unit,
    studentName: String,
    classInfo: String,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    LaunchedEffect(studentId) { viewModel.loadAttendance(studentId) }

    val months by viewModel.months.collectAsState()
    val selectedMonthKey by viewModel.selectedMonthKey.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedMonth = months.firstOrNull { it.key == selectedMonthKey } ?: months.lastOrNull()

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
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .background(StudentColors.BackgroundGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Attendance", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            when {
                isLoading && months.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StudentColors.NavyPrimary)
                    }
                }
                error != null && months.isEmpty() -> {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), shape = RoundedCornerShape(20.dp)) {
                        Text(
                            error ?: "Failed to load attendance.",
                            modifier = Modifier.padding(20.dp),
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
                else -> {
                    MonthSelectorRow(
                        months = months,
                        selectedKey = selectedMonth?.key,
                        onSelect = { viewModel.selectMonth(it) }
                    )

                    selectedMonth?.let { month ->
                        AttendanceSummaryCard(month)
                        AttendanceCalendarCard(month)
                        AttendanceLegend()
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelectorRow(
    months: List<AttendanceMonth>,
    selectedKey: String?,
    onSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(months) { month ->
            val selected = month.key == selectedKey
            val bg = if (selected) StudentColors.NavyPrimary else Color.White
            val fg = if (selected) Color.White else StudentColors.TextPrimary
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .selectable(selected = selected, onClick = { onSelect(month.key) })
                    .background(bg, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(month.shortLabel, color = fg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${month.percentage}%", color = fg, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AttendanceSummaryCard(month: AttendanceMonth) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressRing(
                progress = month.percentage.toFloat(),
                progressColor = StudentColors.ProgressAttendance,
                size = 120.dp
            )
            Text(month.label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            Text(
                "${month.presentDays} of ${month.totalDays} days present",
                color = StudentColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AttendanceCalendarCard(month: AttendanceMonth) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDayLabels.forEach { label ->
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = StudentColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // firstDayOfWeek is 1=Sunday..7=Saturday; convert to 0-based leading blank cells.
            val leadingBlanks = month.firstDayOfWeek - 1
            val cells: List<AttendanceDay?> = List(leadingBlanks) { null } + month.days
            val rows = cells.chunked(7)

            rows.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    week.forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(statusColor(day.status).copy(alpha = if (day.status == AttendanceDayStatus.NONE) 0f else 0.18f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${day.dayOfMonth}",
                                        fontSize = 12.sp,
                                        fontWeight = if (day.status == AttendanceDayStatus.NONE) FontWeight.Normal else FontWeight.Bold,
                                        color = if (day.status == AttendanceDayStatus.NONE) StudentColors.TextSecondary else statusColor(day.status)
                                    )
                                }
                            }
                        }
                    }
                    // Pad the last row out to 7 columns so weekday alignment holds.
                    repeat(7 - week.size) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttendanceLegend() {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LegendDot("Present", StudentColors.GreenSuccess)
            LegendDot("Absent", Color(0xFFD32F2F))
            LegendDot("Late", Color(0xFFF57C00))
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(label, fontSize = 11.sp, color = StudentColors.TextSecondary, modifier = Modifier.padding(start = 4.dp))
    }
}
