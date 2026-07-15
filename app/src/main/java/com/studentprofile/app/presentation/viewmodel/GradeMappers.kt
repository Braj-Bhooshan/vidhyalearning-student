package com.studentprofile.app.presentation.viewmodel

import com.studentprofile.app.R
import com.studentprofile.app.domain.models.GradeResponse
import com.studentprofile.app.domain.models.RecentAssessment
import com.studentprofile.app.domain.models.SubjectPerformance
import com.studentprofile.app.ui.performanceProgressColor
import com.studentprofile.app.ui.subjectIcons

fun GradeResponse.toSubjectPerformance(): SubjectPerformance {
    val (iconRes, iconBgRes) = subjectIcons(subject)
    val pct = percentage?.toFloatOrNull() ?: 0f
    return SubjectPerformance(
        name = subject,
        percentage = pct,
        grade = gradeLetter ?: "-",
        iconRes = iconRes,
        iconBgRes = iconBgRes,
        progressColor = performanceProgressColor(pct)
    )
}

// GradeResponse has no exact date field (only academic_year/term/examination_name) - the "date"
// line falls back to term + academic year instead of a calendar date.
fun GradeResponse.toRecentAssessment(): RecentAssessment {
    val (_, iconBgRes) = subjectIcons(subject)
    val pct = percentage?.toFloatOrNull()
    return RecentAssessment(
        title = "${examinationName ?: term ?: "Exam"} - $subject",
        date = listOfNotNull(term, academicYear).joinToString(" • ").ifBlank { "-" },
        marks = pct?.let { "%.1f%%".format(it) } ?: "$marksObtained/$totalMarks",
        grade = gradeLetter ?: "-",
        iconRes = R.drawable.ic_assessment,
        iconBgRes = iconBgRes
    )
}
