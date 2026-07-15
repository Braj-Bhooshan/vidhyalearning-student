package com.studentprofile.app.presentation.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.studentprofile.app.domain.models.DashboardSummary
import com.studentprofile.app.domain.models.RecentAssessment
import com.studentprofile.app.domain.models.SubjectPerformance
import com.studentprofile.app.presentation.viewmodel.AuthViewModel
import com.studentprofile.app.presentation.viewmodel.DashboardViewModel
import com.studentprofile.app.presentation.viewmodel.NotificationViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.subjectIconBackgroundColor
import com.studentprofile.app.ui.components.ProgressRing
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun DashboardScreen(
    studentId: Int,
    authViewModel: AuthViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    onSwitchStudent: () -> Unit,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onAverageScoreClick: () -> Unit,
    onHomeworkStatsClick: () -> Unit,
    onSubjectPerformanceClick: () -> Unit,
    onViewAllAssessments: () -> Unit
) {
    val details by authViewModel.selectedStudentDetails.collectAsState()
    val student = details?.student
    val classAndSection = if (student != null) {
        val baseClass = student.classInfo.removePrefix("Class ").substringBefore(" -").trim()
        val sectionPart = if (!student.section.isNullOrBlank()) " - ${student.section}" else ""
        "Class $baseClass$sectionPart"
    } else ""

    val summary by dashboardViewModel.summary.collectAsState()
    val isLoading by dashboardViewModel.isLoading.collectAsState()
    val error by dashboardViewModel.error.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    LaunchedEffect(studentId) {
        if (studentId != 0) {
            dashboardViewModel.loadDashboard(studentId)
            notificationViewModel.refreshUnreadCount()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(StudentColors.NavyPrimary)) {
        StudentTopBar(
            studentName = student?.displayName ?: "",
            classInfo = classAndSection,
            photoUrl = student?.photoUrl,
            onMenuClick = onMenuClick,
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
            onLogoutClick = onLogoutClick,
            unreadCount = unreadCount
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudentColors.BackgroundGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val currentDetails = details
            if (currentDetails != null) {
                ProfileSummaryCard(
                    fatherName = currentDetails.fatherName,
                    motherName = currentDetails.motherName,
                    studentName = student?.displayName ?: "",
                    photoUrl = student?.photoUrl
                )
                PerformanceMetricsRow(
                    summary = summary,
                    onAttendanceClick = onAttendanceClick,
                    onAverageScoreClick = onAverageScoreClick,
                    onHomeworkStatsClick = onHomeworkStatsClick
                )
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StudentColors.NavyPrimary)
                    }
                } else if (error != null && summary.subjectPerformances.isEmpty()) {
                    Text(
                        error ?: "",
                        color = StudentColors.RedDue,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    SubjectPerformanceCard(summary.subjectPerformances, onSubjectPerformanceClick)
                    RecentAssessmentsCard(summary.recentAssessments, onViewAllAssessments)
                }
            }
        }
    }
}

@Composable
private fun ProfileSummaryCard(fatherName: String?, motherName: String?, studentName: String, photoUrl: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(StudentColors.BackgroundGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(photoUrl)
                                .build(),
                            contentDescription = "Student Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Student Photo",
                            tint = StudentColors.NavyPrimary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                    Text(
                        studentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = StudentColors.TextPrimary,
                        maxLines = 1
                    )
                    Row(modifier = Modifier.padding(top = 10.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Father's Name - ", color = StudentColors.TextSecondary, fontSize = 11.sp)
                            Text("Mother's Name - ", color = StudentColors.TextSecondary, fontSize = 11.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                fatherName ?: "-",
                                color = StudentColors.TextPrimary,
                                fontSize = 11.sp
                            )

                            Text(
                                motherName ?: "-",
                                color = StudentColors.TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.MetricColumn(
    label: String,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 10.sp, color = StudentColors.TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
private fun PerformanceMetricsRow(
    summary: DashboardSummary,
    onAttendanceClick: () -> Unit,
    onAverageScoreClick: () -> Unit,
    onHomeworkStatsClick: () -> Unit
) {
    val homeworkTotal = (summary.homeworkSubmitted + summary.homeworkPending).coerceAtLeast(1)
    val homeworkPercent = (summary.homeworkSubmitted.toFloat() / homeworkTotal.toFloat()) * 100f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        MetricColumn("Attendance", onAttendanceClick) {
            ProgressRing(progress = summary.attendancePercent, progressColor = StudentColors.ProgressAttendance, size = 65.dp, strokeWidth = 5.dp)
            Text("Present :  ${summary.presentCount}", fontSize = 9.sp, color = StudentColors.TextSecondary, modifier = Modifier.padding(top = 6.dp))
            Text("Absent :  ${summary.absentCount}", fontSize = 9.sp, color = StudentColors.TextSecondary)
        }
        MetricColumn("Average Score", onAverageScoreClick) {
            ProgressRing(progress = summary.avgScore, progressColor = StudentColors.ProgressAvgScore, size = 65.dp, strokeWidth = 5.dp)
            Text("Grade:   ${summary.grade}", fontSize = 9.sp, color = StudentColors.TextSecondary, modifier = Modifier.padding(top = 6.dp))
        }
        MetricColumn("Assignment", onHomeworkStatsClick) {
            ProgressRing(progress = homeworkPercent, progressColor = StudentColors.ProgressHomework, size = 65.dp, strokeWidth = 5.dp)
            Text("Submitted:  ${summary.homeworkSubmitted}", fontSize = 9.sp, color = StudentColors.TextSecondary, modifier = Modifier.padding(top = 6.dp))
            Text("Pending :   ${summary.homeworkPending}", fontSize = 9.sp, color = StudentColors.TextSecondary)
        }
    }
}

@Composable
private fun SubjectPerformanceCard(subjects: List<SubjectPerformance>, onViewAllClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Subject-wise Performance (This Term)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = StudentColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "View All >",
                    color = StudentColors.BlueAccent,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
            Column(modifier = Modifier.padding(top = 12.dp)) {
                subjects.forEachIndexed { index, subject ->
                    SubjectPerformanceRow(subject)
                    if (index != subjects.lastIndex) HorizontalDivider(color = StudentColors.Divider)
                }
            }
        }
    }
}

@Composable
fun SubjectPerformanceRow(subject: SubjectPerformance) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubjectIcon(iconRes = subject.iconRes, iconBgRes = subject.iconBgRes)
        Text(
            subject.name,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = StudentColors.TextPrimary,
            modifier = Modifier.padding(start = 10.dp).weight(1f)
        )
        LinearProgressIndicator(
            progress = { (subject.percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.width(80.dp).height(8.dp).padding(start = 8.dp),
            color = Color(subject.progressColor),
            trackColor = StudentColors.ProgressTrack
        )
        Text(
            String.format("%.1f%%", subject.percentage),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = StudentColors.TextPrimary,
            modifier = Modifier.padding(start = 8.dp)
        )
        GradeCapsule(subject.grade)
    }
}

@Composable
private fun RecentAssessmentsCard(assessments: List<RecentAssessment>, onViewAllClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Recent Assessments",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = StudentColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "View All >",
                    color = StudentColors.BlueAccent,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
            Column(modifier = Modifier.padding(top = 12.dp)) {
                assessments.forEachIndexed { index, assessment ->
                    RecentAssessmentRow(assessment)
                    if (index != assessments.lastIndex) HorizontalDivider(color = StudentColors.Divider)
                }
            }
        }
    }
}

@Composable
fun RecentAssessmentRow(assessment: RecentAssessment) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubjectIcon(iconRes = assessment.iconRes, iconBgRes = assessment.iconBgRes)
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Text(assessment.title, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = StudentColors.TextPrimary)
            Text(assessment.date, fontSize = 11.sp, color = StudentColors.TextTertiary, modifier = Modifier.padding(top = 2.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(assessment.marks, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StudentColors.TextPrimary)
            Text("Marks Obtained", fontSize = 9.sp, color = StudentColors.TextTertiary)
        }
        GradeCapsule(assessment.grade, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun SubjectIcon(iconRes: Int, iconBgRes: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(subjectIconBackgroundColor(iconBgRes), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun GradeCapsule(grade: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 32.dp, height = 28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StudentColors.BlueLight),
        contentAlignment = Alignment.Center
    ) {
        Text(grade, color = StudentColors.NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}
