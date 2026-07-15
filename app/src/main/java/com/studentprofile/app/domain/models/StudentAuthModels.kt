package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChildSummary(
    val id: Int,
    @SerialName("student_id") val studentId: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("grade_level") val gradeLevel: String,
    val section: String? = null,
    val session: String? = null,
    @SerialName("mpin_registered") val mpinRegistered: Boolean,
    @SerialName("father_name") val fatherName: String? = null,
    @SerialName("mother_name") val motherName: String? = null,
    @SerialName("student_photo_url") val studentPhotoUrl: String? = null
)

@Serializable
data class MpinRequest(
    @SerialName("student_id") val studentId: Int,
    val mpin: String
)
