package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Subset of the backend's StudentDetail (GET /api/v1/students/{student_id}), confirmed against
// the local backend's /openapi.json - see ProfileApi. Only the fields the Profile screen shows
// are mapped; the full schema carries ~50 more (correspondence address, medical notes, etc).
@Serializable
data class StudentDetail(
    @SerialName("full_name") val fullName: String = "",
    @SerialName("admission_no") val admissionNo: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    @SerialName("grade_level") val gradeLevel: String = "",
    val section: String? = null,
    @SerialName("blood_group") val bloodGroup: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @SerialName("father_name") val fatherName: String? = null,
    @SerialName("mother_name") val motherName: String? = null,
    @SerialName("emergency_contact_name") val emergencyContactName: String? = null,
    @SerialName("emergency_contact_phone") val emergencyContactPhone: String? = null,
    @SerialName("student_photo_url") val studentPhotoUrl: String? = null
)
