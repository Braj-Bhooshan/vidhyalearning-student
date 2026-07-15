package com.studentprofile.app.domain.models

import androidx.annotation.DrawableRes

data class RecentAssessment(
    val title: String,
    val date: String,
    val marks: String,
    val grade: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val iconBgRes: Int
)
