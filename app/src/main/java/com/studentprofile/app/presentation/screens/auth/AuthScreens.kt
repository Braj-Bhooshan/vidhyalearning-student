package com.studentprofile.app.presentation.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.studentprofile.app.presentation.viewmodel.AuthState
import com.studentprofile.app.presentation.viewmodel.AuthViewModel
import com.studentprofile.app.domain.models.StudentProfile
import com.studentprofile.app.R

@Composable
fun SubdomainScreen(authViewModel: AuthViewModel) {
    var domain by remember { mutableStateOf("") }
    val isLoading by authViewModel.isLoading.collectAsState()
    val subdomainError by authViewModel.subdomainError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = "Welcome to Student Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter your subdomain to continue",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = domain,
            onValueChange = { domain = it },
            label = { Text("Subdomain") },
            suffix = { Text(".localtest.me:8002") },
            isError = subdomainError != null,
            supportingText = subdomainError?.let { err -> { Text(err) } },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (domain.isNotBlank()) authViewModel.verifySubdomain(domain.trim()) },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Continue")
            }
        }
    }
}

@Composable
fun LoginScreen(authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val subdomain by authViewModel.subdomain.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val loginError by authViewModel.loginError.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_student_avatar),
            contentDescription = "Student",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text("Student Login", style = MaterialTheme.typography.headlineSmall)
        Text("Subdomain: ${subdomain ?: ""}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email / Username") },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        loginError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { authViewModel.login(email.trim(), password.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login")
            }
        }
    }
}

@Composable
fun StudentSelectionScreen(parentId: String, students: List<StudentProfile>, authViewModel: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Select Student", style = MaterialTheme.typography.headlineSmall)
        Text("Account: $parentId", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(students) { student ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { authViewModel.selectStudent(student.studentId) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(student.displayName, style = MaterialTheme.typography.titleMedium)
                            Text("Class: ${student.classInfo}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MPINRegistrationScreen(studentId: String, authViewModel: AuthViewModel) {
    var mpin by remember { mutableStateOf("") }
    var confirmMpin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Setup MPIN", style = MaterialTheme.typography.headlineSmall)
        Text("For Student ID: $studentId", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = mpin,
            onValueChange = { if (it.length <= 4) mpin = it },
            label = { Text("Enter 4-digit MPIN") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmMpin,
            onValueChange = { if (it.length <= 4) confirmMpin = it },
            label = { Text("Confirm 4-digit MPIN") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (mpin != confirmMpin) {
                    error = "MPINs do not match"
                } else if (mpin.length < 4) {
                    error = "MPIN must be 4 digits"
                } else {
                    authViewModel.registerMPIN(studentId, mpin)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register MPIN")
        }
    }
}

@Composable
fun MPINLoginScreen(studentId: String, authViewModel: AuthViewModel) {
    var mpin by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter MPIN", style = MaterialTheme.typography.headlineSmall)
        Text("Student ID: $studentId", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = mpin,
            onValueChange = { if (it.length <= 4) mpin = it },
            label = { Text("4-digit MPIN") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { authViewModel.verifyMPIN(studentId, mpin) },
            modifier = Modifier.fillMaxWidth(),
            enabled = mpin.length == 4
        ) {
            Text("Login")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = { authViewModel.logout() }) {
            Text("Use different account")
        }
    }
}
