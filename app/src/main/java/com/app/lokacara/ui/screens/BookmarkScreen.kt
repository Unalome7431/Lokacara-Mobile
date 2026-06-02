package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.components.EventCard
import com.app.lokacara.ui.navigation.NavigationActions
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.BookmarkViewModel

@Composable
fun BookmarkScreen(
    navActions: NavigationActions,
    viewModel: BookmarkViewModel = hiltViewModel()
) {
    val savedEvents by viewModel.savedEvents.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color.White).systemBarsPadding()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 30.dp)
        ) {
            IconButton(
                onClick = { navActions.goBack() },
                modifier = Modifier.align(Alignment.CenterStart).size(28.dp)
            ) {
                Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Kembali", tint = Gray900)
            }

            Text(
                text = "Event Tersimpan",
                modifier = Modifier.align(Alignment.Center),
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Gray900
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(
                items = savedEvents,
                key = { it.id }
            ) { event ->
                EventCard(
                    event = event,
                    onBookmarkClick = {
                        viewModel.toggleBookmark(event.id)
                    },
                    onClick = {
                        navActions.navigateTo(Screen.EventDetail.route)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkScreenPreview() {
    LokacaraMobileTheme {
        val dummyNavController = rememberNavController()
        BookmarkScreen(navActions = NavigationActions(
            navigateTo = { dummyNavController.navigate(it) },
            goBack = { dummyNavController.popBackStack() }
        ))
    }
}