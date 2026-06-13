package com.app.lokacara.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.lokacara.R
import com.app.lokacara.ui.components.*
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import com.app.lokacara.viewmodel.ExploreViewModel

@Composable
fun ExploreScreen(
    navController: NavController,
    initialCategory: String = "",
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsState()
    val eventName by viewModel.eventName.collectAsState()
    val eventLocation by viewModel.eventLocation.collectAsState()
    val eventCategory by viewModel.eventCategory.collectAsState()
    val selectedCategoryChip by viewModel.selectedCategoryChip.collectAsState()
    val events by viewModel.filteredEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val error by viewModel.error.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    // Set initial category from navigation
    LaunchedEffect(initialCategory) {
        viewModel.setInitialCategory(initialCategory)
    }

    val focusManager = LocalFocusManager.current

    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading && !isLoadingMore) {
            viewModel.loadNextPage()
        }
    }

    val allCategoryLabel = stringResource(R.string.category_all)
    val hasActiveFilter = eventName.isNotEmpty() || eventLocation.isNotEmpty() ||
            eventCategory.isNotEmpty() || selectedCategoryChip != allCategoryLabel

    BackHandler(enabled = isSearchExpanded || hasActiveFilter) {
        if (isSearchExpanded) {
            viewModel.collapseSearch()
            focusManager.clearFocus()
        } else if (hasActiveFilter) {
            viewModel.resetFilters()
        } else {
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            isLoading && events.isEmpty() && error == null -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { ExploreHeader() }
                    item { CollapsedSearchBar(onClick = { viewModel.expandSearch() }) }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        ExploreCategories(
                            selectedCategory = selectedCategoryChip,
                            onCategorySelected = { viewModel.selectCategoryChip(it) },
                            allCategories = viewModel.categorySuggestions.value
                        )
                    }
                    item { ExploreShimmer() }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
            error != null && events.isEmpty() -> {
                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = { viewModel.refresh() }
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { ExploreHeader() }
                        item { ErrorStateView(message = error ?: "", onRetry = { viewModel.refresh() }) }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
            else -> PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {

                    item(key = "header") {
                        ExploreHeader()
                    }

                    item(key = "search_bar") {
                        AnimatedVisibility(
                            visible = isSearchExpanded,
                            enter = expandVertically(animationSpec = tween(200)) + fadeIn(tween(200)),
                            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(tween(200))
                        ) {
                            ExpandedSearchSection(
                                eventName = eventName,
                                onEventNameChange = { viewModel.updateEventName(it) },
                                onClearEventName = { viewModel.clearEventName() },
                                eventLocation = eventLocation,
                                onEventLocationChange = { viewModel.updateEventLocation(it) },
                                onClearEventLocation = { viewModel.clearEventLocation() },
                                eventCategory = eventCategory,
                                onEventCategoryChange = { viewModel.updateEventCategory(it) },
                                onClearEventCategory = { viewModel.clearEventCategory() },
                                locationSuggestions = viewModel.locationSuggestions.value,
                                categorySuggestions = viewModel.categorySuggestions.value,
                                onSearchSubmit = {
                                    viewModel.onSearchSubmit()
                                    focusManager.clearFocus()
                                },
                                onCancel = {
                                    viewModel.collapseSearch()
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }

                    if (!isSearchExpanded) {
                        item(key = "collapsed_search") {
                            CollapsedSearchBar(onClick = { viewModel.expandSearch() })
                        }
                    }

                    item(key = "categories") {
                        ExploreCategories(
                            selectedCategory = selectedCategoryChip,
                            onCategorySelected = { viewModel.selectCategoryChip(it) },
                            allCategories = viewModel.categorySuggestions.value
                        )
                    }

                    item(key = "sort_row") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${events.size} event ditemukan",
                                fontFamily = PlusJakartaSansFont,
                                fontSize = 13.sp,
                                color = Gray500
                            )
                            SortDropdown(
                                selected = sortOption,
                                onOptionSelected = { viewModel.selectSortOption(it) }
                            )
                        }
                    }

                    // Inline error banner when there's an error but events still exist
                    if (error != null && events.isNotEmpty()) {
                        item(key = "error_banner") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp)
                                    .background(SemanticErrorLight, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    error ?: "",
                                    fontFamily = PlusJakartaSansFont,
                                    fontSize = 12.sp,
                                    color = SemanticErrorBase
                                )
                            }
                        }
                    }

                    if (events.isEmpty() && !isLoading) {
                        item {
                            EmptyStateView(
                                hasActiveFilter = hasActiveFilter,
                                onResetFilters = { viewModel.resetFilters() }
                            )
                        }
                    } else {
                        items(items = events, key = { it.id }) { event ->
                            EventCard(
                                event = event,
                                onClick = {
                                    viewModel.onEventClick(event.id)
                                    navController.navigate(Screen.EventDetail.createRoute(event.id))
                                },
                                onBookmarkClick = { viewModel.toggleBookmark(event.id.toString()) }
                            )
                        }
                    }

                    if (isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Primary500,
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


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExploreScreenPreview() {
    com.app.lokacara.ui.theme.LokacaraMobileTheme {
        val dummyNavController = androidx.navigation.compose.rememberNavController()
        ExploreScreen(navController = dummyNavController)
    }
}
