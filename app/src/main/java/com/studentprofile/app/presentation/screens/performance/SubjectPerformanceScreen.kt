package com.studentprofile.app.presentation.screens.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.studentprofile.app.domain.models.StudentTrendPoint
import com.studentprofile.app.domain.models.SubjectPerformance
import com.studentprofile.app.presentation.viewmodel.SubjectPerformanceViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun SubjectPerformanceScreen(
    studentId: Int,
    section: String?,
    onBack: () -> Unit,
    studentName: String,
    classInfo: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: SubjectPerformanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(studentId) {
        if (studentId != 0) viewModel.load(studentId, section)
    }

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
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .background(StudentColors.BackgroundGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Subject Performance Breakdown", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StudentColors.TextPrimary)
            Text("Current Academic Term", color = StudentColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StudentColors.NavyPrimary)
                }
            } else if (error != null) {
                Text(error ?: "", color = StudentColors.RedDue, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            } else {
                val rankLabel = if (uiState.classRank != null && uiState.classTotal != null) {
                    "${uiState.classRank}/${uiState.classTotal}"
                } else "-"

                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Overall Performance", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                            OverallStat("%.0f%%".format(uiState.overallPercentage), "Score", Modifier.weight(1f))
                            OverallStat(uiState.overallGrade, "Grade", Modifier.weight(1f))
                            OverallStat(rankLabel, "Rank", Modifier.weight(1f))
                        }
                        uiState.attendancePercent?.let { attendance ->
                            Text(
                                "Attendance : %.0f%%".format(attendance),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .background(StudentColors.GreenLightBg, RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (uiState.subjects.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp)) {
                        Text(
                            "No exam results published yet.",
                            color = StudentColors.TextSecondary,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Subject Breakdown", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            uiState.subjects.forEach { subject ->
                                Text(subject.name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
                                LinearProgressIndicator(
                                    progress = { (subject.percentage / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    color = Color(subject.progressColor),
                                    trackColor = StudentColors.ProgressTrack
                                )
                                Text("%.0f%% • Grade ${subject.grade}".format(subject.percentage), modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    val topSubject = uiState.subjects.maxByOrNull { it.percentage }
                    val weakSubject = uiState.subjects.minByOrNull { it.percentage }

                    topSubject?.let {
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Top Performing Subject", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(it.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                                Text("%.0f%% • Grade ${it.grade}".format(it.percentage))
                            }
                        }
                    }

                    weakSubject?.let {
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Needs Improvement", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(it.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                                Text("%.0f%% • Grade ${it.grade}".format(it.percentage))
                                Text("Additional practice recommended for this subject.", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }

                if (uiState.trend.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Performance Trend", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            uiState.trend.forEach { TrendRow(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label)
    }
}

@Composable
private fun TrendRow(point: StudentTrendPoint) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("${point.label} - %.0f%%".format(point.value))
        LinearProgressIndicator(
            progress = { (point.value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            color = StudentColors.NavyPrimary,
            trackColor = StudentColors.ProgressTrack
        )
    }
}
