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
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.viewmodel.BookmarkViewModel

@Composable
fun SavedEventsScreen(
    navController: NavController,
    viewModel: BookmarkViewModel = hiltViewModel()
) {
    val savedEvents by viewModel.savedEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    ProfilePageScaffold(title = "Event Tersimpan", onBack = { navController.popBackStack() }) {
        if (isLoading && savedEvents.isEmpty()) {
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
                    if (savedEvents.isEmpty()) {
                        item {
                            EmptyEventState(
                                text = "Belum Ada Event Tersimpan\nCari Event Disini",
                                onClick = { navController.navigate(Screen.Explore.createRoute("")) { launchSingleTop = true } }
                            )
                        }
                    } else {
                        items(
                            items = savedEvents,
                            key = { event: Event -> event.id }
                        ) { event ->
                            EventCard(
                                event = event,
                                onClick = {
                                    navController.navigate(Screen.EventDetail.createRoute(event.id))
                                },
                                onBookmarkClick = { viewModel.toggleBookmark(event.id.toString()) }
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
fun SavedEventsScreenPreview() {
    LokacaraMobileTheme {
        SavedEventsScreen(navController = rememberNavController())
    }
}
