package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.EventCardCompact
import com.app.lokacara.ui.components.HomeHeader
import com.app.lokacara.ui.components.PopularEventSection
import com.app.lokacara.ui.components.NearbyEventsHeader
import com.app.lokacara.ui.components.CategoryEventSection
import com.app.lokacara.ui.components.ErrorStateView
import com.app.lokacara.ui.theme.*
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val groupedEvents by viewModel.groupedEvents.collectAsState()
    val popularEvents by viewModel.popularEvents.collectAsState()
    val nearbyEvents by viewModel.nearbyEvents.collectAsState()
    val todayEvents by viewModel.todayEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentLocation by viewModel.currentLocationName.collectAsState()

    val onEventClick = remember {
        { event: Event -> navController.navigate(Screen.EventDetail.createRoute(event.id)) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            isLoading && groupedEvents.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

            // Event Hari Ini
            if (todayEvents.isNotEmpty()) {
                item(key = "today_title") {
                    Text(
                        text = "🎯 Event Hari Ini",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                    )
                }
                item(key = "today_events") {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(todayEvents, key = { it.id }) { event ->
                            EventCardCompact(event = event, onClick = { onEventClick(event) })
                        }
                    }
                }
            }

            item(key = "nearby_header") {
                NearbyEventsHeader(currentLocation = currentLocation)
            }

            if (nearbyEvents.isNotEmpty()) {
                item(key = "nearby_events") {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(nearbyEvents, key = { it.id }) { event ->
                            EventCardCompact(event = event, onClick = { onEventClick(event) })
                        }
                    }
                }
            }

            val sortedCategories = if (selectedCategory == "Semua") groupedEvents.keys.toList()
                else listOf(selectedCategory)

            items(items = sortedCategories, key = { it }) { categoryName ->
                val events = groupedEvents[categoryName] ?: emptyList()
                if (events.isNotEmpty()) {
                    val onSeeAll = {
                        viewModel.updateCategory(categoryName)
                        navController.navigate(Screen.Explore.route)
                    }
                    CategoryEventSection(
                        categoryName = categoryName,
                        events = events,
                        onEventClick = onEventClick,
                        onSeeAll = onSeeAll
                    )
                }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
        }
            }
        }
    }
}
