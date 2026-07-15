package com.studentprofile.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.studentprofile.app.presentation.screens.auth.AuthGate
import com.studentprofile.app.presentation.screens.dashboard.StudentMainScreen
import com.studentprofile.app.presentation.viewmodel.AuthState
import com.studentprofile.app.presentation.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Authenticated) Routes.STUDENT_DASHBOARD else Routes.SUBDOMAIN_AUTH
    ) {
        composable(Routes.SUBDOMAIN_AUTH) {
            AuthGate(
                authViewModel = authViewModel,
                onAuthenticated = {
                    navController.navigate(Routes.STUDENT_DASHBOARD) {
                        popUpTo(Routes.SUBDOMAIN_AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.STUDENT_DASHBOARD) {
            StudentMainScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.SUBDOMAIN_AUTH) {
                        popUpTo(Routes.STUDENT_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
    }
}
