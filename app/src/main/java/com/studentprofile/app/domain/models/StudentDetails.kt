package com.studentprofile.app.domain.models

data class StudentDetails(
    val student: StudentProfile,
    val fatherName: String? = null,
    val motherName: String? = null
)
