package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.components.DetailInfoRow
import com.app.lokacara.ui.navigation.Screen
import coil.compose.AsyncImage
import com.app.lokacara.R
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
    val successMessage by viewModel.successMessage.collectAsState()
    var showJoinDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        if (eventId > 0L) viewModel.loadEvent(eventId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Detail Event",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { showShareDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = Primary500
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        Text(
                            text = error ?: "Terjadi kesalahan",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SemanticErrorBase,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    ) {
                        AsyncImage(
                            model = event.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.65f)
                                        )
                                    )
                                )
                        )

                        Text(
                            text = event.title,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = Secondary500,
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = event.category,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontFamily = PlusJakartaSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column {
                            DetailInfoRow(
                                icon = Icons.Outlined.CalendarToday,
                                label = "Waktu & Tanggal",
                                value = event.date
                            )
                            HorizontalDivider(
                                color = Gray100,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            DetailInfoRow(
                                icon = Icons.Outlined.LocationOn,
                                label = "Lokasi",
                                value = event.location
                            )
                            HorizontalDivider(
                                color = Gray100,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            DetailInfoRow(
                                icon = Icons.Outlined.Groups,
                                label = "Penyelenggara",
                                value = event.penyelenggara.ifEmpty { "Tidak diketahui" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Deskripsi Acara",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Gray900,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = event.description,
                        fontFamily = NunitoFont,
                        fontSize = 14.sp,
                        color = Gray600,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isHost) {
                        HostManagementButtons(
                            eventId = event.id.toLongOrNull() ?: 0L,
                            navController = navController,
                            viewModel = viewModel
                        )
                    } else if (isRegistered) {
                        Button(
                            onClick = { viewModel.leaveEvent() },
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SemanticErrorBase)
                        ) {
                            Text(
                                text = "Batalkan Pendaftaran",
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.joinEvent() },
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                        ) {
                            Text(
                                text = "Gabung Event",
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = {
                Text(
                    text = "Berhasil Bergabung!",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Primary500
                )
            },
            text = {
                Text(
                    text = "Kamu telah bergabung di event \"${event.title}\".\nTiket dapat dilihat di halaman Tiket.",
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 14.sp,
                    color = Gray600,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text(
                        text = "OK",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = Primary500
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = {
                Text(
                    text = "Bagikan Event",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Primary500
                )
            },
            text = {
                Text(
                    text = "Link event \"${event.title}\" telah disalin ke clipboard.",
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 14.sp,
                    color = Gray600,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text(
                        text = "OK",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = Primary500
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun HostManagementButtons(
    eventId: Long,
    navController: NavController,
    viewModel: EventDetailViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { navController.navigate(Screen.Attendees.createRoute(eventId)) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
        ) {
            Text("Lihat Peserta", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Button(
            onClick = { navController.navigate(Screen.QrScan.createRoute(eventId)) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary500)
        ) {
            Text("Scan QR", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Button(
            onClick = { viewModel.sendReminders() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gray700)
        ) {
            Text("Kirim Pengingat", fontWeight = FontWeight.Bold, color = Color.White)
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
