package com.studentprofile.app.domain.models

import androidx.annotation.DrawableRes

data class ClassSession(
    val timeRange: String,
    val subject: String,
    val chapter: String,
    val teacher: String,
    val status: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val iconBgRes: Int
)
