package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.lokacara.R
import com.app.lokacara.data.remote.dto.AttendeeDto
import com.app.lokacara.ui.components.shimmerBrush
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.theme.Gray200
import com.app.lokacara.ui.theme.Gray400
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray700
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.PlusJakartaSansFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.Secondary100
import com.app.lokacara.ui.theme.Secondary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SemanticErrorLight
import com.app.lokacara.ui.theme.SemanticSuccessBase
import com.app.lokacara.ui.theme.SvgBackground
import com.app.lokacara.viewmodel.AttendeesViewModel

private enum class AttendeeFilter(val label: String) {
    ALL("Semua"),
    PRESENT("Hadir"),
    PENDING("Belum")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendeesScreen(
    navController: NavController,
    eventId: Long,
    viewModel: AttendeesViewModel = hiltViewModel()
) {
    val attendees by viewModel.attendees.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val isReminderSending by viewModel.isReminderSending.collectAsState()
    val togglingIds by viewModel.togglingIds.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val error by viewModel.error.collectAsState()

    var showReminderConfirm by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(AttendeeFilter.ALL) }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= layoutInfo.totalItemsCount - 4
        }
    }

    val filteredAttendees = remember(attendees, searchQuery, filter) {
        attendees.filter { attendee ->
            val name = attendee.user?.name.orEmpty()
            val email = attendee.user?.email.orEmpty()
            val matchesSearch = searchQuery.isBlank() ||
                    name.contains(searchQuery, ignoreCase = true) ||
                    email.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filter) {
                AttendeeFilter.ALL -> true
                AttendeeFilter.PRESENT -> attendee.status == "present"
                AttendeeFilter.PENDING -> attendee.status != "present"
            }
            matchesSearch && matchesFilter
        }
    }
    val presentCount = attendees.count { it.status == "present" }
    val pendingCount = attendees.size - presentCount

    LaunchedEffect(eventId) {
        viewModel.loadAttendees(eventId)
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoadingMore && !isLoading) viewModel.loadNextPage()
    }

    if (showReminderConfirm) {
        AlertDialog(
            onDismissRequest = { showReminderConfirm = false },
            title = { Text(stringResource(R.string.attendees_send_reminder), fontFamily = NunitoFont, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.attendees_reminder_confirm), fontFamily = PlusJakartaSansFont, color = Gray600) },
            confirmButton = {
                TextButton(
                    enabled = !isReminderSending,
                    onClick = {
                        viewModel.sendReminders()
                        showReminderConfirm = false
                    }
                ) { Text(stringResource(R.string.attendees_send), color = Primary500, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showReminderConfirm = false }) { Text(stringResource(R.string.cancel), color = Gray600) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvgBackground)
            .statusBarsPadding()
    ) {
        AttendeesTopBar(
            isReminderSending = isReminderSending,
            reminderEnabled = attendees.isNotEmpty() || totalCount > 0,
            onBack = { navController.navigateBackOrHome() },
            onReminder = { showReminderConfirm = true }
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AttendeesSummary(total = totalCount.takeIf { it > 0 } ?: attendees.size, present = presentCount, pending = pendingCount)
                }
                item {
                    AttendeeControlPanel(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onClearSearch = { searchQuery = "" },
                        selectedFilter = filter,
                        onFilterChange = { filter = it }
                    )
                }

                if (error != null && attendees.isNotEmpty()) {
                    item {
                        AttendeesInlineError(message = error.orEmpty(), onRetry = { viewModel.refresh() })
                    }
                }

                when {
                    isLoading && attendees.isEmpty() -> {
                        items(6) { AttendeeSkeletonCard() }
                    }
                    error != null && attendees.isEmpty() -> {
                        item { AttendeesErrorState(message = error.orEmpty(), onRetry = { viewModel.loadAttendees(eventId) }) }
                    }
                    filteredAttendees.isEmpty() -> {
                        item {
                            AttendeesEmptyState(
                                text = if (attendees.isEmpty()) stringResource(R.string.attendees_empty) else "Peserta tidak ditemukan"
                            )
                        }
                    }
                    else -> {
                        items(filteredAttendees, key = { it.id }) { attendee ->
                            AttendeeCard(
                                attendee = attendee,
                                isToggling = attendee.id in togglingIds,
                                onToggle = { viewModel.toggleAttendance(attendee.id) }
                            )
                        }
                    }
                }

                if (isLoadingMore) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary500, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendeesTopBar(
    isReminderSending: Boolean,
    reminderEnabled: Boolean,
    onBack: () -> Unit,
    onReminder: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(62.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color.White, Primary100.copy(alpha = 0.48f), Secondary100.copy(alpha = 0.42f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.size(21.dp),
                    tint = Primary500
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.attendees_title),
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Gray900
                )
                Text(
                    text = stringResource(R.string.attendees_send_reminder),
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 11.sp,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                enabled = reminderEnabled && !isReminderSending,
                onClick = onReminder,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (reminderEnabled) Color.White.copy(alpha = 0.82f) else Gray100)
            ) {
                if (isReminderSending) {
                    CircularProgressIndicator(color = Secondary500, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.attendees_send_reminder),
                        tint = if (reminderEnabled) Secondary500 else Gray400
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendeesSummary(total: Int, present: Int, pending: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AttendeeSummaryCard("Total", total.toString(), Primary500, Modifier.weight(1f))
        AttendeeSummaryCard("Hadir", present.toString(), SemanticSuccessBase, Modifier.weight(1f))
        AttendeeSummaryCard("Belum", pending.toString(), Secondary500, Modifier.weight(1f))
    }
}

@Composable
private fun AttendeeSummaryCard(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.11f), Color.White)))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = accent)
            Text(label, fontFamily = PlusJakartaSansFont, fontSize = 11.sp, color = Gray500)
        }
    }
}

@Composable
private fun AttendeeControlPanel(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    selectedFilter: AttendeeFilter,
    onFilterChange: (AttendeeFilter) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Primary500) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Gray500, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                placeholder = {
                    Text("Cari nama atau email peserta", fontFamily = PlusJakartaSansFont, color = Gray400)
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AttendeeFilter.entries.forEach { item ->
                    FilterChip(
                        selected = selectedFilter == item,
                        onClick = { onFilterChange(item) },
                        label = {
                            Text(
                                item.label,
                                fontFamily = PlusJakartaSansFont,
                                fontWeight = if (selectedFilter == item) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary500,
                            selectedLabelColor = Color.White,
                            containerColor = Gray100,
                            labelColor = Gray600
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == item,
                            borderColor = Gray200,
                            selectedBorderColor = Primary500
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendeesInlineError(message: String, onRetry: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SemanticErrorLight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                fontFamily = PlusJakartaSansFont,
                fontSize = 12.sp,
                color = SemanticErrorBase,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry), color = SemanticErrorBase, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AttendeeCard(
    attendee: AttendeeDto,
    isToggling: Boolean,
    onToggle: () -> Unit
) {
    val isCheckedIn = attendee.status == "present"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isCheckedIn) Secondary100 else Primary100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = attendee.user?.name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = if (isCheckedIn) Secondary500 else Primary500
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attendee.user?.name ?: "Peserta",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                attendee.user?.email?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray500, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCheckedIn) Icons.Filled.CheckCircle else Icons.Outlined.Cancel,
                        contentDescription = null,
                        tint = if (isCheckedIn) SemanticSuccessBase else Gray400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isCheckedIn) stringResource(R.string.attendees_present) else stringResource(R.string.attendees_not_checked_in),
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 12.sp,
                        color = if (isCheckedIn) SemanticSuccessBase else Gray500
                    )
                }
                attendee.checked_in_at?.takeIf { it.isNotBlank() }?.let {
                    Text("Check-in: $it", fontFamily = PlusJakartaSansFont, fontSize = 11.sp, color = Gray500)
                }
            }
            IconButton(enabled = !isToggling, onClick = onToggle) {
                if (isToggling) {
                    CircularProgressIndicator(color = Primary500, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = if (isCheckedIn) Icons.Filled.CheckCircle else Icons.Outlined.Person,
                        contentDescription = if (isCheckedIn) "Ubah status" else "Check-in",
                        tint = if (isCheckedIn) Secondary500 else Primary500,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendeeSkeletonCard() {
    val brush = shimmerBrush()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(brush))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                Box(modifier = Modifier.fillMaxWidth(0.45f).height(11.dp).clip(RoundedCornerShape(6.dp)).background(brush))
            }
        }
    }
}

@Composable
private fun AttendeesEmptyState(text: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = Gray400, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = Gray700)
        }
    }
}

@Composable
private fun AttendeesErrorState(message: String, onRetry: () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, fontFamily = PlusJakartaSansFont, color = SemanticErrorBase)
            HorizontalDivider(color = Gray100, modifier = Modifier.padding(vertical = 14.dp))
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry), color = Primary500, fontWeight = FontWeight.Bold)
            }
        }
    }
}
