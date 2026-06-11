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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringResource
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
    val isJoining by viewModel.isJoining.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    var showJoinDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

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
                contentDescription = stringResource(R.string.back),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.event_detail_title),
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
                    contentDescription = stringResource(R.string.event_detail_share_title),
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    ) {
                        AsyncImage(
                            model = event.imageUrl,
                            contentDescription = event.title,
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
                                label = stringResource(R.string.event_detail_date_label),
                                value = event.date
                            )
                            HorizontalDivider(
                                color = Gray100,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            DetailInfoRow(
                                icon = Icons.Outlined.LocationOn,
                                label = stringResource(R.string.event_detail_location_label),
                                value = event.location
                            )
                            HorizontalDivider(
                                color = Gray100,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            DetailInfoRow(
                                icon = Icons.Outlined.Groups,
                                label = stringResource(R.string.event_detail_organizer_label),
                                value = event.penyelenggara.ifEmpty { stringResource(R.string.event_detail_unknown_organizer) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.event_detail_description_title),
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
                            eventId = event.id,
                            navController = navController,
                            viewModel = viewModel
                        )
                    } else if (isRegistered) {
                        Button(
                            onClick = { viewModel.leaveEvent() },
                            enabled = !isJoining,
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SemanticErrorBase)
                        ) {
                            Text(
                                text = stringResource(R.string.event_detail_cancel_registration),
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.joinEvent() },
                            enabled = !isJoining,
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                        ) {
                            Text(
                                text = stringResource(R.string.event_detail_join_event),
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
            onDismissRequest = { showJoinDialog = false; viewModel.clearMessages() },
            title = {
                Text(
                    text = if (isRegistered) "Berhasil" else "Berhasil",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Primary500
                )
            },
            text = {
                Text(
                    text = successMessage ?: "",
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 14.sp,
                    color = Gray600,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showJoinDialog = false; viewModel.clearMessages() }) {
                    Text(
                        text = stringResource(R.string.ok),
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

    val context = androidx.compose.ui.platform.LocalContext.current

    if (showShareDialog) {
        val shareUrl = "https://lokacara.my.id/events/${event.id}"
        val shareText = "${event.title} - Lokacara\n$shareUrl"

        LaunchedEffect(showShareDialog) {
            showShareDialog = false
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }
            val chooser = android.content.Intent.createChooser(shareIntent, context.getString(R.string.event_detail_share_title))
            context.startActivity(chooser)
            android.widget.Toast.makeText(context, "Link berhasil disalin", android.widget.Toast.LENGTH_SHORT).show()
        }
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
            Text(stringResource(R.string.event_detail_view_attendees), fontWeight = FontWeight.Bold, color = Color.White)
        }
        Button(
            onClick = { navController.navigate(Screen.QrScan.createRoute(eventId)) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary500)
        ) {
            Text(stringResource(R.string.event_detail_scan_qr), fontWeight = FontWeight.Bold, color = Color.White)
        }
        Button(
            onClick = { viewModel.sendReminders() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gray700)
        ) {
            Text(stringResource(R.string.event_detail_send_reminder), fontWeight = FontWeight.Bold, color = Color.White)
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
