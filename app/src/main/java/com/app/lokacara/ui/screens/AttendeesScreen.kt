package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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

    LaunchedEffect(eventId) {
        viewModel.loadAttendees(eventId)
    }

    var showReminderConfirm by remember { mutableStateOf(false) }

    if (showReminderConfirm) {
        AlertDialog(
            onDismissRequest = { showReminderConfirm = false },
            title = { Text("Kirim Pengingat") },
            text = { Text("Kirim email pengingat ke semua peserta?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.sendReminders()
                    showReminderConfirm = false
                }) { Text("Kirim") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderConfirm = false }) { Text("Batal") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Gray50)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp).clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Peserta",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showReminderConfirm = true }) {
                Icon(Icons.Default.Notifications, contentDescription = "Pengingat", tint = Secondary500)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary500)
            } else if (attendees.isEmpty()) {
                Text(
                    text = "Belum ada peserta",
                    modifier = Modifier.align(Alignment.Center),
                    fontFamily = NunitoFont,
                    color = Gray500
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp)) {
                    items(attendees, key = { it.id }) { attendee ->
                        AttendeeCard(
                            attendee = attendee,
                            onToggle = { viewModel.toggleAttendance(attendee.id) }
                        )
                    }
                }
            }

            error?.let {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter)) {
                    Text(it)
                }
            }
        }
    }
}

@Composable
private fun AttendeeCard(
    attendee: AttendeeDto,
    onToggle: () -> Unit
) {
    val isCheckedIn = attendee.status == "present"
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Person, null, tint = if (isCheckedIn) Secondary500 else Gray400, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attendee.user?.name ?: "Peserta",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Gray900
                )
                Text(
                    text = if (isCheckedIn) "Hadir" else "Belum check-in",
                    fontSize = 12.sp,
                    color = if (isCheckedIn) Secondary500 else Gray500
                )
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isCheckedIn) Icons.Filled.CheckCircle else Icons.Outlined.Cancel,
                    contentDescription = if (isCheckedIn) "Check-out" else "Check-in",
                    tint = if (isCheckedIn) Secondary500 else Gray400,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
