package com.studentprofile.app.presentation.viewmodel

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * "Last 5 months through today" window used by any screen showing a rolling attendance summary
 * rather than a full calendar month (Dashboard, Subject Performance) - returns (dateFrom, dateTo)
 * formatted as yyyy-MM-dd for AttendanceApi.getAttendance(). Distinct from AttendanceViewModel's
 * own range, which extends dateTo through the end of the current month (needed for its per-month
 * calendar breakdown) rather than stopping at today, so it isn't unified with this helper.
 */
fun recentAttendanceDateRange(): Pair<String, String> {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    val today = Calendar.getInstance()
    val rangeStart = (today.clone() as Calendar).apply {
        add(Calendar.MONTH, -5)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return format.format(rangeStart.time) to format.format(today.time)
}
