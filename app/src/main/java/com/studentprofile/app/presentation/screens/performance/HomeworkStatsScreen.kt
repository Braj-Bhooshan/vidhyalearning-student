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
import com.studentprofile.app.presentation.viewmodel.HomeworkStatsViewModel
import com.studentprofile.app.presentation.viewmodel.HomeworkSubjectStat
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.ProgressRing
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun HomeworkStatsScreen(
    studentId: Int,
    onBack: () -> Unit,
    studentName: String,
    classInfo: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: HomeworkStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(studentId) { viewModel.load(studentId) }

    val total = (uiState.submitted + uiState.pending).coerceAtLeast(1)
    val completionRate = (uiState.submitted.toFloat() / total.toFloat()) * 100f

    Column(modifier = Modifier.fillMaxSize().background(StudentColors.NavyPrimary)) {
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
                .padding(20.dp)
        ) {
            Text("Homework Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StudentColors.TextPrimary)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StudentColors.NavyPrimary)
                }
            } else if (error != null) {
                Text(error ?: "", color = StudentColors.RedDue, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            } else {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        ProgressRing(progress = completionRate, progressColor = StudentColors.ProgressHomework, size = 130.dp)
                        Text("Homework Completion", color = StudentColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                            HeroStat("${uiState.submitted}", "Submitted", Color(0xFF2E7D32), Modifier.weight(1f))
                            Box(modifier = Modifier.width(1.dp).height(48.dp).background(Color(0xFFE5E7EB)))
                            HeroStat("${uiState.pending}", "Pending", Color(0xFFEF6C00), Modifier.weight(1f))
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Homework Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StudentColors.TextPrimary)
                        SummaryRow("Total Homework", "${uiState.submitted + uiState.pending}", StudentColors.TextPrimary)
                        SummaryRow("Completed", "${uiState.submitted}", Color(0xFF2E7D32))
                        SummaryRow("Pending", "${uiState.pending}", Color(0xFFEF6C00))
                        SummaryRow("Completion Rate", "%.1f%%".format(completionRate), StudentColors.ProgressHomework, showDivider = false)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Homework by Subject", color = StudentColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (uiState.subjects.isEmpty()) {
                            Text("No assignments yet.", color = StudentColors.TextSecondary, modifier = Modifier.padding(top = 12.dp))
                        } else {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                uiState.subjects.forEach { HomeworkSubjectRow(it) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = StudentColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color, showDivider: Boolean = true) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = StudentColors.TextSecondary, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        if (showDivider) HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = Color(0xFFECECEC))
    }
}

@Composable
private fun HomeworkSubjectRow(item: HomeworkSubjectStat) {
    val progress = if (item.total > 0) (item.submitted.toFloat() / item.total.toFloat()) else 0f
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.subject,
                modifier = Modifier
                    .size(44.dp)
                    .background(StudentColors.BlueLight, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            )
            Text(
                item.subject,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = StudentColors.TextPrimary,
                modifier = Modifier.padding(start = 14.dp).weight(1f)
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            color = StudentColors.ProgressHomework,
            trackColor = Color(0xFFECECEC)
        )
        Text(
            "${item.submitted} / ${item.total} Submitted",
            color = StudentColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
