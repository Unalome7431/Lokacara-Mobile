package com.app.lokacara.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.viewmodel.BookmarkViewModel

@Composable
fun BookmarkScreen(
    navController: NavController,
    viewModel: BookmarkViewModel = hiltViewModel()
) {
    SavedEventsScreen(navController = navController, viewModel = viewModel)
}

@Preview(showBackground = true)
@Composable
fun BookmarkScreenPreview() {
    LokacaraMobileTheme {
        BookmarkScreen(navController = rememberNavController())
    }
}
