package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.EmptyEventState
import com.app.lokacara.ui.components.EventCard
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.components.ProfileSubpageSummaryCard
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.navigation.navigateToCreateEvent
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.PlusJakartaSansFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.Secondary100
import com.app.lokacara.ui.theme.Secondary500
import com.app.lokacara.ui.theme.SvgBackground
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun MyEventsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val myEvents by viewModel.myEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    ProfilePageScaffold(title = "Event Saya", onBack = { navController.navigateBackOrHome() }) {
        if (isLoading && myEvents.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary500)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray50),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        com.app.lokacara.ui.components.AnimatedEntry(delayMillis = 0) {
                            ProfileSubpageSummaryCard(
                                title = "Event Saya",
                                subtitle = "Event yang kamu kelola sebagai penyelenggara.",
                                value = myEvents.size.toString(),
                                valueLabel = "event",
                                icon = Icons.Rounded.Event,
                                accentColor = Primary500
                            )
                        }
                    }
                    if (myEvents.isEmpty()) {
                        item {
                            com.app.lokacara.ui.components.AnimatedEntry(delayMillis = 100) {
                                Spacer(modifier = Modifier.height(20.dp))
                                EmptyEventState(
                                    text = "Kamu belum memiliki event yang aktif. Mulai buat event pertamamu sekarang!",
                                    onClick = { navController.navigateToCreateEvent() }
                                )
                            }
                        }
                    } else {
                        items(
                            items = myEvents,
                            key = { event: Event -> event.id }
                        ) { event ->
                            com.app.lokacara.ui.components.AnimatedEntry(delayMillis = 100) {
                                EventCard(
                                    event = event,
                                    onClick = {
                                        navController.navigate(Screen.EventDetail.createRoute(event.id))
                                    },
                                    showBookmark = false,
                                    trailingContent = {
                                        IconButton(
                                            onClick = { navController.navigate(Screen.QrScan.createRoute(event.id)) },
                                            modifier = Modifier.size(32.dp).background(Secondary100, CircleShape)
                                        ) {
                                            Icon(androidx.compose.material.icons.Icons.Outlined.QrCode2, null, tint = Secondary500, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { navController.navigate(Screen.Attendees.createRoute(event.id)) },
                                            modifier = Modifier.size(32.dp).background(Primary100, CircleShape)
                                        ) {
                                            Icon(androidx.compose.material.icons.Icons.Outlined.Groups, null, tint = Primary500, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyEventsScreenPreview() {
    LokacaraMobileTheme {
        MyEventsScreen(navController = rememberNavController())
    }
}
