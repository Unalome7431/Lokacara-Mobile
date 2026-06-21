package com.app.lokacara.ui.screens
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.lokacara.model.NotificationItem
import com.app.lokacara.notifications.NotificationRouteMapper
import com.app.lokacara.notifications.NotificationTarget
import com.app.lokacara.ui.components.NotificationCard
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.NotificationViewModel
import androidx.compose.ui.res.stringResource
import com.app.lokacara.R
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val notifications by viewModel.filteredNotifications.collectAsStateWithLifecycle(initialValue = emptyList())
    val groupedNotifications = remember(notifications) { notifications.groupBy { it.dateGroup } }
    val tabs = listOf(
        stringResource(R.string.tab_notifications_activity),
        stringResource(R.string.tab_notifications_info)
    )

    Column(modifier = Modifier.fillMaxSize().background(SvgBackground).systemBarsPadding()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 24.dp)
        ) {

            IconButton(
                onClick = { navController.navigateBackOrHome() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Kembali",
                    tint = Gray900,
                    modifier = Modifier.size(22.dp)
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
        Text(
            text = "Aktivitas dan informasi dari event yang kamu ikuti.",
            fontFamily = PlusJakartaSansFont,
            fontSize = 13.sp,
            color = Gray500,
            modifier = Modifier.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 16.dp)
        )
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

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada notifikasi",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Gray900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notifikasi event, tiket, dan informasi akun akan muncul di sini.",
                            fontFamily = PlusJakartaSansFont,
                            fontSize = 13.sp,
                            color = Gray500
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedNotifications.forEach { (dateGroup, items) ->
                    item(key = "date_$dateGroup", contentType = "date_header") {
                        Text(
                            dateGroup,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Gray900,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(
                        items = items,
                        key = { notification -> notification.id },
                        contentType = { "notification" }
                    ) { notification ->
                        val route = notificationTargetRoute(notification)
                        NotificationCard(
                            notification = notification,
                            onClick = route?.let {
                                {
                                    navController.navigate(it) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun notificationTargetRoute(notification: NotificationItem): String? {
    val hasTargetMetadata = !notification.target.isNullOrBlank() || !notification.category.isNullOrBlank()
    if (!hasTargetMetadata) return null

    val target = NotificationTarget.from(notification.target, notification.category.orEmpty())
    val route = NotificationRouteMapper.routeFor(target, notification.eventId)
    return route.takeUnless { it == Screen.Notification.route }
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
