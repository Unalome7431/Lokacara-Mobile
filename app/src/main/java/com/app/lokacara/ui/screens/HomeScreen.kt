package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.components.EventCard
import com.app.lokacara.ui.components.HomeHeader
import com.app.lokacara.ui.components.PopularEventSection
import com.app.lokacara.ui.components.NearbyEventsHeader
import com.app.lokacara.ui.components.BottomNavbar
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredEvents by viewModel.filteredEvents.collectAsState()
    val popularEvents by viewModel.popularEvents.collectAsState()

    val onEventClick = remember {
        { navController.navigate(Screen.EventDetail.route) }
    }
    val onBookmarkClick: (String) -> Unit = remember {
        { eventId -> viewModel.toggleBookmark(eventId) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            item(key = "header") {
                HomeHeader(navController = navController)
            }

            item(key = "popular_section") {
                PopularEventSection(
                    popularEvents = popularEvents,
                    onEventClick = { onEventClick() }
                )
            }

            item(key = "nearby_header") {
                NearbyEventsHeader(
                    currentLocation = selectedLocation,
                    selectedCategory = selectedCategory,
                    locations = viewModel.locations,
                    categories = viewModel.categories,
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
                    onBookmarkClick = { onBookmarkClick(event.id) },
                    onClick = onEventClick
                )
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
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