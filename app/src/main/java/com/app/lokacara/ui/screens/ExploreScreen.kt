package com.app.lokacara.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import com.app.lokacara.R
import com.app.lokacara.ui.components.*
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.viewmodel.ExploreViewModel

@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsState()
    val eventName by viewModel.eventName.collectAsState()
    val eventLocation by viewModel.eventLocation.collectAsState()
    val eventCategory by viewModel.eventCategory.collectAsState()
    val selectedCategoryChip by viewModel.selectedCategoryChip.collectAsState()
    val events by viewModel.filteredEvents.collectAsState()
    val locationSuggestions by viewModel.locationSuggestions.collectAsState()
    val categorySuggestions by viewModel.categorySuggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val focusManager = LocalFocusManager.current

    val allCategoryLabel = stringResource(R.string.category_all)
    val hasActiveFilter = eventName.isNotEmpty() || eventLocation.isNotEmpty() ||
            eventCategory.isNotEmpty() || selectedCategoryChip != allCategoryLabel

    BackHandler(enabled = isSearchExpanded || hasActiveFilter) {
        if (isSearchExpanded) {
            viewModel.isSearchExpanded.value = false
            focusManager.clearFocus()
        } else {
            viewModel.resetFilters()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Gray100)) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary500)
            }
            error != null -> ErrorStateView(message = error ?: "", onRetry = { viewModel.refresh() })
            else -> PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

            item { ExploreHeader() }

            item {
                if (isSearchExpanded) {
                    ExpandedSearchSection(
                        eventName = eventName,
                        onEventNameChange = { viewModel.eventName.value = it; viewModel.searchWithDebounce(it) },
                        eventLocation = eventLocation,
                        onEventLocationChange = { viewModel.eventLocation.value = it },
                        eventCategory = eventCategory,
                        onEventCategoryChange = { viewModel.eventCategory.value = it },
                        locationSuggestions = locationSuggestions,
                        categorySuggestions = categorySuggestions,
                        onSearchSubmit = {
                            viewModel.isSearchExpanded.value = false
                            focusManager.clearFocus()
                        }
                    )
                } else {
                    CollapsedSearchBar(onClick = { viewModel.isSearchExpanded.value = true })
                }
            }

            if (!isSearchExpanded) {
                item {
                    HotLabelSection(
                        selectedCategory = selectedCategoryChip,
                        onCategorySelected = { viewModel.selectedCategoryChip.value = it },
                        allCategories = categorySuggestions
                    )
                }

                item {
                    ExploreCategories(
                        selectedCategory = selectedCategoryChip,
                        onCategorySelected = { viewModel.selectedCategoryChip.value = it },
                        allCategories = categorySuggestions
                    )
                }

                if (events.isEmpty()) {
                    item { EmptyStateView() }
                } else {
                    items(items = events, key = { it.id }) { event ->
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

            item { Spacer(modifier = Modifier.height(80.dp)) }
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
