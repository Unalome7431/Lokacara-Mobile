package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.EmptyEventState
import com.app.lokacara.ui.components.EventCard
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.components.ProfileSubpageSummaryCard
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.navigation.navigateToCreateEvent
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.PlusJakartaSansFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.Secondary100
import com.app.lokacara.ui.theme.Secondary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SemanticErrorLight
import com.app.lokacara.ui.theme.SemanticSuccessBase
import com.app.lokacara.ui.theme.SemanticSuccessLight
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun MyEventsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val myEvents by viewModel.myEvents.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val cancellingEventId by viewModel.cancellingEventId.collectAsStateWithLifecycle()
    var eventToCancel by remember { mutableStateOf<Event?>(null) }

    ProfilePageScaffold(title = "Event Saya", onBack = { navController.navigateBackOrHome() }) {
        eventToCancel?.let { event ->
            AlertDialog(
                onDismissRequest = { eventToCancel = null },
                title = {
                    Text(
                        text = "Batalkan Event",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                },
                text = {
                    Text(
                        text = "Event \"${event.title}\" akan dibatalkan dan peserta akan menerima pemberitahuan.",
                        fontFamily = PlusJakartaSansFont,
                        color = Gray600
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.cancelMyEvent(event.id)
                            eventToCancel = null
                        }
                    ) {
                        Text("Batalkan Event", color = SemanticErrorBase, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { eventToCancel = null }) {
                        Text("Kembali", color = Gray500)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(22.dp)
            )
        }

        if (isLoading && myEvents.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary500)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray50),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        ProfileSubpageSummaryCard(
                            title = "Event Saya",
                            subtitle = "Event yang kamu kelola sebagai penyelenggara.",
                            value = myEvents.size.toString(),
                            valueLabel = "event",
                            icon = Icons.Rounded.Event,
                            accentColor = Primary500
                        )
                    }
                    if (myEvents.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            EmptyEventState(
                                text = "Kamu belum memiliki event yang aktif. Mulai buat event pertamamu sekarang!",
                                onClick = { navController.navigateToCreateEvent() }
                            )
                        }
                    } else {
                        items(
                            items = myEvents,
                            key = { event: Event -> event.id },
                            contentType = { "managed_event" }
                        ) { event ->
                            EventCard(
                                event = event,
                                onClick = {
                                    navController.navigate(Screen.EventDetail.createRoute(event.id))
                                },
                                showBookmark = false,
                                trailingContent = {
                                    EventStatusBadge(status = event.status)
                                    if (event.status.isNotActiveStatus()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Secondary100)
                                            .clickable { navController.navigate(Screen.QrScan.createRoute(event.id)) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            androidx.compose.material.icons.Icons.Outlined.QrCode2,
                                            null,
                                            tint = Secondary500,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Primary100)
                                            .clickable { navController.navigate(Screen.Attendees.createRoute(event.id)) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            androidx.compose.material.icons.Icons.Outlined.Groups,
                                            null,
                                            tint = Primary500,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(SemanticSuccessLight)
                                            .clickable {
                                                navController.navigate(
                                                    Screen.CertificateManagement.createRoute(event.id)
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.WorkspacePremium,
                                            contentDescription = "Kelola sertifikat",
                                            tint = SemanticSuccessBase,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val isCancelling = cancellingEventId == event.id
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCancelling) Gray50 else SemanticErrorLight
                                            )
                                            .clickable(
                                                enabled = !isCancelling && !event.status.isNotActiveStatus()
                                            ) { eventToCancel = event },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCancelling) {
                                            CircularProgressIndicator(
                                                color = SemanticErrorBase,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Icon(
                                                Icons.Outlined.EventBusy,
                                                null,
                                                tint = if (event.status.isNotActiveStatus()) Gray500 else SemanticErrorBase,
                                                modifier = Modifier.size(14.dp)
                                            )
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
}

@Composable
private fun EventStatusBadge(status: String) {
    if (!status.isNotActiveStatus()) return

    val label = when (status.lowercase()) {
        "cancelled" -> "Dibatalkan"
        "banned" -> "Diblokir"
        else -> status.replaceFirstChar { it.uppercase() }
    }
    val color = if (status.equals("banned", ignoreCase = true)) SemanticErrorBase else SemanticErrorBase
    val background = if (status.equals("banned", ignoreCase = true)) SemanticErrorLight else SemanticErrorLight

    Text(
        text = label,
        fontFamily = PlusJakartaSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private fun String.isNotActiveStatus(): Boolean {
    return !equals("active", ignoreCase = true) && isNotBlank()
}

@Preview(showBackground = true)
@Composable
fun MyEventsScreenPreview() {
    LokacaraMobileTheme {
        MyEventsScreen(navController = rememberNavController())
    }
}
