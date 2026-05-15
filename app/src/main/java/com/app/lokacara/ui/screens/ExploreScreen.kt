package com.app.lokacara.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.lokacara.ui.components.*
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.viewmodel.ExploreViewModel

@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = viewModel()
) {
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsState()
    val eventName by viewModel.eventName.collectAsState()
    val eventLocation by viewModel.eventLocation.collectAsState()
    val eventCategory by viewModel.eventCategory.collectAsState()
    val selectedCategoryChip by viewModel.selectedCategoryChip.collectAsState()
    val events by viewModel.filteredEvents.collectAsState()

    val focusManager = LocalFocusManager.current

    val hasActiveFilter = eventName.isNotEmpty() || eventLocation.isNotEmpty() ||
            eventCategory.isNotEmpty() || selectedCategoryChip != "Semua"

    BackHandler(enabled = isSearchExpanded || hasActiveFilter) {
        if (isSearchExpanded) {
            viewModel.isSearchExpanded.value = false
            focusManager.clearFocus()
        } else {
            viewModel.resetFilters()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Gray100)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            item { ExploreHeader() }

            item {
                if (isSearchExpanded) {
                    ExpandedSearchSection(
                        eventName = eventName,
                        onEventNameChange = { viewModel.eventName.value = it },
                        eventLocation = eventLocation,
                        onEventLocationChange = { viewModel.eventLocation.value = it },
                        eventCategory = eventCategory,
                        onEventCategoryChange = { viewModel.eventCategory.value = it },
                        locationSuggestions = viewModel.locationSuggestions,
                        categorySuggestions = viewModel.categorySuggestions,
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
                        allCategories = viewModel.categorySuggestions
                    )
                }

                item {
                    ExploreCategories(
                        selectedCategory = selectedCategoryChip,
                        onCategorySelected = { viewModel.selectedCategoryChip.value = it }
                    )
                }

                if (events.isEmpty()) {
                    item { EmptyStateView() }
                } else {
                    items(items = events, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onClick = {
                                navController.navigate(Screen.EventDetail.route)
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
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
