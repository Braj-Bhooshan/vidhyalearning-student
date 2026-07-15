package com.studentprofile.app.presentation.screens.performance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studentprofile.app.domain.models.SubjectPerformance
import com.studentprofile.app.presentation.viewmodel.AverageScoreViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.ProgressRing
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun AverageScoreScreen(
    studentId: Int,
    section: String?,
    onBack: () -> Unit,
    studentName: String,
    classInfo: String,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: AverageScoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(studentId) {
        if (studentId != 0) viewModel.load(studentId, section)
    }

    Column(modifier = Modifier.fillMaxSize().background(StudentColors.NavyPrimary)) {
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
            Text("Average Score Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StudentColors.TextPrimary)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StudentColors.NavyPrimary)
                }
            } else if (error != null) {
                Text(error ?: "", color = StudentColors.RedDue, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            } else {
                val rankLabel = if (uiState.classRank != null && uiState.classTotal != null) {
                    "#${uiState.classRank} / ${uiState.classTotal}"
                } else "-"

                Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProgressRing(progress = uiState.overallPercentage, progressColor = StudentColors.ProgressAvgScore, size = 130.dp)
                        Text(
                            if (uiState.overallPercentage >= 75f) "Great going! 📈" else "Keep improving",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                            HeroStat(uiState.highestScore?.let { "%.0f%%".format(it) } ?: "-", "Highest Score", Modifier.weight(1f))
                            Box(modifier = Modifier.width(1.dp).height(48.dp).background(Color(0xFFE5E7EB)))
                            HeroStat(rankLabel, "Class Rank", Modifier.weight(1f))
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Performance Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudentColors.TextPrimary)
                        SummaryRow("Overall Grade", uiState.overallGrade, StudentColors.NavyPrimary)
                        SummaryRow("Class Rank", rankLabel, StudentColors.NavyPrimary)
                        SummaryRow("Highest Score", uiState.highestScore?.let { "%.0f%%".format(it) } ?: "-", Color(0xFF2E7D32))
                        SummaryRow("Lowest Score", uiState.lowestScore?.let { "%.0f%%".format(it) } ?: "-", Color(0xFFEF6C00))
                        SummaryRow("Total Exams", "${uiState.totalExams}", StudentColors.TextPrimary, showDivider = false)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Subject-wise Performance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudentColors.TextPrimary)
                        if (uiState.subjects.isEmpty()) {
                            Text("No exam results published yet.", color = StudentColors.TextSecondary, modifier = Modifier.padding(top = 12.dp))
                        } else {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                uiState.subjects.forEach { SubjectScoreRow(it) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = StudentColors.NavyPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = StudentColors.TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color, showDivider: Boolean = true) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text(label, color = StudentColors.TextSecondary, modifier = Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.Bold, color = valueColor)
        }
        if (showDivider) HorizontalDivider(modifier = Modifier.padding(top = 10.dp), color = Color(0xFFECECEC))
    }
}

@Composable
private fun SubjectScoreRow(subject: SubjectPerformance) {
    Column(modifier = Modifier.padding(top = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = subject.iconRes),
                contentDescription = subject.name,
                modifier = Modifier
                    .size(34.dp)
                    .background(StudentColors.BlueLight, RoundedCornerShape(8.dp))
                    .padding(7.dp)
            )
            Text(
                subject.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = StudentColors.TextPrimary,
                modifier = Modifier.padding(start = 12.dp).weight(1f)
            )
            Text("%.0f%%".format(subject.percentage), fontWeight = FontWeight.Bold, color = StudentColors.NavyPrimary)
        }
        LinearProgressIndicator(
            progress = { (subject.percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            color = StudentColors.NavyPrimary,
            trackColor = StudentColors.ProgressTrack
        )
        Text(
            subject.grade,
            color = StudentColors.NavyPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 8.dp)
                .background(StudentColors.BlueLight, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(top = 14.dp), color = Color(0xFFECECEC))
    }
}
