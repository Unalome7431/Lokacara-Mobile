package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberUpdatedState
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
import com.app.lokacara.ui.navigation.navigateToExplore
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val groupedEvents by viewModel.groupedEvents.collectAsStateWithLifecycle()
    val popularEvents by viewModel.popularEvents.collectAsStateWithLifecycle()
    val upcomingEvents by viewModel.myUpcomingEvents.collectAsStateWithLifecycle()
    val nearbyEvents by viewModel.nearbyEvents.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val feedError by viewModel.feedError.collectAsStateWithLifecycle()
    val categoryError by viewModel.categoryError.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocationName.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val isLocationPickerVisible by viewModel.isLocationPickerVisible.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val hasMorePages = viewModel.hasMorePages

    val onEventClick = remember {
        { event: Event ->
            viewModel.onEventClick(event)
            navController.navigate(Screen.EventDetail.createRoute(event.id))
        }
    }

    if (isLocationPickerVisible) {
        LocationPickerDialog(
            currentLocation = currentLocation,
            onDismiss = { viewModel.dismissLocationPicker() },
            onLocationSelected = { city, lat, lng ->
                viewModel.setManualLocation(city, lat, lng)
            },
            onUseCurrentGps = { viewModel.useCurrentGps() }
        )
    }

    val listState = rememberLazyListState()

    val hasMorePagesState by rememberUpdatedState(hasMorePages)
    val isLoadingMoreState by rememberUpdatedState(isLoadingMore)
    val isLoadingState by rememberUpdatedState(isLoading)
    val sortedCategories = remember(groupedEvents, selectedCategory) {
        if (selectedCategory == "Semua") groupedEvents.keys.toList()
        else listOf(selectedCategory)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            lastVisible >= totalItems - 3
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore && hasMorePagesState && !isLoadingMoreState && !isLoadingState) {
                    viewModel.loadMore()
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            isLoading && groupedEvents.isEmpty() && feedError == null -> HomeLoadingShimmer()
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
                            onClick = { navController.navigateToExplore() },
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

                    item(key = "header", contentType = "header") {
                        HomeHeader(navController = navController)
                    }

                    item(key = "popular_section", contentType = "popular") {
                        Box(modifier = Modifier.animateItem()) {
                            if (popularEvents.isNotEmpty()) {
                                PopularEventSection(
                                    popularEvents = popularEvents,
                                    onEventClick = { onEventClick(it) }
                                )
                            }
                        }
                    }

                    item(key = "upcoming_section", contentType = "upcoming") {
                        Box(modifier = Modifier.animateItem()) {
                            UpcomingEventSection(
                                upcomingEvents = upcomingEvents,
                                onEventClick = { eventId ->
                                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                                },
                                onExploreClick = { navController.navigateToExplore() },
                                onSeeAll = { navController.navigate(Screen.Tickets.route) }
                            )
                        }
                    }

                    item(key = "nearby_header", contentType = "nearby_header") {
                        Box(modifier = Modifier.animateItem()) {
                            NearbyEventsHeader(
                                currentLocation = currentLocation,
                                onLocationClick = { viewModel.showLocationPicker() }
                            )
                        }
                    }

                    if (nearbyEvents.isNotEmpty()) {
                        item(key = "nearby_events", contentType = "nearby_row") {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(nearbyEvents, key = { it.id }, contentType = { "nearby_event" }) { event ->
                                    Box(modifier = Modifier.animateItem()) {
                                        EventCardCompact(
                                            event = event,
                                            onClick = { onEventClick(event) },
                                            onBookmarkClick = { viewModel.toggleBookmark(event.id.toString()) }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        item(key = "nearby_empty", contentType = "nearby_empty") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                                    .background(Gray100, RoundedCornerShape(18.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Belum ada event di sekitar lokasi ini",
                                    fontFamily = PlusJakartaSansFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Gray900
                                )
                                Text(
                                    "Coba ganti kota, gunakan GPS, atau jelajahi semua event.",
                                    fontFamily = PlusJakartaSansFont,
                                    fontSize = 12.sp,
                                    color = Gray500
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.showLocationPicker() },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Pilih Kota", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary500)
                                    }
                                    Button(
                                        onClick = { viewModel.useCurrentGps() },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Pakai GPS", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Text(
                                    text = "Buka Explore",
                                    fontFamily = PlusJakartaSansFont,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SvgOrange,
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .clickable { navController.navigateToExplore() }
                                        .padding(top = 14.dp)
                                )
                            }
                        }
                    }

                    if (sortedCategories.isNotEmpty()) {
                        item(key = "categories_title", contentType = "section_title") {
                            Text(
                                text = "Kategori",
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                color = Primary500,
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 0.dp)
                            )
                        }

                        items(items = sortedCategories, key = { it }, contentType = { "category" }) { categoryName ->
                            val events = groupedEvents[categoryName] ?: emptyList()
                            if (events.isNotEmpty()) {
                                CategoryEventSection(
                                    categoryName = categoryName,
                                    events = events,
                                    onEventClick = onEventClick,
                                    onSeeAll = { navController.navigateToExplore(categoryName) },
                                    onBookmarkClick = { eventId -> viewModel.toggleBookmark(eventId) }
                                )
                            }
                        }
                    }

                    if (categoryError != null) {
                        item(key = "category_error", contentType = "error") {
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

                    if (isLoadingMore) {
                        item(key = "loading_more", contentType = "loading") {
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

                    item(key = "bottom_spacer", contentType = "spacer") { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HomeLoadingShimmer() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
