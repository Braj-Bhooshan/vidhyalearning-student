package com.studentprofile.app.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import com.studentprofile.app.presentation.viewmodel.ProfileViewModel
import com.studentprofile.app.ui.StudentColors
import com.studentprofile.app.ui.components.StudentTopBar

@Composable
fun ProfileScreen(
    studentId: Int,
    studentName: String,
    classInfo: String,
    fatherName: String? = null,
    motherName: String? = null,
    onBack: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(studentId) {
        if (studentId != 0) viewModel.load(studentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudentColors.NavyPrimary)
    ) {
        StudentTopBar(
            studentName = studentName,
            classInfo = classInfo,
            onMenuClick = { },
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
            onLogoutClick = onLogoutClick,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .background(StudentColors.BackgroundGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val photoUrl = viewModel.resolvePhotoUrl(detail?.studentPhotoUrl)
                    if (!photoUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(photoUrl).build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.size(80.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = StudentColors.NavyPrimary, modifier = Modifier.size(80.dp))
                            },
                            error = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = StudentColors.NavyPrimary, modifier = Modifier.size(80.dp))
                            },
                            success = { SubcomposeAsyncImageContent() }
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Avatar",
                            tint = StudentColors.NavyPrimary,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    }
                    Text(
                        studentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 12.dp),
                        color = StudentColors.TextPrimary
                    )
                    Text(classInfo.ifBlank { "Student Account" }, color = StudentColors.TextSecondary)

                    Row(modifier = Modifier.padding(top = 16.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Father's Name - ", color = StudentColors.TextSecondary, fontSize = 12.sp)
                            Text("Mother's Name - ", color = StudentColors.TextSecondary, fontSize = 12.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(detail?.fatherName ?: fatherName ?: "-", color = StudentColors.TextPrimary, fontSize = 12.sp)
                            Text(detail?.motherName ?: motherName ?: "-", color = StudentColors.TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (isLoading) {
                Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = StudentColors.NavyPrimary)
                }
            } else if (error != null) {
                Text(
                    error ?: "",
                    color = StudentColors.RedDue,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                detail?.let { d ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Student Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StudentColors.TextPrimary)
                            ProfileDetailRow("Admission No.", d.admissionNo)
                            ProfileDetailRow("Email", d.email)
                            ProfileDetailRow("Phone", d.phone)
                            ProfileDetailRow("Date of Birth", d.dateOfBirth)
                            ProfileDetailRow("Gender", d.gender)
                            ProfileDetailRow("Blood Group", d.bloodGroup)
                        }
                    }

                    val addressParts = listOfNotNull(d.address, d.city, d.state).filter { it.isNotBlank() }
                    if (addressParts.isNotEmpty() || d.emergencyContactName != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Contact & Emergency", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StudentColors.TextPrimary)
                                if (addressParts.isNotEmpty()) {
                                    ProfileDetailRow("Address", addressParts.joinToString(", "))
                                }
                                ProfileDetailRow("Emergency Contact", d.emergencyContactName)
                                ProfileDetailRow("Emergency Phone", d.emergencyContactPhone)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                color = StudentColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                color = StudentColors.TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                modifier = Modifier.weight(1.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 10.dp),
            color = StudentColors.Divider.copy(alpha = 0.5f)
        )
    }
}
