package com.studentprofile.app.presentation.screens.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studentprofile.app.presentation.components.StudentSwitchSheet
import com.studentprofile.app.presentation.screens.assessments.AssessmentScreen
import com.studentprofile.app.presentation.screens.notifications.NotificationScreen
import com.studentprofile.app.presentation.screens.performance.AttendanceScreen
import com.studentprofile.app.presentation.screens.performance.AverageScoreScreen
import com.studentprofile.app.presentation.screens.performance.HomeworkStatsScreen
import com.studentprofile.app.presentation.screens.performance.PerformanceScreen
import com.studentprofile.app.presentation.screens.performance.SubjectPerformanceScreen
import com.studentprofile.app.presentation.screens.profile.ProfileScreen
import com.studentprofile.app.presentation.screens.video.VideoPlayerScreen
import com.studentprofile.app.presentation.viewmodel.AssignmentViewModel
import com.studentprofile.app.presentation.viewmodel.ClassVideoState
import com.studentprofile.app.presentation.viewmodel.AttendanceViewModel
import com.studentprofile.app.presentation.viewmodel.AuthViewModel
import com.studentprofile.app.presentation.viewmodel.ClassesViewModel
import com.studentprofile.app.presentation.viewmodel.ExaminationViewModel
import com.studentprofile.app.presentation.viewmodel.NotificationViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.SchoolNavigationDrawer

private val bottomTabs = listOf("Dashboard", "Classes", "Assignment", "Message")

@Composable
fun StudentMainScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    var screenStack by remember { mutableStateOf(listOf("Dashboard")) }
    val currentScreen = screenStack.last()
    var drawerOpen by remember { mutableStateOf(false) }
    var switchSheetOpen by remember { mutableStateOf(false) }
    val assignmentViewModel: AssignmentViewModel = hiltViewModel()
    val classesViewModel: ClassesViewModel = hiltViewModel()
    val attendanceViewModel: AttendanceViewModel = hiltViewModel()
    val examinationViewModel: ExaminationViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    val details by authViewModel.selectedStudentDetails.collectAsState()
    val currentAcademicSession by authViewModel.currentAcademicSession.collectAsState()
    val student = details?.student
    val studentName = student?.displayName ?: ""
    val classAndSection = if (student != null) {
        val baseClass = student.classInfo.removePrefix("Class ").substringBefore(" -").trim()
        val sectionPart = if (!student.section.isNullOrBlank()) " - ${student.section}" else ""
        "Class $baseClass$sectionPart"
    } else ""
    val gradeLevel = student?.classInfo?.filter { it.isDigit() }?.toIntOrNull() ?: 0

    val academicSession = when {
        !currentAcademicSession.isNullOrBlank() -> "Academic Session $currentAcademicSession"
        !student?.session.isNullOrBlank() -> "Academic Session ${student.session}"
        else -> "Academic Session 2025 - 26"
    }

    fun navigate(screen: String) {
        screenStack = screenStack + screen
    }

    fun back() {
        if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
    }

    fun switchTab(tab: String) {
        screenStack = listOf(tab)
    }

    val commonOnNotificationClick = { navigate("Notifications") }
    val commonOnProfileClick = { navigate("Profile") }
    val commonOnLogoutClick = { authViewModel.logout(); onLogout() }

    BackHandler(enabled = screenStack.size > 1) { back() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    "Dashboard" -> DashboardScreen(
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        authViewModel = authViewModel,
                        notificationViewModel = notificationViewModel,
                        onSwitchStudent = { switchSheetOpen = true },
                        onMenuClick = { drawerOpen = true },
                        onProfileClick = commonOnProfileClick,
                        onNotificationClick = commonOnNotificationClick,
                        onLogoutClick = commonOnLogoutClick,
                        onAttendanceClick = { navigate("Attendance") },
                        onAverageScoreClick = { navigate("AverageScore") },
                        onHomeworkStatsClick = { navigate("HomeworkStats") },
                        onSubjectPerformanceClick = { navigate("SubjectPerformance") },
                        onViewAllAssessments = { navigate("Assessment") }
                    )
                    "Classes" -> TodaysClassesScreen(
                        viewModel = classesViewModel,
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        studentName = studentName,
                        classInfo = classAndSection,
                        onMenuClick = { drawerOpen = true },
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "Assignment" -> AssignmentScreen(
                        viewModel = assignmentViewModel,
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        studentName = studentName,
                        classInfo = classAndSection,
                        onMenuClick = { drawerOpen = true },
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick,
                        onAssignmentClick = { navigate("AssignmentDetail") }
                    )
                    "AssignmentDetail" -> AssignmentDetailScreen(
                        viewModel = assignmentViewModel,
                        gradeLevel = gradeLevel,
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        studentName = studentName,
                        classInfo = classAndSection,
                        onBack = { back() },
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "Message" -> MessageScreen(
                        studentName = studentName,
                        classInfo = classAndSection,
                        onMenuClick = { drawerOpen = true },
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "Profile" -> ProfileScreen(
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        studentName = studentName,
                        classInfo = classAndSection,
                        fatherName = details?.fatherName,
                        motherName = details?.motherName,
                        onBack = { back() },
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "Notifications" -> NotificationScreen(
                        viewModel = notificationViewModel,
                        onBack = { back() },
                        studentName = studentName,
                        classInfo = classAndSection,
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "Attendance" -> AttendanceScreen(
                        viewModel = attendanceViewModel,
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        onBack = { back() },
                        studentName = studentName,
                        classInfo = classAndSection,
                        onMenuClick = { drawerOpen = true },
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "AverageScore" -> AverageScoreScreen(
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        section = student?.section,
                        onBack = { back() },
                        studentName = studentName,
                        classInfo = classAndSection,
                        onMenuClick = { drawerOpen = true },
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "HomeworkStats" -> HomeworkStatsScreen(
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        onBack = { back() },
                        studentName = studentName,
                        classInfo = classAndSection,
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "SubjectPerformance" -> SubjectPerformanceScreen(
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        section = student?.section,
                        onBack = { back() },
                        studentName = studentName,
                        classInfo = classAndSection,
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "Assessment" -> AssessmentScreen(
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        onBack = { back() },
                        studentName = studentName,
                        classInfo = classAndSection,
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                    "Performance" -> PerformanceScreen(
                        examinationViewModel = examinationViewModel,
                        assignmentViewModel = assignmentViewModel,
                        studentId = student?.studentId?.toIntOrNull() ?: 0,
                        studentName = studentName,
                        classInfo = classAndSection,
                        onBack = { back() },
                        onNotificationClick = commonOnNotificationClick,
                        onProfileClick = commonOnProfileClick,
                        onLogoutClick = commonOnLogoutClick
                    )
                }
            }

            if (currentScreen in bottomTabs) {
                StudentBottomNav(selectedTab = currentScreen, onTabSelected = ::switchTab)
            }
        }

        if (drawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                SchoolNavigationDrawer(
                    studentName = studentName,
                    classInfo = classAndSection,
                    academicSession = academicSession,
                    onMenuClick = { item ->
                        drawerOpen = false
                        when (item) {
                            "Message" -> switchTab("Message")
                            "Attendance" -> navigate("Attendance")
                            "Performance" -> navigate("Performance")
                            "Assignment" -> switchTab("Assignment")
                        }
                    }
                )
            }
        }

        if (switchSheetOpen) {
            StudentSwitchSheet(
                students = authViewModel.getLinkedStudentsForCurrentSession(),
                onSelect = {
                    authViewModel.selectStudent(it.studentId)
                    switchSheetOpen = false
                },
                onDismiss = { switchSheetOpen = false }
            )
        }

        // Class-video playback overlays - hosted at the root so the player covers the
        // whole screen (including the bottom nav), same as the parent app's dashboard.
        val videoState by classesViewModel.videoState.collectAsState()

        if (videoState is ClassVideoState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        if (videoState is ClassVideoState.Ready) {
            val video = (videoState as ClassVideoState.Ready).video
            VideoPlayerScreen(
                videoId = video.id,
                videoUrl = video.url,
                videoTitle = video.title,
                onBack = { classesViewModel.clearVideoState() }
            )
        }

        if (videoState is ClassVideoState.Error) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudentColors.RedDue)
                    .clickable { classesViewModel.clearVideoState() }
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    (videoState as ClassVideoState.Error).message,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StudentBottomNav(selectedTab: String, onTabSelected: (String) -> Unit) {
    val items = listOf(
        "Dashboard" to Icons.Default.Home,
        "Classes" to Icons.Default.CalendarMonth,
        "Assignment" to Icons.AutoMirrored.Filled.Assignment,
        "Message" to Icons.AutoMirrored.Filled.Message
    )
    NavigationBar(containerColor = Color.White) {
        items.forEach { (label, icon) ->
            NavigationBarItem(
                selected = selectedTab == label,
                onClick = { onTabSelected(label) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StudentColors.BottomNavActive,
                    selectedTextColor = StudentColors.BottomNavActive,
                    unselectedIconColor = StudentColors.BottomNavInactive,
                    unselectedTextColor = StudentColors.BottomNavInactive
                )
            )
        }
    }
}
