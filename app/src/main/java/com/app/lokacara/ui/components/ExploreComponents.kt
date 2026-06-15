package com.app.lokacara.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.navigation.NavController
import com.app.lokacara.R
import com.app.lokacara.ui.components.SnackbarManager
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.SortOption
import com.app.lokacara.viewmodel.DateFilter
import com.app.lokacara.viewmodel.PriceFilter
import com.app.lokacara.viewmodel.ErrorType

@Composable
fun ExploreHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Temukan Event",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Gray900
            )
            Text(
                "Cari berbagai event menarik di sekitarmu",
                fontFamily = PlusJakartaSansFont,
                fontSize = 13.sp,
                color = Gray500
            )
        }
    }
}

@Composable
fun CollapsedSearchBar(onClick: () -> Unit, onFilterClick: () -> Unit = {}, activeFilterCount: Int = 0) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(54.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(100.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray100)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Search, null, tint = Gray400, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Cari event impianmu...", style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Gray400))
            }
            
            Surface(
                onClick = onFilterClick,
                shape = CircleShape,
                color = Primary500.copy(alpha = 0.08f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Tune, contentDescription = "Filter", tint = Primary500, modifier = Modifier.size(18.dp))
                    if (activeFilterCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(14.dp)
                                .offset(x = 2.dp, y = (-2).dp)
                                .background(SemanticErrorBase, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$activeFilterCount",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PlusJakartaSansFont
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandedSearchSection(
    eventName: String, onEventNameChange: (String) -> Unit, onClearEventName: () -> Unit,
    eventLocation: String, onEventLocationChange: (String) -> Unit, onClearEventLocation: () -> Unit,
    locationSuggestions: List<String>, categorySuggestions: List<String>,
    searchHistory: List<String> = emptyList(),
    onClearHistory: () -> Unit = {},
    onSearchSubmit: () -> Unit,
    onFilterClick: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LokacaraTextField(
                value = eventName,
                onValueChange = onEventNameChange,
                placeholder = "Apa yang ingin kamu cari?",
                label = "Nama Event",
                isOutlined = true,
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White,
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Primary500) },
                trailingIcon = if (eventName.isNotEmpty()) {
                    {
                        IconButton(onClick = onClearEventName) {
                            Icon(Icons.Filled.Close, "Hapus", tint = Gray400, modifier = Modifier.size(18.dp))
                        }
                    }
                } else null
            )

            SearchAutocompleteField(
                value = eventLocation,
                onValueChange = onEventLocationChange,
                onClear = onClearEventLocation,
                placeholder = "Semua lokasi",
                label = "Lokasi",
                icon = Icons.Outlined.LocationOn,
                suggestions = locationSuggestions,
                focusManager = focusManager
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gray200)
                ) {
                    Text("Batal", fontFamily = PlusJakartaSansFont, color = Gray600, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSearchSubmit,
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                ) {
                    Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cari Sekarang", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String,
    label: String,
    icon: ImageVector,
    suggestions: List<String>,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value) { suggestions.filter { it.contains(value, true) } }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Gray700
        )
        
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(it); expanded = it.isNotEmpty() && filtered.isNotEmpty() },
                placeholder = { Text(placeholder, fontSize = 12.sp, color = Gray400) },
                textStyle = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Gray900),
                leadingIcon = { Icon(icon, null, tint = Primary500, modifier = Modifier.size(20.dp)) },
                trailingIcon = if (value.isNotEmpty()) {
                    {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Filled.Close, "Hapus", tint = Gray400, modifier = Modifier.size(18.dp))
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary500,
                    unfocusedBorderColor = Gray200,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            if (expanded && filtered.isNotEmpty()) {
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                    filtered.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Gray900, fontFamily = PlusJakartaSansFont) },
                            onClick = { onValueChange(option); expanded = false; focusManager.clearFocus() },
                            leadingIcon = { Icon(icon, null, tint = Gray500, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreCategories(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    allCategories: List<String>
) {
    val categories = listOf("Semua") + allCategories
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        items(categories) { cat ->
            CategoryChip(cat, selectedCategory == cat) { onCategorySelected(cat) }
        }
    }
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val background by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) Primary500 else Color.White,
        label = "chip_bg"
    )
    val contentColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) Color.White else Gray700,
        label = "chip_fg"
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable { onClick() },
        color = background,
        shape = RoundedCornerShape(100.dp),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Gray100) else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = contentColor,
            style = TextStyle(
                fontFamily = PlusJakartaSansFont,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
fun SortDropdown(
    selected: SortOption,
    onOptionSelected: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = selected.label

        Box {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = Gray100,
                modifier = Modifier.clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.FilterList, null, tint = Gray600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text, fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray700, fontWeight = FontWeight.Medium)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(option.label, fontFamily = PlusJakartaSansFont, color = if (option == selected) Primary500 else Gray900)
                            }
                        },
                        onClick = { onOptionSelected(option); expanded = false },
                        leadingIcon = {
                            if (option == selected) {
                                Icon(Icons.Outlined.FilterList, null, tint = Primary500, modifier = Modifier.size(18.dp))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreShimmer() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        repeat(4) {
            ExploreShimmerCard()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ExploreShimmerCard() {
    val brush = shimmerBrush()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(11.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(11.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
    }
}

@Composable
fun DateFilterChips(
    selected: DateFilter,
    onSelected: (DateFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        items(DateFilter.entries) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = {
                    Text(
                        filter.label,
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 11.sp,
                        fontWeight = if (selected == filter) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected == filter) Color.White else Gray700
                    )
                },
                shape = RoundedCornerShape(100.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary500,
                    containerColor = Gray100
                )
            )
        }
    }
}

@Composable
fun PriceFilterChips(
    selected: PriceFilter,
    onSelected: (PriceFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        items(PriceFilter.entries) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = {
                    Text(
                        filter.label,
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 11.sp,
                        fontWeight = if (selected == filter) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected == filter) Color.White else Gray700
                    )
                },
                shape = RoundedCornerShape(100.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Secondary500,
                    containerColor = Gray100
                )
            )
        }
    }
}

@Composable
fun EmptyStateView(
    title: String? = null,
    subtitle: String? = null,
    hasActiveFilter: Boolean = false,
    onResetFilters: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.SentimentDissatisfied,
            "Tidak Ditemukan",
            tint = Gray300,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            title ?: if (hasActiveFilter) "Event tidak ditemukan" else "Belum ada event",
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Gray600
        )
        Text(
            subtitle ?: if (hasActiveFilter) "Coba gunakan kata kunci atau filter lain" else "Event akan muncul di sini",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp),
            color = Gray400,
            fontSize = 14.sp,
            fontFamily = PlusJakartaSansFont
        )
        if (hasActiveFilter) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onResetFilters) {
                Icon(Icons.Outlined.Clear, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Filter", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sortOption: SortOption,
    onSortChange: (SortOption) -> Unit,
    priceFilter: PriceFilter,
    onPriceChange: (PriceFilter) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 3 }
            ) {
                Text(
                    "Filter Pencarian",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Gray900
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 3 }
            ) {
                Column {
                    Text(
                        "Urutkan",
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Gray700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SortOption.entries.forEach { option ->
                            FilterChip(
                                selected = sortOption == option,
                                onClick = { onSortChange(option) },
                                label = {
                                    Text(
                                        option.label,
                                        fontFamily = PlusJakartaSansFont,
                                        fontSize = 11.sp,
                                        fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Medium,
                                        color = if (sortOption == option) Color.White else Gray700
                                    )
                                },
                                shape = RoundedCornerShape(100.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary500,
                                    containerColor = Gray100
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }
            ) {
                Column {
                    Text(
                        "Harga",
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Gray700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PriceFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = priceFilter == filter,
                                onClick = { onPriceChange(filter) },
                                label = {
                                    Text(
                                        filter.label,
                                        fontFamily = PlusJakartaSansFont,
                                        fontSize = 11.sp,
                                        fontWeight = if (priceFilter == filter) FontWeight.Bold else FontWeight.Medium,
                                        color = if (priceFilter == filter) Color.White else Gray700
                                    )
                                },
                                shape = RoundedCornerShape(100.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Secondary500,
                                    containerColor = Gray100
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onReset) {
                        Text(
                            "Reset Filter",
                            fontFamily = PlusJakartaSansFont,
                            fontWeight = FontWeight.Bold,
                            color = SemanticErrorBase
                        )
                    }
                    Button(
                        onClick = { onDismiss(); SnackbarManager.show("Filter diterapkan") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                    ) {
                        Text("Terapkan", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: (() -> Unit)? = null,
    errorType: ErrorType? = null
) {
    val title = when (errorType) {
        ErrorType.NETWORK -> "Gangguan Jaringan"
        ErrorType.SERVER -> "Gangguan Server"
        else -> null
    }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val icon = when (errorType) {
            ErrorType.NETWORK -> Icons.Outlined.SignalWifiOff
            ErrorType.SERVER -> Icons.Outlined.CloudOff
            else -> Icons.Outlined.ErrorOutline
        }
        Icon(icon, "Error", tint = SemanticErrorBase, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        if (title != null) {
            Text(title, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Gray600)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(message, fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Gray500)
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary500)
            ) {
                Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
