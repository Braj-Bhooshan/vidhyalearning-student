package com.studentprofile.app.presentation.viewmodel

import com.studentprofile.app.domain.models.StudentProfile

sealed interface AuthState {
    object SubdomainRequired : AuthState
    object Unauthenticated : AuthState
    object Loading : AuthState
    data class Authenticated(val studentId: String) : AuthState
    data class StudentSelectionRequired(val parentId: String, val students: List<StudentProfile>) : AuthState
    data class MPINRegistrationRequired(val studentId: String) : AuthState
    data class MPINLoginRequired(val studentId: String) : AuthState
    data class Error(val message: String) : AuthState
}
