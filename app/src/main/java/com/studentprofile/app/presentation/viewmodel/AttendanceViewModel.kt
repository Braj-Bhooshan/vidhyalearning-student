package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.remote.AttendanceApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class AttendanceDayStatus { PRESENT, ABSENT, LATE, NONE }

data class AttendanceDay(
    val dayOfMonth: Int,
    val status: AttendanceDayStatus
)

data class AttendanceMonth(
    val key: String, // "yyyy-MM"
    val label: String, // "July 2026"
    val shortLabel: String, // "Jul"
    val percentage: Int,
    val presentDays: Int,
    val totalDays: Int,
    val days: List<AttendanceDay>,
    // Calendar.DAY_OF_WEEK of the 1st of this month: 1=Sunday..7=Saturday
    val firstDayOfWeek: Int
)

private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

private fun parseStatus(raw: String): AttendanceDayStatus = when (raw.lowercase(Locale.ROOT)) {
    "present" -> AttendanceDayStatus.PRESENT
    "absent" -> AttendanceDayStatus.ABSENT
    "late" -> AttendanceDayStatus.LATE
    else -> AttendanceDayStatus.NONE
}

private fun monthKey(year: Int, monthIndex: Int) = "%04d-%02d".format(year, monthIndex + 1)

private fun buildMonth(year: Int, monthIndex: Int, statusByDay: Map<Int, AttendanceDayStatus>): AttendanceMonth {
    val cal = Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthIndex)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val label = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    val shortLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)

    val dayList = (1..daysInMonth).map { AttendanceDay(it, statusByDay[it] ?: AttendanceDayStatus.NONE) }
    val markedDays = dayList.filter { it.status != AttendanceDayStatus.NONE }
    val presentDays = markedDays.count {
        it.status == AttendanceDayStatus.PRESENT || it.status == AttendanceDayStatus.LATE
    }
    val totalDays = markedDays.size
    val percentage = if (totalDays > 0) presentDays * 100 / totalDays else 0

    return AttendanceMonth(
        key = monthKey(year, monthIndex),
        label = label,
        shortLabel = shortLabel,
        percentage = percentage,
        presentDays = presentDays,
        totalDays = totalDays,
        days = dayList,
        firstDayOfWeek = firstDayOfWeek
    )
}

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceApi: AttendanceApi
) : ViewModel() {

    private val _months = MutableStateFlow<List<AttendanceMonth>>(emptyList())
    val months: StateFlow<List<AttendanceMonth>> = _months.asStateFlow()

    private val _selectedMonthKey = MutableStateFlow<String?>(null)
    val selectedMonthKey: StateFlow<String?> = _selectedMonthKey.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun selectMonth(key: String) {
        _selectedMonthKey.value = key
    }

    fun loadAttendance(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val today = Calendar.getInstance()
                val rangeStart = (today.clone() as Calendar).apply {
                    add(Calendar.MONTH, -5)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val rangeEnd = (today.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                }

                val report = attendanceApi.getAttendance(
                    studentId = studentId,
                    dateFrom = apiDateFormat.format(rangeStart.time),
                    dateTo = apiDateFormat.format(rangeEnd.time)
                )

                val recordsByMonth = report.records.groupBy { it.date.take(7) }

                val monthCursor = rangeStart.clone() as Calendar
                val builtMonths = (0 until 6).map {
                    val year = monthCursor.get(Calendar.YEAR)
                    val monthIndex = monthCursor.get(Calendar.MONTH)
                    val statusByDay = recordsByMonth[monthKey(year, monthIndex)].orEmpty().associate { record ->
                        val dayNum = record.date.substringAfterLast("-").toIntOrNull() ?: 0
                        dayNum to parseStatus(record.status)
                    }
                    val built = buildMonth(year, monthIndex, statusByDay)
                    monthCursor.add(Calendar.MONTH, 1)
                    built
                }

                _months.value = builtMonths
                _selectedMonthKey.value = builtMonths.lastOrNull()?.key
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load attendance."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
