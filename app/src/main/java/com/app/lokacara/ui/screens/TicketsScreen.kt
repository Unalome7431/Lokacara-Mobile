package com.app.lokacara.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.app.lokacara.R
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.ui.components.*
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.TicketsViewModel

@Composable
fun TicketsScreen(
    navController: NavController,
    rootNavController: NavController? = null,
    viewModel: TicketsViewModel = hiltViewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isAuthChecked by viewModel.isAuthChecked.collectAsState()

    if (!isAuthChecked) {
        Box(Modifier.fillMaxSize().background(SvgBackground), contentAlignment = Alignment.Center) {
            TicketLoadingState()
        }
        return
    }

    if (!isLoggedIn) {
        Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Silakan login untuk melihat tiket",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        (rootNavController ?: navController).navigate(com.app.lokacara.ui.navigation.Screen.Login.route) {
                            launchSingleTop = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Login", color = Color.White)
                }
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val todayEvents by viewModel.todayEvents.collectAsState()
    val historyEvents by viewModel.historyEvents.collectAsState()
    val tabs = listOf(
        "${stringResource(R.string.tab_tickets_upcoming)} (${upcomingEvents.size})",
        "${stringResource(R.string.tab_tickets_history)} (${historyEvents.size})"
    )
    val userName by viewModel.userName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val downloadedCertIds by viewModel.downloadedCertIds.collectAsState()
    val hasContent = todayEvents.isNotEmpty() || upcomingEvents.isNotEmpty() || historyEvents.isNotEmpty()

    if (isLoading && !hasContent) {
        Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            TicketLoadingState()
        }
        return
    }

    if (error != null && !hasContent) {
        Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            ErrorStateView(message = error ?: "", onRetry = { viewModel.refresh() })
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(SvgBackground)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_lokacara),
                contentDescription = "Logo",
                modifier = Modifier.height(34.dp),
                contentScale = ContentScale.Fit
            )
            Row {
                IconButton(onClick = { navController.navigate(com.app.lokacara.ui.navigation.Screen.Notification.route) }) {
                    Icon(Icons.Outlined.Notifications, null, tint = SvgOrange, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { navController.navigate(com.app.lokacara.ui.navigation.Screen.Bookmark.route) }) {
                    Icon(Icons.Outlined.FavoriteBorder, null, tint = SvgOrange, modifier = Modifier.size(26.dp))
                }
            }
        }

        LiveTicketsSection(
            todayEvents = todayEvents,
            userName = userName
        )

        TicketTabRow(
            tabs = tabs,
            selectedTab = selectedTab,
            onSelect = { selectedTab = it }
        )

        if (error != null) {
            InlineTicketError(message = error ?: "", onRetry = { viewModel.refresh() })
        }

        if (selectedTab == 0) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                MendatangContent(upcomingEvents, userName = userName)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                RiwayatContent(
                    historyEvents = historyEvents,
                    downloadedCertIds = downloadedCertIds,
                    onDownloadCert = { viewModel.downloadCertificate(it) }
                )
            }
        }
    }
}

@Composable
fun MendatangContent(
    upcomingEvents: List<UpcomingEvent>,
    userName: String
) {
    var selectedEvent by remember { mutableStateOf<UpcomingEvent?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (upcomingEvents.isEmpty()) {
            item {
                EmptyStateView(
                    title = "Belum ada tiket",
                    subtitle = "Gabung event untuk melihat tiket kamu di sini"
                )
            }
        } else {
            items(upcomingEvents, key = { it.id }) { event ->
                SmallUpcomingEventCard(event, onClick = { selectedEvent = event })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    selectedEvent?.let { event ->
        Dialog(onDismissRequest = { selectedEvent = null }) {
                BigTicketCard(
                    title = event.title,
                    date = event.date,
                    time = event.time,
                    location = event.location,
                    uniqueCode = event.id.toString(),
                    qrData = event.qrToken ?: event.id.toString(),
                    userName = userName.ifBlank { "Peserta Lokacara" },
                    onQrClick = { showQrDialog = true }
                )
            }
    }

    val currentEvent = selectedEvent
    if (showQrDialog && currentEvent != null) {
        QrCodeDialog(
            qrData = currentEvent.qrToken ?: currentEvent.id.toString(),
            onDismiss = { showQrDialog = false }
        )
    }
}

@Composable
fun RiwayatContent(
    historyEvents: List<HistoryEvent>,
    downloadedCertIds: Set<Long> = emptySet(),
    onDownloadCert: (HistoryEvent) -> Unit = {}
) {
    var selectedEvent by remember { mutableStateOf<HistoryEvent?>(null) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (historyEvents.isEmpty()) {
            item {
                EmptyStateView(
                    title = "Belum ada riwayat",
                    subtitle = "Event yang sudah selesai akan muncul di sini"
                )
            }
        } else {
            items(historyEvents, key = { it.id }) { event ->
                HistoryItemCard(event, onClick = { selectedEvent = event })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
    selectedEvent?.let {
        HistoryDetailDialog(
            event = it,
            onDismiss = { selectedEvent = null },
            onDownload = { onDownloadCert(it) },
            isDownloaded = it.id in downloadedCertIds
        )
    }
}

@Composable
private fun LiveTicketsSection(
    todayEvents: List<UpcomingEvent>,
    userName: String
) {
    if (todayEvents.isEmpty()) return

    var selectedTicket by remember { mutableStateOf<UpcomingEvent?>(null) }
    val resolvedName = userName.ifBlank { "Peserta Lokacara" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sedang Berlangsung",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Gray900
                )
                Text(
                    text = "Tiket untuk event hari ini",
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 12.sp,
                    color = Gray500
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            todayEvents.forEach { event ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White
                ) {
                    BigTicketCard(
                        title = event.title,
                        date = event.date,
                        time = event.time,
                        location = event.location,
                        uniqueCode = event.id.toString(),
                        qrData = event.qrToken ?: event.id.toString(),
                        userName = resolvedName,
                        onQrClick = { selectedTicket = event }
                    )
                }
            }
        }
    }

    selectedTicket?.let { event ->
        QrCodeDialog(
            qrData = event.qrToken ?: event.id.toString(),
            onDismiss = { selectedTicket = null }
        )
    }
}

@Composable
private fun TicketTabRow(
    tabs: List<String>,
    selectedTab: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = selectedTab == index
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = if (selected) Primary500 else Gray500,
                    fontFamily = PlusJakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(3.dp)
                        .background(if (selected) Primary500 else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun TicketSectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFont,
                fontSize = 12.sp,
                color = Gray500
            )
        }
    }
}

@Composable
private fun InlineTicketError(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        color = SemanticErrorLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                fontFamily = PlusJakartaSansFont,
                fontSize = 12.sp,
                color = SemanticErrorBase,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = onRetry) {
                Text("Coba lagi", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TicketLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Primary500)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Memuat tiket...",
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Gray700
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun TicketsScreenPreview() {
    com.app.lokacara.ui.theme.LokacaraMobileTheme {
        TicketsScreen(navController = androidx.navigation.compose.rememberNavController())
    }
}
