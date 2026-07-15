package com.studentprofile.app.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.studentprofile.app.presentation.viewmodel.AuthViewModel
import com.studentprofile.app.ui.StudentColors

@Composable
fun StudentTopBar(
    studentName: String,
    classInfo: String,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    photoUrl: String? = null,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    unreadCount: Int = 0,
    // AuthViewModel is created Activity-scoped in AppNavigation (outside the NavHost), so it
    // must be looked up via that same ComponentActivity scope here too - a bare hiltViewModel()
    // call from inside a NavHost destination resolves to the NavBackStackEntry's ViewModelStore
    // instead and would create a SEPARATE AuthViewModel instance, whose init{} wipes the shared
    // TenantProvider (subdomain/access token) out from under the real session.
    authViewModel: AuthViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val schoolLogoUrl by authViewModel.schoolLogoUrl.collectAsState()
    val details by authViewModel.selectedStudentDetails.collectAsState()
    val effectivePhotoUrl = photoUrl ?: details?.student?.photoUrl

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudentColors.NavyPrimary)
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 30.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            } else {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (!schoolLogoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(schoolLogoUrl)
                            .build(),
                        contentDescription = "School Logo",
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, contentDescription = "School Logo", tint = Color.White)
                }
            }

            Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                Text("Hello, $studentName", color = Color.White, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(classInfo, color = Color(0xFFB0C4DE))
                }
            }

            Box {
                IconButton(onClick = onNotificationClick) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 6.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StudentColors.NotificationBadge)
                    )
                }
            }

            ProfileMenuButton(
                photoUrl = effectivePhotoUrl,
                onProfileClick = onProfileClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun ProfileMenuButton(
    photoUrl: String?,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .build(),
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile menu", tint = Color.White)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("My Profile") },
                onClick = { expanded = false; onProfileClick() }
            )
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = { expanded = false; onLogoutClick() }
            )
        }
    }
}
