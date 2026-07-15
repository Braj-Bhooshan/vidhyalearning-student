package com.studentprofile.app.presentation.screens.assessments

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.studentprofile.app.presentation.screens.dashboard.RecentAssessmentRow
import com.studentprofile.app.presentation.viewmodel.AssessmentViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun AssessmentScreen(
    studentId: Int,
    onBack: () -> Unit,
    studentName: String,
    classInfo: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: AssessmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(studentId) {
        if (studentId != 0) viewModel.load(studentId)
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
            Text("Assessment Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StudentColors.TextPrimary)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StudentColors.NavyPrimary)
                }
            } else if (error != null) {
                Text(error ?: "", color = StudentColors.RedDue, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        AssessmentStat("${uiState.totalTests}", "Total Tests", Modifier.weight(1f))
                        AssessmentStat("%.0f%%".format(uiState.averageScore), "Average Score", Modifier.weight(1f))
                        AssessmentStat(uiState.bestGrade, "Best Grade", Modifier.weight(1f))
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Assessment History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudentColors.TextPrimary)
                        if (uiState.assessments.isEmpty()) {
                            Text(
                                "No exam results published yet.",
                                color = StudentColors.TextSecondary,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        } else {
                            uiState.assessments.forEachIndexed { index, assessment ->
                                RecentAssessmentRow(assessment)
                                if (index != uiState.assessments.lastIndex) HorizontalDivider(color = StudentColors.Divider)
                            }
                        }
                    }
                }

                if (uiState.assessments.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = StudentColors.GreenLightBg)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (uiState.averageScore >= 75f) "Great Job! You're performing above average." else "Keep going - steady progress adds up.",
                                color = Color(0xFF2E7D32),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessmentStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StudentColors.TextPrimary)
        Text(label, color = StudentColors.TextSecondary, fontSize = 12.sp)
    }
}
