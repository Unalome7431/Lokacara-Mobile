package com.app.lokacara.ui.screens
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.app.lokacara.viewmodel.DateFilter
import com.app.lokacara.viewmodel.ErrorType
import com.app.lokacara.viewmodel.ExploreViewModel
import com.app.lokacara.viewmodel.PriceFilter
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ExploreScreen(
    navController: NavController,
    initialCategory: String = "",
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsStateWithLifecycle()
    val selectedCategoryChip by viewModel.selectedCategoryChip.collectAsStateWithLifecycle()
    val events by viewModel.filteredEvents.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val hasMorePages by viewModel.hasMorePages.collectAsStateWithLifecycle()
    val totalEvents by viewModel.totalEvents.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val errorType by viewModel.errorType.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val priceFilter by viewModel.priceFilter.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val categorySuggestions by viewModel.categorySuggestions.collectAsStateWithLifecycle()
    val showDatePicker by viewModel.showDatePicker.collectAsStateWithLifecycle()
    val activeFilterCount by viewModel.activeFilterCount.collectAsStateWithLifecycle()

    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(initialCategory) {
        viewModel.setInitialCategory(initialCategory)
    }

    val focusManager = LocalFocusManager.current

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val isGridViewState by rememberUpdatedState(isGridView)
    val isLoadingState by rememberUpdatedState(isLoading)
    val isLoadingMoreState by rememberUpdatedState(isLoadingMore)

    LaunchedEffect(listState, gridState, isGridView) {
        snapshotFlow {
            if (isGridViewState) {
                val layoutInfo = gridState.layoutInfo
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisible >= layoutInfo.totalItemsCount - 6
            } else {
                val layoutInfo = listState.layoutInfo
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisible >= layoutInfo.totalItemsCount - 3
            }
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore && !isLoadingState && !isLoadingMoreState) {
                    viewModel.loadNextPage()
                }
            }
    }

    val hasActiveFilter = activeFilterCount > 0

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
        val state = when {
            isLoading && events.isEmpty() && error == null -> "loading"
            error != null && events.isEmpty() -> "error"
            else -> "content"
        }
        when (state) {
                "loading" -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { ExploreHeader() }
                        item { CollapsedSearchBar(onClick = { viewModel.expandSearch() }, onFilterClick = {}, activeFilterCount = activeFilterCount) }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            ExploreCategories(
                                selectedCategory = selectedCategoryChip,
                                onCategorySelected = { viewModel.selectCategoryChip(it) },
                                allCategories = categorySuggestions
                            )
                        }
                        item { ExploreShimmer() }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
                "error" -> {
                    PullToRefreshBox(
                        isRefreshing = isLoading,
                        onRefresh = { viewModel.refresh() }
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item { ExploreHeader() }
                            item {
                                ErrorStateView(
                                    message = error ?: "",
                                    errorType = errorType,
                                    onRetry = { viewModel.refresh() }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
                else -> PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = { viewModel.refresh() }
                ) {
                    if (isGridView && events.isNotEmpty()) {
                        LazyVerticalGrid(
                            modifier = Modifier.fillMaxSize(),
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item(key = "header", span = { GridItemSpan(2) }) { ExploreHeader() }
                            item(key = "search_bar", span = { GridItemSpan(2) }) {
                                AnimatedVisibility(
                                    visible = isSearchExpanded,
                                    enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                                    exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
                                ) {
                                    ExploreExpandedSearch(
                                        viewModel = viewModel,
                                        categorySuggestions = categorySuggestions,
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
                                item(key = "collapsed_search", span = { GridItemSpan(2) }) {
                                    CollapsedSearchBar(
                                        onClick = { viewModel.expandSearch() },
                                        onFilterClick = { showBottomSheet = true },
                                        activeFilterCount = activeFilterCount
                                    )
                                }
                            }
                            item(key = "categories", span = { GridItemSpan(2) }) {
                                Spacer(modifier = Modifier.height(8.dp))
                                ExploreCategories(
                                    selectedCategory = selectedCategoryChip,
                                    onCategorySelected = { viewModel.selectCategoryChip(it) },
                                    allCategories = categorySuggestions
                                )
                            }
                            item(key = "sort_row", span = { GridItemSpan(2) }) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(resultCountLabel(events.size, totalEvents, hasActiveFilter), fontFamily = PlusJakartaSansFont, fontSize = 13.sp, color = Gray500)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.toggleGridView() }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.AutoMirrored.Outlined.ViewList, "List view", tint = Gray500, modifier = Modifier.size(18.dp))
                                        }
                                        SortDropdown(selected = sortOption, onOptionSelected = { viewModel.selectSortOption(it) })
                                    }
                                }
                            }
                            if (error != null && events.isNotEmpty()) {
                                item(key = "error_banner", span = { GridItemSpan(2) }) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)
                                            .background(SemanticErrorLight, RoundedCornerShape(8.dp)).padding(12.dp)
                                    ) {
                                        Text(error ?: "", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = SemanticErrorBase)
                                    }
                                }
                            }
                            items(events, key = { it.id }, contentType = { "event_grid" }) { event ->
                                EventCardCompact(
                                    event = event,
                                    onClick = {
                                        viewModel.onEventClick(event.id)
                                        navController.navigate(Screen.EventDetail.createRoute(event.id))
                                    },
                                    onBookmarkClick = { viewModel.toggleBookmark(event.id.toString()) },
                                    imageCrossfade = false
                                )
                            }
                            if (isLoadingMore) {
                                item(key = "loading_more", span = { GridItemSpan(2) }) {
                                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                        LoadMoreSkeleton()
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LoadMoreSkeleton()
                                    }
                                }
                            }
                            if (!hasMorePages && events.isNotEmpty()) {
                                item(key = "end_of_grid", span = { GridItemSpan(2) }) {
                                    EndOfExploreList()
                                }
                            }
                            item(key = "bottom_spacer", span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState
                        ) {
                            item(key = "header") { ExploreHeader() }
                            item(key = "search_bar") {
                                AnimatedVisibility(
                                    visible = isSearchExpanded,
                                    enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                                    exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
                                ) {
                                    ExploreExpandedSearch(
                                        viewModel = viewModel,
                                        categorySuggestions = categorySuggestions,
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
                                    CollapsedSearchBar(
                                        onClick = { viewModel.expandSearch() },
                                        onFilterClick = { showBottomSheet = true },
                                        activeFilterCount = activeFilterCount
                                    )
                                }
                            }
                            item(key = "categories") {
                                Spacer(modifier = Modifier.height(8.dp))
                                ExploreCategories(
                                    selectedCategory = selectedCategoryChip,
                                    onCategorySelected = { viewModel.selectCategoryChip(it) },
                                    allCategories = categorySuggestions
                                )
                            }
                            item(key = "sort_row") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(resultCountLabel(events.size, totalEvents, hasActiveFilter), fontFamily = PlusJakartaSansFont, fontSize = 13.sp, color = Gray500)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.toggleGridView() }, modifier = Modifier.size(32.dp)) {
                                            Icon(
                                                imageVector = if (isGridView) Icons.AutoMirrored.Outlined.ViewList else Icons.Outlined.GridView,
                                                contentDescription = "Toggle view", tint = Gray500, modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        SortDropdown(selected = sortOption, onOptionSelected = { viewModel.selectSortOption(it) })
                                    }
                                }
                            }
                            if (error != null && events.isNotEmpty()) {
                                item(key = "error_banner") {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)
                                            .background(SemanticErrorLight, RoundedCornerShape(8.dp)).padding(12.dp)
                                    ) {
                                        Text(error ?: "", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = SemanticErrorBase)
                                    }
                                }
                            }

                            if (events.isEmpty() && !isLoading) {
                                item { EmptyStateView(hasActiveFilter = hasActiveFilter, onResetFilters = { viewModel.resetFilters() }) }
                            } else {
                                items(items = events, key = { event -> event.id }, contentType = { "event_list" }) { event ->
                                    EventCard(
                                        event = event,
                                        onClick = {
                                            viewModel.onEventClick(event.id)
                                            navController.navigate(Screen.EventDetail.createRoute(event.id))
                                        },
                                        onBookmarkClick = { viewModel.toggleBookmark(event.id.toString()) },
                                        imageCrossfade = false
                                    )
                                }
                            }
                            if (isLoadingMore) {
                                item(key = "loading_more") {
                                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                        LoadMoreSkeleton()
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LoadMoreSkeleton()
                                    }
                                }
                            }
                            if (!hasMorePages && events.isNotEmpty()) {
                                item(key = "end_of_list") {
                                    EndOfExploreList()
                                }
                            }
                            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { viewModel.dismissDatePicker() },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.setCustomDate(it) }
                            ?: viewModel.dismissDatePicker()
                    }
                ) {
                    Text("Pilih", fontWeight = FontWeight.Bold, color = Primary500)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDatePicker() }) {
                    Text("Batal", color = SemanticErrorBase)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showBottomSheet) {
        FilterBottomSheet(
            sortOption = sortOption,
            onSortChange = { viewModel.selectSortOption(it) },
            priceFilter = priceFilter,
            onPriceChange = { viewModel.selectPriceFilter(it) },
            onReset = { viewModel.resetFilters() },
            onDismiss = { showBottomSheet = false }
        )
    }
}

@Composable
private fun ExploreExpandedSearch(
    viewModel: ExploreViewModel,
    categorySuggestions: List<String>,
    onSearchSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val eventName by viewModel.eventName.collectAsStateWithLifecycle()
    val eventLocation by viewModel.eventLocation.collectAsStateWithLifecycle()
    val locationSuggestions by viewModel.locationSuggestions.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()

    ExpandedSearchSection(
        eventName = eventName,
        onEventNameChange = viewModel::updateEventName,
        onClearEventName = viewModel::clearEventName,
        eventLocation = eventLocation,
        onEventLocationChange = viewModel::updateEventLocation,
        onClearEventLocation = viewModel::clearEventLocation,
        locationSuggestions = locationSuggestions,
        categorySuggestions = categorySuggestions,
        searchHistory = searchHistory,
        onClearHistory = viewModel::clearSearchHistory,
        onSearchSubmit = onSearchSubmit,
        onCancel = onCancel
    )
}

private fun resultCountLabel(visibleCount: Int, totalEvents: Int, hasActiveFilter: Boolean): String {
    return if (!hasActiveFilter && totalEvents > visibleCount) {
        "$visibleCount dari $totalEvents event"
    } else {
        "$visibleCount event ditemukan"
    }
}

@Composable
private fun EndOfExploreList() {
    Text(
        text = "Semua event sudah ditampilkan",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        fontFamily = PlusJakartaSansFont,
        fontSize = 12.sp,
        color = Gray500
    )
}

@Composable
private fun LoadMoreSkeleton() {
    val brush = shimmerBrush()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
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
