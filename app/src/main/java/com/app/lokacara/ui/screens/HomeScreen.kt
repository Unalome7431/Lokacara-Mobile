package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val groupedEvents by viewModel.groupedEvents.collectAsState()
    val popularEvents by viewModel.popularEvents.collectAsState()
    val nearbyEvents by viewModel.nearbyEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val feedError by viewModel.feedError.collectAsState()
    val categoryError by viewModel.categoryError.collectAsState()
    val currentLocation by viewModel.currentLocationName.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val isLocationPickerVisible by viewModel.isLocationPickerVisible.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val hasMorePages = viewModel.hasMorePages

    val listState = rememberLazyListState()

    val onEventClick = remember {
        { event: Event ->
            viewModel.onEventClick(event)
            navController.navigate(Screen.EventDetail.createRoute(event.id))
        }
    }

    // Load more when reaching the end
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to totalItems
        }.collect { (lastVisible, totalItems) ->
            if (lastVisible >= totalItems - 3 && hasMorePages && !isLoadingMore && !isLoading) {
                viewModel.loadMore()
            }
        }
    }

    // Location picker dialog
    if (isLocationPickerVisible) {
        LocationPickerDialog(
            currentLocation = currentLocation,
            onDismiss = { viewModel.dismissLocationPicker() },
            onLocationSelected = { city, lat, lng ->
                viewModel.setManualLocation(city, lat, lng)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            isLoading && groupedEvents.isEmpty() && feedError == null -> LoadingShimmer()
            feedError != null && groupedEvents.isEmpty() -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() }
                ) {
                    ErrorStateView(message = feedError!!, onRetry = { viewModel.refresh() })
                }
            }
            groupedEvents.isEmpty() && feedError == null -> PullToRefreshBox(
                isRefreshing = isRefreshing,
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
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {

                    item(key = "header") {
                        HomeHeader(navController = navController)
                    }

                    // ── Popular Events ──
                    item(key = "popular_section") {
                        if (popularEvents.isNotEmpty()) {
                            PopularEventSection(
                                popularEvents = popularEvents,
                                onEventClick = { onEventClick(it) }
                            )
                        }
                    }

                    // ── Nearby Events ──
                    item(key = "nearby_header") {
                        NearbyEventsHeader(
                            currentLocation = currentLocation,
                            onLocationClick = { viewModel.showLocationPicker() }
                        )
                    }

                    if (nearbyEvents.isNotEmpty()) {
                        item(key = "nearby_events") {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(nearbyEvents, key = { it.id }) { event ->
                                    EventCardCompact(
                                        event = event,
                                        onClick = { onEventClick(event) },
                                        onBookmarkClick = { viewModel.toggleBookmark(event.id.toString()) }
                                    )
                                }
                            }
                        }
                    } else {
                        item(key = "nearby_empty") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                Text(
                                    "Tidak ada event di sekitar Anda saat ini",
                                    fontFamily = PlusJakartaSansFont,
                                    fontSize = 13.sp,
                                    color = Gray400
                                )
                            }
                        }
                    }

                    // ── Category Sections ──
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
                                    },
                                    onBookmarkClick = { eventId -> viewModel.toggleBookmark(eventId) }
                                )
                            }
                        }
                    }

                    // ── Category error ──
                    if (categoryError != null) {
                        item(key = "category_error") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    categoryError ?: "",
                                    fontFamily = PlusJakartaSansFont,
                                    fontSize = 13.sp,
                                    color = SemanticErrorBase
                                )
                            }
                        }
                    }

                    // ── Load more indicator ──
                    if (isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = SvgPrimaryBlue,
                                    strokeWidth = 2.dp
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

@Composable
private fun LoadingShimmer() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header shimmer
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(34.dp)
                    .background(Gray200.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Gray200.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ShimmerSkeletonCardRow(cardCount = 1, height = 200)
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerSkeletonCardRow(cardCount = 3, height = 14)
        Spacer(modifier = Modifier.height(32.dp))
        ShimmerSkeletonCardRow(cardCount = 1, height = 14)
        ShimmerSkeletonCardRow(cardCount = 3, height = 110)
        Spacer(modifier = Modifier.height(24.dp))
        ShimmerSkeletonCardRow(cardCount = 1, height = 14)
        ShimmerSkeletonCardRow(cardCount = 3, height = 110)
    }
}

@Composable
private fun ShimmerSkeletonCardRow(cardCount: Int, height: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = if (cardCount == 1) Arrangement.Start else Arrangement.spacedBy(12.dp)
    ) {
        repeat(cardCount) {
            Box(
                modifier = Modifier
                    .then(if (cardCount == 1) Modifier.fillMaxWidth() else Modifier.width(if (height > 100) 160.dp else 80.dp))
                    .height(height.dp)
                    .background(Gray200.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            )
        }
    }
}
