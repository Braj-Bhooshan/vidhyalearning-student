package com.studentprofile.app.domain.models

data class DashboardSummary(
    val attendancePercent: Float = 0f,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val avgScore: Float = 0f,
    val grade: String = "-",
    val homeworkSubmitted: Int = 0,
    val homeworkPending: Int = 0,
    val subjectPerformances: List<SubjectPerformance> = emptyList(),
    val recentAssessments: List<RecentAssessment> = emptyList()
)
