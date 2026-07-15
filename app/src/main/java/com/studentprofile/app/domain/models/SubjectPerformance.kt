package com.studentprofile.app.domain.models

import androidx.annotation.DrawableRes

data class SubjectPerformance(
    val name: String,
    val percentage: Float,
    val grade: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val iconBgRes: Int,
    val progressColor: Int
)
