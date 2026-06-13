package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.*
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*
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
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentLocation by viewModel.currentLocationName.collectAsState()

    val onEventClick = remember {
        { event: Event -> navController.navigate(Screen.EventDetail.createRoute(event.id)) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            isLoading && groupedEvents.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize().padding(top = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) { ShimmerSkeletonCard() }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(2) { ShimmerSkeletonCard() }
                }
            }
            error != null -> ErrorStateView(message = error!!, onRetry = { viewModel.refresh() })
            groupedEvents.isEmpty() -> PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Belum ada event di sekitarmu", fontFamily = NunitoFont, color = Gray500, fontSize = 15.sp)
                        Button(
                            onClick = { navController.navigate(Screen.Explore.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = SvgPrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Jelajahi Event", fontWeight = FontWeight.Bold, color = Color.White) }
                    }
                }
            }
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
                NearbyEventsHeader(currentLocation = currentLocation)
            }

            if (nearbyEvents.isNotEmpty()) {
                item(key = "nearby_events") {
                    val nearbyScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(nearbyScrollState)
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        nearbyEvents.forEach { event ->
                            EventCardCompact(event = event, onClick = { onEventClick(event) })
                        }
                    }
                }
            }

            // Category sections
            val sortedCategories = if (selectedCategory == "Semua") groupedEvents.keys.toList()
                else listOf(selectedCategory)

            if (sortedCategories.isNotEmpty()) {
                item(key = "categories_title") {
                    Text(
                        text = "Kategori",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 4.dp)
                    )
                }

            items(items = sortedCategories, key = { it }) { categoryName ->
                val events = groupedEvents[categoryName] ?: emptyList()
                if (events.isNotEmpty()) {
                    CategoryEventSection(
                        categoryName = categoryName,
                        events = events,
                        onEventClick = onEventClick,
                        onSeeAll = {
                            navController.navigate(Screen.Explore.createRoute(categoryName))
                        }
                    )
                }
            }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
        }
            }
        }
    }
}
