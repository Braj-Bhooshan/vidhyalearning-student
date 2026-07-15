package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors the backend's StudentAttendanceReport / AttendanceRecordResponse, confirmed against
// the local backend's /openapi.json - see GET /api/v1/students/{student_id}/attendance.
@Serializable
data class AttendanceRecordDto(
    val date: String, // "yyyy-MM-dd"
    val status: String // present | absent | late | excused | half_day
)

@Serializable
data class AttendanceSummaryDto(
    @SerialName("total_days") val totalDays: Int = 0,
    @SerialName("present_count") val presentCount: Int = 0,
    @SerialName("absent_count") val absentCount: Int = 0,
    @SerialName("late_count") val lateCount: Int = 0,
    @SerialName("excused_count") val excusedCount: Int = 0,
    @SerialName("half_day_count") val halfDayCount: Int = 0,
    @SerialName("attendance_percentage") val attendancePercentage: String = "0"
)

@Serializable
data class StudentAttendanceReport(
    @SerialName("student_id") val studentId: Int = 0,
    @SerialName("student_name") val studentName: String = "",
    @SerialName("grade_level") val gradeLevel: String = "",
    val summary: AttendanceSummaryDto = AttendanceSummaryDto(),
    val records: List<AttendanceRecordDto> = emptyList()
)
