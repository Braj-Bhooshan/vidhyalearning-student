package com.studentprofile.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import com.studentprofile.app.domain.models.StudentProfile
import com.studentprofile.app.ui.StudentColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSwitchSheet(
    students: List<StudentProfile>,
    onSelect: (StudentProfile) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                "Switch Student",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            students.forEach { student ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(student) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!student.photoUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(student.photoUrl).build(),
                            contentDescription = student.displayName,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = StudentColors.NavyPrimary, modifier = Modifier.size(40.dp))
                            },
                            error = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = StudentColors.NavyPrimary, modifier = Modifier.size(40.dp))
                            },
                            success = { SubcomposeAsyncImageContent() }
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = student.displayName,
                            tint = StudentColors.NavyPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(student.displayName, fontWeight = FontWeight.Bold)
                        Text(
                            listOfNotNull(student.classInfo, student.section?.let { "Section: $it" }).joinToString(" • "),
                            color = StudentColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}
