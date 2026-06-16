package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
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
import com.app.lokacara.ui.components.ProfileSubpageSummaryCard
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.navigation.navigateToExplore
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.Secondary500
import com.app.lokacara.viewmodel.BookmarkViewModel

@Composable
fun SavedEventsScreen(
    navController: NavController,
    viewModel: BookmarkViewModel = hiltViewModel()
) {
    val savedEvents by viewModel.savedEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    ProfilePageScaffold(title = "Event Tersimpan", onBack = { navController.navigateBackOrHome() }) {
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
                    item {
                        com.app.lokacara.ui.components.AnimatedEntry(delayMillis = 0) {
                            ProfileSubpageSummaryCard(
                                title = "Event Tersimpan",
                                subtitle = "Event favorit yang bisa kamu akses lagi nanti.",
                                value = savedEvents.size.toString(),
                                valueLabel = "event",
                                icon = Icons.Rounded.Bookmark,
                                accentColor = Secondary500
                            )
                        }
                    }
                    if (savedEvents.isEmpty()) {
                        item {
                            com.app.lokacara.ui.components.AnimatedEntry(delayMillis = 100) {
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))
                                EmptyEventState(
                                    text = "Kamu belum memiliki event favorit. Temukan event menarik di halaman Eksplor!",
                                    onClick = { navController.navigateToExplore() }
                                )
                            }
                        }
                    } else {
                        items(
                            items = savedEvents,
                            key = { event: Event -> event.id }
                        ) { event ->
                            com.app.lokacara.ui.components.AnimatedEntry(delayMillis = 100) {
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
}

@Preview(showBackground = true)
@Composable
fun SavedEventsScreenPreview() {
    LokacaraMobileTheme {
        SavedEventsScreen(navController = rememberNavController())
    }
}
