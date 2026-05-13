package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.app.lokacara.R
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.EventCard
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun SavedEventsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val savedEvents by viewModel.savedEvents.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp).clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Event Tersimpan",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(20.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for navbar
        ) {
            items(savedEvents) { event ->
                EventCard(event = event)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedEventsScreenPreview() {
    LokacaraMobileTheme {
        SavedEventsScreen(navController = rememberNavController())
    }
}

