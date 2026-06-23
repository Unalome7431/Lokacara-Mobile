package com.app.lokacara.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.app.lokacara.R
import com.app.lokacara.data.remote.countdownLabel
import com.app.lokacara.data.canHostCancelEvent
import com.app.lokacara.data.remote.formatViewCount
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.ReminderSchedulePanel
import com.app.lokacara.ui.components.rememberEventImageRequest
import com.app.lokacara.ui.components.shimmerBrush
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.navigation.navigateToMainTab
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.theme.Gray200
import com.app.lokacara.ui.theme.Gray400
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray700
import com.app.lokacara.ui.theme.Gray800
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.PlusJakartaSansFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.Secondary100
import com.app.lokacara.ui.theme.Secondary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SemanticErrorLight
import com.app.lokacara.ui.theme.SemanticSuccessBase
import com.app.lokacara.ui.theme.SemanticSuccessLight
import com.app.lokacara.ui.theme.SvgBackground
import com.app.lokacara.viewmodel.EventDetailAction
import com.app.lokacara.viewmodel.EventDetailViewModel

@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: Long = 0L,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isRegistered by viewModel.isRegistered.collectAsStateWithLifecycle()
    val isHost by viewModel.isHost.collectAsStateWithLifecycle()
    val isJoining by viewModel.isJoining.collectAsStateWithLifecycle()
    val isCancelling by viewModel.isCancelling.collectAsStateWithLifecycle()
    val isQrLoading by viewModel.isQrLoading.collectAsStateWithLifecycle()
    val qrToken by viewModel.qrToken.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val lastAction by viewModel.lastAction.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var showCancelEventConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showCollapsedHeader: Boolean by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 100 }
    }

    LaunchedEffect(eventId) {
        if (eventId > 0L) viewModel.loadEvent(eventId) else navController.navigateBackOrHome()
    }

    Scaffold(
        bottomBar = {
            if (event.id != 0L && !isLoading) {
                EventBottomActionBar(
                    isHost = isHost,
                    isRegistered = isRegistered,
                    isJoining = isJoining,
                    onJoin = { viewModel.joinEvent() },
                    onLeave = { showLeaveConfirm = true },
                    onOpenTickets = { navController.navigateToMainTab(Screen.Tickets.route) },
                    onCancelEvent = { showCancelEventConfirm = true },
                    onEditEvent = { navController.navigate("edit_event/${event.id}") }
                )
            }
        },
        containerColor = SvgBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when {
                isLoading && event.id == 0L -> EventDetailLoading(onBack = { navController.navigateBackOrHome() })
                error != null -> EventDetailError(
                    message = error ?: stringResource(R.string.error_occurred),
                    onBack = { navController.navigateBackOrHome() },
                    onRetry = { viewModel.loadEvent(eventId) }
                )
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item(key = "hero", contentType = "hero") {
                                EventHero(
                                    event = event,
                                    onBack = { navController.navigateBackOrHome() },
                                    onShare = { shareEvent(context, event) },
                                    onBookmark = { viewModel.toggleBookmark() }
                                )
                            }

                            item(key = "content", contentType = "content") {
                                EventDetailContent(
                                    event = event,
                                    isRegistered = isRegistered,
                                    isHost = isHost,
                                    isQrLoading = isQrLoading,
                                    qrToken = qrToken,
                                    onOpenMap = { openEventMap(context, event) },
                                    onOpenLink = { openEventLink(context, event) }
                                )
                            }

                            if (isHost) {
                                item(key = "host_management", contentType = "host_management") {
                                    HostManagementPanel(
                                        event = event,
                                        navController = navController,
                                        isCancelling = isCancelling,
                                        onCancelEvent = { showCancelEventConfirm = true }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = showCollapsedHeader,
                            enter = fadeIn(tween(200)) + slideInVertically(tween(200)),
                            exit = fadeOut(tween(200)) + slideOutVertically(tween(200)),
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            CollapsedEventHeader(
                                event = event,
                                onBack = { navController.navigateBackOrHome() },
                                onShare = { shareEvent(context, event) },
                                onBookmark = { viewModel.toggleBookmark() }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.event_detail_cancel_registration),
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Kamu yakin ingin membatalkan pendaftaran event ini?",
                    fontFamily = PlusJakartaSansFont,
                    color = Gray600
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirm = false
                        viewModel.leaveEvent()
                    }
                ) {
                    Text("Batalkan", color = SemanticErrorBase, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = Gray600)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp)
        )
    }

    if (showCancelEventConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isCancelling) showCancelEventConfirm = false },
            title = { Text("Batalkan Event", fontFamily = NunitoFont, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Event \"${event.title}\" akan dibatalkan dan peserta akan menerima pemberitahuan.",
                    fontFamily = PlusJakartaSansFont,
                    color = Gray600
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = {
                        showCancelEventConfirm = false
                        viewModel.cancelEvent()
                    }
                ) {
                    if (isCancelling) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Batalkan Event", color = SemanticErrorBase, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(enabled = !isCancelling, onClick = { showCancelEventConfirm = false }) {
                    Text("Kembali", color = Gray600)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp)
        )
    }

    if (successMessage != null && lastAction == EventDetailAction.JOIN) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = {
                Text(
                    text = stringResource(R.string.event_detail_join_success),
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = Primary500
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.event_detail_join_success_detail, event.title),
                    fontFamily = PlusJakartaSansFont,
                    color = Gray600,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearMessages()
                        navController.navigateToMainTab(Screen.Tickets.route)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Lihat Tiket", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearMessages() }) {
                    Text(stringResource(R.string.ok), color = Gray600, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
private fun EventHero(
    event: Event,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onBookmark: () -> Unit
) {
    val countdown = remember(event.startDatetime) { countdownLabel(event.startDatetime) }
    val overlayBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.18f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.78f)
            )
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(Gray200)
    ) {
        AsyncImage(
            model = rememberEventImageRequest(event.imageUrl, 900),
            contentDescription = event.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayBrush)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroIconButton(icon = Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.back), onClick = onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroIconButton(icon = Icons.Outlined.Share, contentDescription = stringResource(R.string.event_detail_share_title), onClick = onShare)
                HeroIconButton(
                    icon = if (event.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (event.isBookmarked) Secondary500 else Color.White,
                    onClick = onBookmark
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EventPill(text = event.category, color = Secondary500, contentColor = Color.White)
                countdown?.let {
                    EventPill(text = it, color = Color.White.copy(alpha = 0.92f), contentColor = Primary500)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = event.title,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.penyelenggara.ifEmpty { stringResource(R.string.event_detail_unknown_organizer) },
                fontFamily = PlusJakartaSansFont,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CollapsedEventHeader(
    event: Event,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onBookmark: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroIconButton(Icons.Rounded.ArrowBackIosNew, stringResource(R.string.back), onClick = onBack)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroIconButton(Icons.Outlined.Share, stringResource(R.string.event_detail_share_title), onClick = onShare)
                HeroIconButton(
                    icon = if (event.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (event.isBookmarked) Secondary500 else Gray600,
                    onClick = onBookmark
                )
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    isRegistered: Boolean,
    isHost: Boolean,
    isQrLoading: Boolean,
    qrToken: String?,
    onOpenMap: () -> Unit,
    onOpenLink: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        FlatEventStatus(event = event, isRegistered = isRegistered, isHost = isHost, isQrLoading = isQrLoading, qrToken = qrToken)

        HorizontalDivider(thickness = 1.dp, color = Gray100)

        EventInfoGrid(event = event)

        HorizontalDivider(thickness = 1.dp, color = Gray100)

        FlatEventLocation(event = event, onOpenMap = onOpenMap, onOpenLink = onOpenLink)

        HorizontalDivider(thickness = 1.dp, color = Gray100)

        FlatEventDescription(text = event.description)
    }
}

@Composable
private fun FlatEventStatus(
    event: Event,
    isRegistered: Boolean,
    isHost: Boolean,
    isQrLoading: Boolean,
    qrToken: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EventPill(
                text = localizedEventType(event.type),
                color = Primary100.copy(alpha = 0.5f),
                contentColor = Primary500
            )
            EventPill(
                text = event.price,
                color = if (event.price == "Gratis") SemanticSuccessLight else Secondary100.copy(alpha = 0.5f),
                contentColor = if (event.price == "Gratis") SemanticSuccessBase else Secondary500
            )
            if (isHost) EventPill(text = "Host", color = Gray900, contentColor = Color.White)
        }

        val statusText = when {
            isHost -> "Kamu adalah penyelenggara event ini."
            isRegistered -> "Kamu sudah terdaftar. Tiket tersedia di halaman Tiket."
            else -> "Daftar untuk menyimpan tiket dan mendapatkan akses check-in."
        }
        Text(
            text = statusText,
            fontFamily = PlusJakartaSansFont,
            fontSize = 13.sp,
            color = Gray600,
            lineHeight = 19.sp
        )

        if (isRegistered && !isHost) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                color = Primary100.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.QrCode2, contentDescription = null, tint = Primary500, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when {
                            isQrLoading -> "Menyiapkan tiket..."
                            qrToken.isNullOrBlank() -> "Tiket sedang disiapkan"
                            else -> "QR check-in siap digunakan"
                        },
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary500
                    )
                }
            }
        }
    }
}

@Composable
private fun EventInfoGrid(event: Event) {
    val viewCountLabel = remember(event.viewCount) { "${formatViewCount(event.viewCount)}x" }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EventMetricCard(
                icon = Icons.Outlined.CalendarToday,
                label = stringResource(R.string.event_detail_date_label),
                value = event.date,
                modifier = Modifier.weight(1f)
            )
            EventMetricCard(
                icon = Icons.Outlined.ConfirmationNumber,
                label = "Harga",
                value = event.price,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EventMetricCard(
                icon = Icons.Outlined.Groups,
                label = "Kuota",
                value = event.capacity?.let { "$it peserta" } ?: "Tidak dibatasi",
                modifier = Modifier.weight(1f)
            )
            EventMetricCard(
                icon = Icons.Outlined.Visibility,
                label = "Dilihat",
                value = viewCountLabel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EventMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = Primary500, modifier = Modifier.size(20.dp))
            Text(label, fontFamily = PlusJakartaSansFont, fontSize = 11.sp, color = Gray500)
            Text(
                value,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Gray900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FlatEventLocation(
    event: Event,
    onOpenMap: () -> Unit,
    onOpenLink: () -> Unit
) {
    val isOnline = event.type.equals("online", ignoreCase = true)
    val detail = when {
        isOnline && !event.link.isNullOrBlank() -> event.link.orEmpty()
        !event.address.isNullOrBlank() -> event.address.orEmpty()
        else -> event.location
    }
    val canOpen = if (isOnline) !event.link.isNullOrBlank() else event.latitude != null || event.longitude != null || event.location.isNotBlank()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Lokasi",
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Gray900
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Primary100.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isOnline) Icons.Outlined.Language else Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Primary500,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOnline) "Platform online" else stringResource(R.string.event_detail_location_label),
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 12.sp,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.location.ifBlank { if (isOnline) "Online" else "Lokasi belum tersedia" },
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (detail.isNotBlank() && detail != event.location) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = detail,
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 13.sp,
                        color = Gray600,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (canOpen) {
                IconButton(onClick = if (isOnline) onOpenLink else onOpenMap) {
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = "Buka", tint = Secondary500)
                }
            }
        }
    }
}

@Composable
private fun FlatEventDescription(text: String) {
    var expanded by remember(text) { mutableStateOf(false) }
    val shouldCollapse = text.length > 320

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.event_detail_description_title),
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(12.dp))

        androidx.compose.animation.AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "description_expansion"
        ) { isExpanded ->
            val displayText = if (!isExpanded && shouldCollapse) text.take(320).trimEnd() + "..." else text
            Text(
                text = displayText.ifBlank { "Deskripsi event belum tersedia." },
                fontFamily = PlusJakartaSansFont,
                fontSize = 14.sp,
                color = Gray700,
                lineHeight = 24.sp
            )
        }

        if (shouldCollapse) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (expanded) "Tampilkan lebih sedikit" else "Baca selengkapnya",
                    fontFamily = PlusJakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Primary500,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }
        }
    }
}

@Composable
private fun HostManagementPanel(
    event: Event,
    navController: NavController,
    isCancelling: Boolean,
    onCancelEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Kelola Event",
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Gray900
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HostActionCard(
                icon = Icons.Outlined.Groups,
                title = stringResource(R.string.event_detail_view_attendees),
                subtitle = "Pantau peserta",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Attendees.createRoute(event.id)) }
            )
            HostActionCard(
                icon = Icons.Outlined.QrCode2,
                title = stringResource(R.string.event_detail_scan_qr),
                subtitle = "Check-in cepat",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.QrScan.createRoute(event.id)) }
            )
        }
        HostActionCard(
            icon = Icons.Outlined.WorkspacePremium,
            title = "Kelola Sertifikat",
            subtitle = "Atur dan kirim sertifikat",
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(Screen.CertificateManagement.createRoute(event.id)) }
        )
        if (canHostCancelEvent(event)) {
            HostActionCard(
                icon = Icons.Outlined.Cancel,
                title = if (isCancelling) "Membatalkan Event..." else "Batalkan Event",
                subtitle = "Beri tahu seluruh peserta",
                modifier = Modifier.fillMaxWidth(),
                tint = SemanticErrorBase,
                enabled = !isCancelling,
                onClick = onCancelEvent
            )
        }
        ReminderSchedulePanel(startDatetime = event.startDatetime)
    }
}

@Composable
private fun HostActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    tint: Color = Secondary500,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Text(title, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Gray900)
            Text(subtitle, fontFamily = PlusJakartaSansFont, fontSize = 11.sp, color = Gray500, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun EventBottomActionBar(
    isHost: Boolean,
    isRegistered: Boolean,
    isJoining: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onOpenTickets: () -> Unit,
    onCancelEvent: () -> Unit,
    onEditEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            if (isHost) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onCancelEvent,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Batalkan Event", fontWeight = FontWeight.Bold, color = SemanticErrorBase)
                    }
                    Button(
                        onClick = onEditEvent,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                    ) {
                        Text("Edit Detail", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (isRegistered) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onLeave,
                        enabled = !isJoining,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.event_detail_cancel_registration), color = SemanticErrorBase, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onOpenTickets,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                    ) {
                        Text("Lihat Tiket", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onJoin,
                    enabled = !isJoining,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                ) {
                    if (isJoining) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.event_detail_join_event), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDetailLoading(onBack: () -> Unit) {
    val brush = shimmerBrush()
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
                    .background(brush)
            ) {
                HeroIconButton(
                    icon = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.statusBarsPadding().padding(16.dp),
                    onClick = onBack
                )
            }
        }
        item {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (it == 0) 96.dp else 72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailError(message: String, onBack: () -> Unit, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.ArrowBackIosNew,
                contentDescription = stringResource(R.string.back),
                modifier = Modifier.size(22.dp).clickable(onClick = onBack)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(SemanticErrorLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null, tint = SemanticErrorBase)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Gray900,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onRetry, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary500)) {
            Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HeroIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun EventPill(text: String, color: Color, contentColor: Color, fontSize: TextUnit = 11.sp) {
    Surface(color = color, shape = RoundedCornerShape(999.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontFamily = PlusJakartaSansFont,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = contentColor,
            maxLines = 1
        )
    }
}

private fun localizedEventType(type: String?): String {
    return when (type?.lowercase()) {
        "online" -> "Online"
        "offline" -> "Offline"
        else -> "Event"
    }
}

private fun shareEvent(context: Context, event: Event) {
    if (event.id == 0L) return
    val shareUrl = "https://lokacara.my.id/events/${event.id}"
    val shareText = "${event.title} - Lokacara\n${event.date} • ${event.location}\n$shareUrl"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    runCatching {
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.event_detail_share_title)))
    }
}

private fun openEventMap(context: Context, event: Event) {
    val uri = if (event.latitude != null && event.longitude != null) {
        Uri.parse("geo:${event.latitude},${event.longitude}?q=${event.latitude},${event.longitude}(${Uri.encode(event.location)})")
    } else {
        Uri.parse("geo:0,0?q=${Uri.encode(event.address ?: event.location)}")
    }
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

private fun openEventLink(context: Context, event: Event) {
    val rawLink = event.link?.trim().orEmpty()
    if (rawLink.isBlank()) return
    val normalized = if (rawLink.startsWith("http://") || rawLink.startsWith("https://")) rawLink else "https://$rawLink"
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EventDetailScreenPreview() {
    LokacaraMobileTheme {
        EventDetailScreen(navController = rememberNavController())
    }
}
