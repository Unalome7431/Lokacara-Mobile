package com.app.lokacara.ui.navigation

import androidx.compose.runtime.Immutable

@Immutable
data class NavigationActions(
    val navigateTo: (String) -> Unit,
    val goBack: () -> Unit
)
