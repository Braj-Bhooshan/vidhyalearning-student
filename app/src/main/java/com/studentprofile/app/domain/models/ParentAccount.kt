package com.studentprofile.app.domain.models

data class ParentAccount(
    val parentId: String,
    val password: String,
    val children: List<StudentProfile> = emptyList()
)
