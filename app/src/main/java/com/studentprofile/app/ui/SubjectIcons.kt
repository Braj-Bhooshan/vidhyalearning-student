package com.studentprofile.app.ui

import com.studentprofile.app.R

fun subjectIcons(subject: String): Pair<Int, Int> = when (subject) {
    "English" -> R.drawable.ic_subject_english to R.drawable.bg_subject_icon_english
    "Mathematics" -> R.drawable.ic_subject_math to R.drawable.bg_subject_icon_math
    "Science" -> R.drawable.ic_subject_science to R.drawable.bg_subject_icon_science
    "Computer" -> R.drawable.ic_subject_computer to R.drawable.bg_subject_icon_computer
    "Hindi" -> R.drawable.ic_subject_hindi to R.drawable.bg_subject_icon_hindi
    else -> R.drawable.ic_subject_english to R.drawable.bg_subject_icon_english
}
