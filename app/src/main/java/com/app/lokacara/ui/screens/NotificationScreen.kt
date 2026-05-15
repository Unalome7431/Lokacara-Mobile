package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.lokacara.ui.components.NotificationCard
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.NotificationViewModel

@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val notifications by viewModel.filteredNotifications.collectAsState(initial = emptyList())
    val tabs = listOf("Aktivitas", "Informasi")

    Column(modifier = Modifier.fillMaxSize().background(Color.White).systemBarsPadding()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 24.dp)
        ) {

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Gray900
                )
            }

            Text(
                text = "Notifikasi",
                modifier = Modifier.align(Alignment.Center),
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Gray900
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            tabs.forEachIndexed { index, title ->
                Column(
                    modifier = Modifier.weight(1f).clickable { viewModel.selectedTab.value = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(title, color = if (selectedTab == index) Primary500 else Gray500, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.5f).height(3.dp).background(if (selectedTab == index) Primary500 else Color.Transparent))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val grouped = notifications.groupBy { it.dateGroup }
            grouped.forEach { (dateGroup, items) ->
                item {
                    Text(dateGroup, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(items) { notification -> NotificationCard(notification) }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationScreenPreview() {
    com.app.lokacara.ui.theme.LokacaraMobileTheme {
        NotificationScreen(
            navController = androidx.navigation.compose.rememberNavController()
        )
    }
}