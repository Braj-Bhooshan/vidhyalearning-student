package com.studentprofile.app.presentation.screens.performance

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.studentprofile.app.domain.models.GradedAssignment
import com.studentprofile.app.domain.models.StudentExaminationMarkItem
import com.studentprofile.app.domain.models.StudentExaminationPerformanceResponse
import com.studentprofile.app.domain.models.SubjectScoreTotal
import com.studentprofile.app.presentation.viewmodel.AssignmentViewModel
import com.studentprofile.app.presentation.viewmodel.ExaminationViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.ProgressRing
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun PerformanceScreen(
    examinationViewModel: ExaminationViewModel,
    assignmentViewModel: AssignmentViewModel,
    studentId: Int,
    studentName: String,
    classInfo: String,
    onBack: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var currentTab by remember { mutableStateOf("examination") }

    LaunchedEffect(studentId) { examinationViewModel.loadGrades(studentId) }

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
                .padding(20.dp)
        ) {
            Text("Performance", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StudentColors.TextPrimary)

            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                PerformanceTab("Examination", currentTab == "examination") { currentTab = "examination" }
                PerformanceTab("Assignment", currentTab == "assignment", Modifier.padding(start = 20.dp)) { currentTab = "assignment" }
            }
            HorizontalDivider(color = StudentColors.Divider, modifier = Modifier.padding(top = 8.dp))

            when (currentTab) {
                "examination" -> ExaminationTab(examinationViewModel)
                "assignment" -> AssignmentTabContent(
                    viewModel = assignmentViewModel,
                    studentId = studentId
                )
            }
        }
    }
}

@Composable
private fun PerformanceTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.width(IntrinsicSize.Max).clickable { onClick() }) {
        Text(
            label,
            color = if (selected) StudentColors.TabActive else StudentColors.TabInactive,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(3.dp)
                .background(if (selected) StudentColors.TabActive else Color.Transparent)
        )
    }
}

@Composable
private fun ExaminationTab(viewModel: ExaminationViewModel) {
    val report by viewModel.report.collectAsState()
    val terms by viewModel.terms.collectAsState()
    val selectedTerm by viewModel.selectedTerm.collectAsState()
    val grades by viewModel.filteredGrades.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        when {
            isLoading && report == null -> {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StudentColors.NavyPrimary)
                }
            }
            error != null && report == null -> {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), shape = RoundedCornerShape(20.dp)) {
                    Text(
                        error ?: "Failed to load examination results.",
                        modifier = Modifier.padding(20.dp),
                        color = Color(0xFFD32F2F)
                    )
                }
            }
            else -> {
                TermDropdown(
                    terms = terms,
                    selectedTerm = selectedTerm,
                    onSelect = { viewModel.selectTerm(it) }
                )

                report?.let { OverallScoreCard(it) }

                if (grades.isEmpty()) {
                    Text(
                        "No examination results found.",
                        color = StudentColors.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                } else {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        grades.forEach { grade -> GradeCard(grade) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TermDropdown(terms: List<String>, selectedTerm: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(top = 20.dp)) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selectedTerm, color = StudentColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select term", tint = StudentColors.TextSecondary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            terms.forEach { term ->
                DropdownMenuItem(
                    text = { Text(term) },
                    onClick = { expanded = false; onSelect(term) }
                )
            }
        }
    }
}

@Composable
private fun OverallScoreCard(report: StudentExaminationPerformanceResponse) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressRing(
                progress = report.overallPercentage,
                progressColor = StudentColors.ProgressAvgScore,
                size = 120.dp
            )
            Text("Overall Grade: ${report.overallGrade}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            Text(
                "${report.academicYear} • Class ${report.gradeLevel}",
                color = StudentColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun GradeCard(grade: StudentExaminationMarkItem) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(grade.subject, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StudentColors.TextPrimary)
                Box(modifier = Modifier.weight(1f))
                grade.gradeLetter?.let {
                    Text(it, color = StudentColors.ProgressAvgScore, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
            grade.examinationName?.let {
                Text(it, color = StudentColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text(
                    "%.0f / %.0f".format(grade.marksObtained, grade.totalMarks),
                    color = StudentColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                grade.percentage?.let {
                    Text(
                        "%.0f%%".format(it),
                        color = StudentColors.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
            grade.remarks?.let {
                Text(it, color = StudentColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun AssignmentTabContent(
    viewModel: AssignmentViewModel,
    studentId: Int
) {
    var gradeTab by remember { mutableStateOf("grades") }

    LaunchedEffect(studentId) { viewModel.loadGradedAssignments(studentId) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
            PerformanceTab("Assignment Grades", gradeTab == "grades") { gradeTab = "grades" }
            PerformanceTab("Total Score by Subject", gradeTab == "totals", Modifier.padding(start = 20.dp)) { gradeTab = "totals" }
        }
        HorizontalDivider(color = StudentColors.Divider, modifier = Modifier.padding(top = 8.dp))

        when (gradeTab) {
            "grades" -> AssignmentGradesBySubjectTab(viewModel)
            "totals" -> AssignmentTotalScoreBySubjectTab(viewModel)
        }
    }
}

@Composable
private fun AssignmentGradesBySubjectTab(viewModel: AssignmentViewModel) {
    val subjects by viewModel.gradeSubjects.collectAsState()
    val selectedSubject by viewModel.selectedGradeSubject.collectAsState()
    val assignments by viewModel.filteredGradedAssignments.collectAsState()
    val isLoading by viewModel.isLoadingGrades.collectAsState()
    val error by viewModel.gradesError.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SubjectDropdown(
            subjects = subjects,
            selectedSubject = selectedSubject,
            onSelect = { viewModel.selectGradeSubject(it) }
        )

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = StudentColors.NavyPrimary) }

            error != null -> Text(
                error ?: "Failed to load assignment grades.",
                color = Color(0xFFD32F2F),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 24.dp)
            )

            assignments.isEmpty() -> Text(
                "No graded assignments found.",
                color = StudentColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 24.dp)
            )

            else -> Column(modifier = Modifier.padding(top = 12.dp)) {
                assignments.forEachIndexed { index, assignment ->
                    GradedAssignmentCard(number = index + 1, assignment = assignment)
                }
            }
        }
    }
}

@Composable
private fun AssignmentTotalScoreBySubjectTab(viewModel: AssignmentViewModel) {
    val totals by viewModel.subjectScoreTotals.collectAsState()
    val isLoading by viewModel.isLoadingGrades.collectAsState()
    val error by viewModel.gradesError.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = StudentColors.NavyPrimary) }

            error != null -> Text(
                error ?: "Failed to load assignment grades.",
                color = Color(0xFFD32F2F),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 24.dp)
            )

            totals.isEmpty() -> Text(
                "No subjects assigned yet.",
                color = StudentColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 24.dp)
            )

            else -> Column(modifier = Modifier.padding(top = 20.dp)) {
                SubjectScoreTableHeader()
                totals.forEach { total -> SubjectScoreTotalRow(total) }
            }
        }
    }
}

@Composable
private fun SubjectDropdown(subjects: List<String>, selectedSubject: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(top = 20.dp)) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selectedSubject, color = StudentColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select subject", tint = StudentColors.TextSecondary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            subjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject) },
                    onClick = { expanded = false; onSelect(subject) }
                )
            }
        }
    }
}

@Composable
private fun GradedAssignmentCard(number: Int, assignment: GradedAssignment) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "#$number  ${assignment.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = StudentColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    assignment.gradeLetter ?: (assignment.percentage?.let { "%.0f%%".format(it) } ?: "N/A"),
                    color = StudentColors.ProgressAvgScore,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Text(assignment.subject, color = StudentColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            Text(
                "%.0f / %.0f".format(assignment.obtainedMarks, assignment.maxMarks) +
                    (assignment.percentage?.let { " (%.0f%%)".format(it) } ?: ""),
                color = StudentColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun SubjectScoreTableHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Text(
            "Subject",
            color = StudentColors.TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.weight(1.6f)
        )
        Text(
            "Score",
            color = StudentColors.TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            "Grade",
            color = StudentColors.TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun SubjectScoreTotalRow(total: SubjectScoreTotal) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.6f)) {
                Text(total.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StudentColors.TextPrimary)
                Text(
                    if (total.assignmentCount > 0) {
                        "%.0f/%.0f • ${total.assignmentCount} assignment${if (total.assignmentCount == 1) "" else "s"}"
                            .format(total.totalObtainedMarks, total.totalMaxMarks)
                    } else {
                        "No graded assignments yet"
                    },
                    color = StudentColors.TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                total.percentage?.let { "%.0f%%".format(it) } ?: "-",
                color = StudentColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                total.gradeLetter ?: "N/A",
                color = StudentColors.ProgressAvgScore,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}
