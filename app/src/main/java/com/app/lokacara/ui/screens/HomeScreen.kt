package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.EventCard
import com.app.lokacara.ui.components.HomeHeader
import com.app.lokacara.ui.components.PopularEventSection
import com.app.lokacara.ui.components.NearbyEventsHeader
import com.app.lokacara.ui.components.EmptyStateView
import com.app.lokacara.ui.components.ErrorStateView
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredEvents by viewModel.filteredEvents.collectAsState()
    val popularEvents by viewModel.popularEvents.collectAsState()
    val locationNames by viewModel.locationNames.collectAsState()
    val categoryNames by viewModel.categoryNames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val onEventClick = remember {
        { event: Event -> navController.navigate(Screen.EventDetail.createRoute(event.id)) }
    }
    val onBookmarkClick: (String) -> Unit = remember {
        { eventId -> viewModel.toggleBookmark(eventId) }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary500)
            }
            error != null -> ErrorStateView(message = error!!, onRetry = { viewModel.refresh() })
            else -> PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

            item(key = "header") {
                HomeHeader(navController = navController)
            }

            item(key = "popular_section") {
                PopularEventSection(
                    popularEvents = popularEvents,
                    onEventClick = { onEventClick(it) }
                )
            }

            item(key = "nearby_header") {
                NearbyEventsHeader(
                    currentLocation = selectedLocation,
                    selectedCategory = selectedCategory,
                    locations = locationNames,
                    categories = categoryNames,
                    onLocationChange = { viewModel.updateLocation(it) },
                    onCategoryChange = { viewModel.updateCategory(it) }
                )
            }

            items(
                items = filteredEvents,
                key = { it.id }
            ) { event ->
                EventCard(
                    event = event,
                    onBookmarkClick = { onBookmarkClick(event.id.toString()) },
                    onClick = { onEventClick(event) }
                )
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
        }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    LokacaraMobileTheme {
        HomeScreen(navController = rememberNavController())
    }
}
