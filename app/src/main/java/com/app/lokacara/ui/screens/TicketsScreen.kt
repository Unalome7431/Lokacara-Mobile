package com.app.lokacara.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
                            popUpTo(0) { inclusive = true }
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
    val tabs = listOf(
        stringResource(R.string.tab_tickets_upcoming),
        stringResource(R.string.tab_tickets_history)
    )
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val historyEvents by viewModel.historyEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    if (isLoading) {
        Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary500)
        }
        return
    }

    if (error != null) {
        Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            ErrorStateView(message = error ?: "", onRetry = { viewModel.refresh() })
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
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

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            tabs.forEachIndexed { index, title ->
                Column(
                    modifier = Modifier.weight(1f).clickable { selectedTab = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) Gray900 else Gray500,
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    if (selectedTab == index) {
                        Box(modifier = Modifier.width(60.dp).height(2.dp).background(Gray900, RoundedCornerShape(2.dp)))
                    } else {
                        Box(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                MendatangContent(upcomingEvents)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                RiwayatContent(
                    historyEvents = historyEvents,
                    downloadedCertIds = viewModel.downloadedCertIds.collectAsState().value,
                    onDownloadCert = { viewModel.downloadCertificate(it) }
                )
            }
        }
    }
}

@Composable
fun MendatangContent(upcomingEvents: List<UpcomingEvent>) {
    var selectedEvent by remember { mutableStateOf<UpcomingEvent?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)) {
        if (upcomingEvents.isEmpty()) {
            item {
                EmptyStateView(
                    title = "Belum ada tiket",
                    subtitle = "Gabung event untuk melihat tiket kamu di sini"
                )
            }
        } else {
            item {
                val firstEvent = upcomingEvents.first()
                Text(
                    text = stringResource(R.string.tickets_upcoming_event),
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            items(upcomingEvents) { event ->
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
                userName = "",
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
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)) {
        if (historyEvents.isEmpty()) {
            item {
                EmptyStateView(
                    title = "Belum ada riwayat",
                    subtitle = "Event yang sudah selesai akan muncul di sini"
                )
            }
        } else {
            items(historyEvents) { event ->
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun TicketsScreenPreview() {
    com.app.lokacara.ui.theme.LokacaraMobileTheme {
        TicketsScreen(navController = androidx.navigation.compose.rememberNavController())
    }
}
