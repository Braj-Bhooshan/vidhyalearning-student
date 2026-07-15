package com.studentprofile.app.domain.models

import kotlinx.serialization.Serializable

// Mirrors GET /api/v1/academic/student-dashboard/{student_id}/class-rank and .../performance-trend,
// confirmed against the local backend's /openapi.json. Unlike every other endpoint this app calls,
// these live under the "academic" tag rather than "students"/"mobile/*" - auth scope for a
// parent-role token is unconfirmed and they may be staff/admin-only. Callers must treat failures
// (403 or otherwise) as "field unavailable", not a hard error - see AverageScoreViewModel/
// SubjectPerformanceViewModel.
@Serializable
data class StudentClassRankResponse(
    val rank: Int? = null,
    val total: Int? = null
)

@Serializable
data class StudentTrendPoint(
    val label: String = "",
    val value: Float = 0f
)

@Serializable
data class StudentPerformanceTrendResponse(
    val points: List<StudentTrendPoint> = emptyList()
)
