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
import androidx.lifecycle.viewmodel.compose.viewModel // Pastikan import ini ada
import androidx.navigation.NavController
import com.app.lokacara.R
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.ui.components.*
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.TicketsViewModel

@Composable
fun TicketsScreen(
    navController: NavController,
    viewModel: TicketsViewModel = viewModel() // Injeksi ViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mendatang", "Riwayat")

    // Collect data dari ViewModel
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val historyEvents by viewModel.historyEvents.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- TOP HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_lokacara),
                contentDescription = "Logo Lokacara",
                modifier = Modifier.height(34.dp),
                contentScale = ContentScale.Fit
            )

            Row {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    tint = SvgOrange,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorit",
                    tint = SvgOrange,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // --- CUSTOM TABS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = index },
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
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(2.dp)
                                .background(Gray900, RoundedCornerShape(2.dp))
                        )
                    } else {
                        Box(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- CONTENT ---
        // Pass data dari ViewModel ke fungsi komponen
        if (selectedTab == 0) {
            MendatangContent(upcomingEvents)
        } else {
            RiwayatContent(historyEvents)
        }
    }
}

@Composable
fun MendatangContent(upcomingEvents: List<UpcomingEvent>) {
    var selectedUpcomingEvent by remember { mutableStateOf<UpcomingEvent?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = "Event Hari Ini",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            BigTicketCard(
                title = "Seminar Ai di Kota Surakarta",
                date = "Minggu, 30 Nov",
                time = "15.00",
                location = "Pura Mangkunegaran",
                uniqueCode = "AI0347",
                userName = "Arrivo Aryanto"
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Event Mendatang",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(upcomingEvents) { event ->
            SmallUpcomingEventCard(
                event = event,
                onClick = { selectedUpcomingEvent = event }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    selectedUpcomingEvent?.let { event ->
        Dialog(onDismissRequest = { selectedUpcomingEvent = null }) {
            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                BigTicketCard(
                    title = event.title,
                    date = event.date,
                    time = event.time,
                    location = event.location,
                    uniqueCode = "WK0123",
                    userName = "Arrivo Aryanto"
                )
            }
        }
    }
}

@Composable
fun RiwayatContent(historyEvents: List<HistoryEvent>) {
    var selectedHistoryEvent by remember { mutableStateOf<HistoryEvent?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
    ) {
        items(historyEvents) { event ->
            HistoryItemCard(
                event = event,
                onClick = { selectedHistoryEvent = event }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    selectedHistoryEvent?.let { event ->
        HistoryDetailDialog(
            event = event,
            onDismiss = { selectedHistoryEvent = null }
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