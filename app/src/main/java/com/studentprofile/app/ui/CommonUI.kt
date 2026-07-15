// CommonUi.kt
package com.studentprofile.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ListItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Assignment


private val DrawerBlue = Color(0xFF0A3487)
private val DrawerBackground = Color(0xFFF5F7FB)
private val CardWhite = Color(0xFFFFFFFF)
private val PrimaryText = Color(0xFF202124)
private val SecondaryText = Color(0xFF6B7280)
private val DividerColor = Color(0xFFE8ECF3)
private val AccentGreen = Color(0xFF22C55E)


data class DrawerMenuItem(
    val title: String,
    val icon: ImageVector
)

private val schoolDrawerItems = listOf(
    DrawerMenuItem(title = "Performance", icon = Icons.Outlined.Person),
    DrawerMenuItem(title = "Attendance", icon = Icons.Outlined.EventAvailable),
    DrawerMenuItem(title = "Assignment", icon = Icons.Outlined.Assignment),
    DrawerMenuItem(title = "Message", icon = Icons.Outlined.Email),
)


@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun SchoolNavigationDrawer(
    modifier: Modifier = Modifier,
    studentName: String = "Yash Tiwari",
    classInfo: String = "Class 10 - A • 1A1A",
    academicSession: String = "Academic Session 2025 - 26",
    onMenuClick: (String) -> Unit = {}
) {

    var showStudentSheet by remember {
        mutableStateOf(false)
    }

    var selectedStudent by remember {
        mutableStateOf(studentName)
    }

    val students = listOf(
        "Rahul Kumar",
        "Priya Kumar",
        "Aryan Kumar"
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp),
        color = DrawerBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DrawerBlue)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 48.dp,
                        bottom = 24.dp
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedStudent.take(1),
                            color = DrawerBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Hello,",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )

                        Text(
                            text = selectedStudent,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = classInfo,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-16).dp)
                    .clickable {
                        showStudentSheet = true
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardWhite
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Student Account",
                            color = PrimaryText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        Text(
                            text = academicSession,
                            color = SecondaryText,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {

                itemsIndexed(schoolDrawerItems) { index, item ->

                    if (index == 4) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Divider(
                            color = DividerColor,
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    DrawerMenuRow(
                        item = item,
                        onClick = {
                            onMenuClick(item.title)
                        }
                    )
                }
            }
        }
    }


    if (showStudentSheet) {

        ModalBottomSheet(
            onDismissRequest = {
                showStudentSheet = false
            }
        ) {

            Text(
                text = "Select Student",
                modifier = Modifier.padding(16.dp)
            )

            students.forEach { student ->

                ListItem(
                    headlineContent = {
                        Text(student)
                    },
                    modifier = Modifier.clickable {

                        selectedStudent = student

                        showStudentSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuRow(
    item: DrawerMenuItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = DrawerBlue,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            color = PrimaryText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SchoolNavigationDrawerPreview() {
    MaterialTheme {
        SchoolNavigationDrawer()
    }
}

