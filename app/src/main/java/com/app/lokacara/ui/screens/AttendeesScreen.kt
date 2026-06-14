package com.app.lokacara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Output
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.lokacara.R
import com.app.lokacara.data.remote.dto.AttendeeDto
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.AttendeesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendeesScreen(
    navController: NavController,
    eventId: Long,
    viewModel: AttendeesViewModel = hiltViewModel()
) {
    val attendees by viewModel.attendees.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedAttendeeId by remember { mutableStateOf<Long?>(null) }
    var showReminderConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        viewModel.loadAttendees(eventId)
    }

    if (showReminderConfirm) {
        AlertDialog(
            onDismissRequest = { showReminderConfirm = false },
            title = { Text(stringResource(R.string.attendees_send_reminder), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.attendees_reminder_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.sendReminders()
                    showReminderConfirm = false
                }) { Text(stringResource(R.string.attendees_send), color = Primary500, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showReminderConfirm = false }) { Text(stringResource(R.string.cancel), color = Gray500) }
            },
            containerColor = Color.White
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp).clickable { navController.popBackStack() },
                tint = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.attendees_title),
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(20.dp))
        }

        // Search Bar & Reminder Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f).height(50.dp),
                placeholder = { Text("Nama pendaftar", fontSize = 14.sp, color = Gray500) },
                trailingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Primary500) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary500,
                    unfocusedBorderColor = Gray200,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp),
                singleLine = true
            )

            Button(
                onClick = { showReminderConfirm = true },
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary500)
            ) {
                Icon(Icons.Outlined.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reminder", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary500)
            } else if (attendees.isEmpty()) {
                Text(
                    text = stringResource(R.string.attendees_empty),
                    modifier = Modifier.align(Alignment.Center),
                    fontFamily = NunitoFont,
                    color = Gray500
                )
            } else {
                val filteredAttendees = attendees.filter {
                    it.user?.name?.contains(searchQuery, ignoreCase = true) == true
                }

                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAttendees, key = { it.id }) { attendee ->
                        AttendeeCard(
                            attendee = attendee,
                            isSelected = selectedAttendeeId == attendee.id,
                            onClick = { selectedAttendeeId = attendee.id },
                            onToggle = { viewModel.toggleAttendance(attendee.id) }
                        )
                    }
                }
            }

            error?.let {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text(it) }
            }
        }
    }
}

@Composable
private fun AttendeeCard(
    attendee: AttendeeDto,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val isCheckedIn = attendee.status == "present"

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = if (isSelected) BorderStroke(2.dp, Primary500) else BorderStroke(1.dp, Gray200),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Gray200),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = attendee.user?.name?.take(1)?.uppercase() ?: "P",
                    color = Gray600,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = attendee.user?.name ?: "Peserta",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Gray900,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Chips Status Kehadiran (Klik untuk toggle status)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { onToggle() }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCheckedIn) Secondary500 else Gray200,
                    modifier = Modifier.height(24.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                        Text("Hadir", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isCheckedIn) Color.White else Gray500)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (!isCheckedIn) Gray600 else Gray200,
                    modifier = Modifier.height(24.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (!isCheckedIn) Color.White else Gray500)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Outlined.Output,
                contentDescription = "Export",
                tint = SemanticErrorBase,
                modifier = Modifier.size(20.dp).clickable { /* TODO: Export Logic */ }
            )
        }
    }
}