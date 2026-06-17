package com.app.lokacara.ui.screens

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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.rememberSaveable
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
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsState()
    val eventName by viewModel.eventName.collectAsState()
    val eventLocation by viewModel.eventLocation.collectAsState()
    val eventCategory by viewModel.eventCategory.collectAsState()
    val selectedCategoryChip by viewModel.selectedCategoryChip.collectAsState()
    val events by viewModel.filteredEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val error by viewModel.error.collectAsState()
    val errorType by viewModel.errorType.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val priceFilter by viewModel.priceFilter.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val locationSuggestions by viewModel.locationSuggestions.collectAsState()
    val categorySuggestions by viewModel.categorySuggestions.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()

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

    val allCategoryLabel = "Semua"
    val hasActiveFilter = eventName.isNotEmpty() || eventLocation.isNotEmpty() ||
            eventCategory.isNotEmpty() || selectedCategoryChip != allCategoryLabel ||
            priceFilter != PriceFilter.SEMUA || dateFilter != DateFilter.SEMUA
    val activeFilterCount = listOf(
        eventName.isNotEmpty(),
        eventLocation.isNotEmpty(),
        eventCategory.isNotEmpty(),
        selectedCategoryChip != allCategoryLabel,
        priceFilter != PriceFilter.SEMUA,
        dateFilter != DateFilter.SEMUA
    ).count { it }

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
        AnimatedContent(
            targetState = when {
                isLoading && events.isEmpty() && error == null -> "loading"
                error != null && events.isEmpty() -> "error"
                else -> "content"
            },
            transitionSpec = {
                fadeIn(tween(250)) togetherWith fadeOut(tween(150))
            },
            label = "explore_state"
        ) { state ->
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
                                    ExpandedSearchSection(
                                        eventName = eventName,
                                        onEventNameChange = { viewModel.updateEventName(it) },
                                        onClearEventName = { viewModel.clearEventName() },
                                        eventLocation = eventLocation,
                                        onEventLocationChange = { viewModel.updateEventLocation(it) },
                                        onClearEventLocation = { viewModel.clearEventLocation() },
                                        locationSuggestions = locationSuggestions,
                                        categorySuggestions = categorySuggestions,
                                        searchHistory = searchHistory,
                                        onClearHistory = { viewModel.clearSearchHistory() },
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
                                    Text("${events.size} event ditemukan", fontFamily = PlusJakartaSansFont, fontSize = 13.sp, color = Gray500)
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
                                Crossfade(targetState = selectedCategoryChip, animationSpec = tween(160), label = "grid_crossfade") {
                                    EventCardCompact(
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
                                item(key = "loading_more", span = { GridItemSpan(2) }) {
                                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                        LoadMoreSkeleton()
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LoadMoreSkeleton()
                                    }
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
                                    ExpandedSearchSection(
                                        eventName = eventName,
                                        onEventNameChange = { viewModel.updateEventName(it) },
                                        onClearEventName = { viewModel.clearEventName() },
                                        eventLocation = eventLocation,
                                        onEventLocationChange = { viewModel.updateEventLocation(it) },
                                        onClearEventLocation = { viewModel.clearEventLocation() },
                                        locationSuggestions = locationSuggestions,
                                        categorySuggestions = categorySuggestions,
                                        searchHistory = searchHistory,
                                        onClearHistory = { viewModel.clearSearchHistory() },
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
                                    Text("${events.size} event ditemukan", fontFamily = PlusJakartaSansFont, fontSize = 13.sp, color = Gray500)
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
                                itemsIndexed(items = events, key = { _, event -> event.id }, contentType = { _, _ -> "event_list" }) { index, event ->
                                    Crossfade(targetState = selectedCategoryChip, animationSpec = tween(160), label = "list_crossfade") {
                                        StaggeredCardItem(index = index) {
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
                            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
                        }
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
private fun StaggeredCardItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index.coerceAtMost(8) * 24L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
    ) {
        content()
    }
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
