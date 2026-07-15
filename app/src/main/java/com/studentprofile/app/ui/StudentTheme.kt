package com.studentprofile.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.studentprofile.app.R

object StudentColors {
    val NavyPrimary = Color(0xFF002874)
    val NavyDark = Color(0xFF001B4F)
    val NavyMedium = Color(0xFF003399)

    val BackgroundGray = Color(0xFFF7F9FC)
    val BackgroundLight = Color(0xFFF0F4F8)

    val GreenSuccess = Color(0xFF28C76F)
    val GreenLightBg = Color(0xFFE8F5E9)
    val AmberPending = Color(0xFFFF9F43)
    val AmberLightBg = Color(0xFFFFF3E0)
    val RedDue = Color(0xFFEA5455)
    val BlueAccent = Color(0xFF2196F3)
    val BlueLight = Color(0xFFE3F2FD)

    val TextPrimary = Color(0xFF2D3748)
    val TextSecondary = Color(0xFF718096)
    val TextTertiary = Color(0xFFA0AEC0)

    val BorderLight = Color(0xFFE2E8F0)
    val BorderMedium = Color(0xFFCBD5E0)
    val Divider = Color(0xFFEDF2F7)

    val NotificationBadge = Color(0xFFFF6B00)
    val StarGold = Color(0xFFFFD700)

    // Subject icon fg/bg pairs
    val EnglishBg = Color(0xFFFFF3E0); val EnglishFg = Color(0xFFFF9800)
    val HindiBg = Color(0xFFFCE4EC); val HindiFg = Color(0xFFE91E63)
    val MathBg = Color(0xFFE3F2FD); val MathFg = Color(0xFF2196F3)
    val ScienceBg = Color(0xFFE8F5E9); val ScienceFg = Color(0xFF4CAF50)
    val SocialBg = Color(0xFFF3E5F5); val SocialFg = Color(0xFF9C27B0)
    val ComputerBg = Color(0xFFFFF8E1); val ComputerFg = Color(0xFFFFC107)
    val SanskritBg = Color(0xFFE0F7FA); val SanskritFg = Color(0xFF00BCD4)

    // Progress rings
    val ProgressAttendance = Color(0xFF28C76F)
    val ProgressAvgScore = Color(0xFF002874)
    val ProgressHomework = Color(0xFFFF9F43)
    val ProgressTrack = Color(0xFFE8ECF0)

    val InfoBannerBg = Color(0xFFEBF5FF)
    val InfoBannerText = Color(0xFF1A73E8)

    val BottomNavActive = Color(0xFF002874)
    val BottomNavInactive = Color(0xFFA0AEC0)

    val TabActive = Color(0xFF002874)
    val TabInactive = Color(0xFF718096)
}

/**
 * The legacy `bg_subject_icon_*` drawables are plain solid-color rounded-rect shapes
 * (not vectors/rasters), so they can't be loaded via `painterResource`. This maps the
 * same drawable IDs to their equivalent Compose color for use as a Box background.
 */
fun subjectIconBackgroundColor(iconBgRes: Int): Color = when (iconBgRes) {
    R.drawable.bg_subject_icon_english -> StudentColors.EnglishBg
    R.drawable.bg_subject_icon_hindi -> StudentColors.HindiBg
    R.drawable.bg_subject_icon_math -> StudentColors.MathBg
    R.drawable.bg_subject_icon_science -> StudentColors.ScienceBg
    R.drawable.bg_subject_icon_social -> StudentColors.SocialBg
    R.drawable.bg_subject_icon_computer -> StudentColors.ComputerBg
    R.drawable.bg_subject_icon_sanskrit -> StudentColors.SanskritBg
    else -> StudentColors.BlueLight
}

/** Maps a raw percentage (0-100) from backend grade/attendance data to a semantic progress color. */
fun performanceProgressColor(percentage: Float): Int = when {
    percentage >= 75f -> StudentColors.GreenSuccess.toArgb()
    percentage >= 50f -> StudentColors.AmberPending.toArgb()
    else -> StudentColors.RedDue.toArgb()
}
