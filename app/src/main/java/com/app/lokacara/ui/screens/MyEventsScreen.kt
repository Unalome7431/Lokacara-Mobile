package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.EmptyEventState
import com.app.lokacara.ui.components.EventCard
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.navigation.navigateToCreateEvent
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
                    if (myEvents.isEmpty()) {
                        item {
                            EmptyEventState(onClick = { navController.navigateToCreateEvent() })
                        }
                    } else {
                        items(
                            items = myEvents,
                            key = { event: Event -> event.id }
                        ) { event ->
                            EventCard(
                                event = event,
                                onClick = {
                                    navController.navigate(Screen.EventDetail.createRoute(event.id))
                                },
                                showBookmark = false
                            )
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
