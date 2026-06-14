package com.app.lokacara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.app.lokacara.R
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.EventDetailViewModel

@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: Long = 0L,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRegistered by viewModel.isRegistered.collectAsState()
    val isHost by viewModel.isHost.collectAsState()
    val isJoining by viewModel.isJoining.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    var showJoinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        if (eventId > 0L) {
            viewModel.loadEvent(eventId)
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(successMessage) {
        if (!successMessage.isNullOrBlank()) {
            showJoinDialog = true
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Gray50)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Manajemen Event",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Gray900
                )
                Spacer(modifier = Modifier.weight(1f))
                // Spacer kosong untuk menyeimbangkan posisi teks di tengah
                Spacer(modifier = Modifier.size(20.dp))
            }
        },
        containerColor = Gray50
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary500)
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = error ?: stringResource(R.string.error_occurred),
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SemanticErrorBase,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadEvent(eventId) }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
                else -> {
                    // 1. Hero Image Event
                    AsyncImage(
                        model = event.imageUrl,
                        contentDescription = event.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Judul & Deskripsi
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            text = event.title,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Primary500,
                            lineHeight = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = event.description,
                            fontFamily = NunitoFont,
                            fontSize = 14.sp,
                            color = Gray600,
                            lineHeight = 22.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Info Detail Event
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Tanggal
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = Gray600, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = event.date, fontFamily = NunitoFont, fontSize = 14.sp, color = Gray700)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Lokasi
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Gray600, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = event.location, fontFamily = NunitoFont, fontSize = 14.sp, color = Gray700)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Harga Tiket
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = Gray600, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = event.price, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Gray900)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Penyelenggara
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Gray200))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = event.penyelenggara.ifEmpty { "Unknown" }, fontFamily = NunitoFont, fontSize = 14.sp, color = Gray700)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Secondary500)
                            ) {
                                Text(
                                    text = "3 Hari Lagi",
                                    color = Secondary500,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. Analitik Peserta (Terkoneksi ke Data Class)
                    Text(
                        text = "Analitik Peserta",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Gray900,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card Pendaftar
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Gray200),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Pendaftar", fontSize = 14.sp, color = Gray600)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${event.pendaftarCount} / ${event.kuota}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Kuota Terisi", fontSize = 12.sp, color = Gray500)
                            }
                        }

                        // Card Kehadiran
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Gray200),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Kehadiran", fontSize = 14.sp, color = Gray600)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${event.hadirCount}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Secondary500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Hadir via Scan", fontSize = 12.sp, color = Gray500)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 5. Tombol Aksi
                    if (isHost) {
                        HostManagementButtons(
                            eventId = event.id,
                            navController = navController
                        )
                    } else if (isRegistered) {
                        Button(
                            onClick = { viewModel.leaveEvent() },
                            enabled = !isJoining,
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SemanticErrorBase)
                        ) {
                            Text("Batal Daftar", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.joinEvent() },
                            enabled = !isJoining,
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                        ) {
                            Text(stringResource(R.string.event_detail_join_event), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false; viewModel.clearMessages() },
            title = { Text("Berhasil", fontWeight = FontWeight.Bold, color = Primary500) },
            text = { Text(successMessage ?: "", color = Gray600) },
            confirmButton = {
                TextButton(onClick = { showJoinDialog = false; viewModel.clearMessages() }) {
                    Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold, color = Primary500)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun HostManagementButtons(
    eventId: Long,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { navController.navigate(Screen.QrScan.createRoute(eventId)) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
        ) {
            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan QR", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Button(
            // TODO: Pastikan route EditEvent sudah kamu definisikan di navigasimu
            onClick = { navController.navigate("edit_event/$eventId")},
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary500)
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Detail Acara", fontWeight = FontWeight.Bold, color = Color.White)
        }

        OutlinedButton(
            onClick = { navController.navigate(Screen.Attendees.createRoute(eventId)) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Primary500)
        ) {
            Icon(Icons.Outlined.Groups, contentDescription = null, tint = Gray900, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pendaftar", fontWeight = FontWeight.Bold, color = Gray900)
        }

        OutlinedButton(
            onClick = { /* TODO: Add cancel logic */ },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SemanticErrorBase)
        ) {
            Icon(
                imageVector = Icons.Outlined.Cancel,
                contentDescription = null,
                tint = SemanticErrorBase,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Batalkan Event",
                fontWeight = FontWeight.Bold,
                color = SemanticErrorBase
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EventDetailScreenPreview() {
    LokacaraMobileTheme {
        EventDetailScreen(
            navController = rememberNavController(),
            viewModel = hiltViewModel()
        )
    }
}