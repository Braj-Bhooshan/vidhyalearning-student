package com.studentprofile.app.presentation.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.studentprofile.app.presentation.viewmodel.AuthState
import com.studentprofile.app.presentation.viewmodel.AuthViewModel

@Composable
fun AuthGate(
    authViewModel: AuthViewModel,
    onAuthenticated: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.SubdomainRequired -> SubdomainScreen(authViewModel)
        is AuthState.Unauthenticated -> LoginScreen(authViewModel)
        is AuthState.StudentSelectionRequired -> StudentSelectionScreen(state.parentId, state.students, authViewModel)
        is AuthState.MPINRegistrationRequired -> MPINRegistrationScreen(state.studentId, authViewModel)
        is AuthState.MPINLoginRequired -> MPINLoginScreen(state.studentId, authViewModel)
        is AuthState.Error -> {
            // Normally we stay on the current screen and show an error.
            // For simplicity, if we get a top-level error state, we show Login.
            LoginScreen(authViewModel)
        }
        is AuthState.Authenticated -> {
            LaunchedEffect(Unit) { onAuthenticated() }
        }
        is AuthState.Loading -> {
            // No dedicated loading screen; per-screen isLoading flows drive their own spinners.
        }
    }
}
