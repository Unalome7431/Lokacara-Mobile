package com.app.lokacara.ui.components

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.app.lokacara.R
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*

@Composable
fun ExploreHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_lokacara),
            contentDescription = "Logo",
            modifier = Modifier.height(34.dp)
        )
    }
}
@Composable
fun CollapsedSearchBar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(52.dp)
            .border(1.dp, Primary300, RoundedCornerShape(100.dp))
            .background(Color.White, RoundedCornerShape(100.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.explore_search_placeholder), style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray500))
            Icon(Icons.Outlined.Search, "Cari", tint = Primary500)
        }
    }
}

@Composable
fun HotLabelSection(selectedCategory: String, onCategorySelected: (String) -> Unit, allCategories: List<String>) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.explore_hot_label), style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Primary500))

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Outlined.FilterList, "Filter", tint = Gray900)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color.White)) {
                allCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category, color = if(selectedCategory == category) Primary500 else Gray900, fontFamily = PlusJakartaSansFont) },
                        onClick = { onCategorySelected(category); showMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandedSearchSection(
    eventName: String, onEventNameChange: (String) -> Unit,
    eventLocation: String, onEventLocationChange: (String) -> Unit,
    eventCategory: String, onEventCategoryChange: (String) -> Unit,
    locationSuggestions: List<String>, categorySuggestions: List<String>,
    onSearchSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Secondary100, unfocusedContainerColor = Secondary100,
        focusedBorderColor = Primary500, unfocusedBorderColor = Primary500,
        focusedTextColor = Gray900, unfocusedTextColor = Gray900
    )

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = eventName, onValueChange = onEventNameChange,
                placeholder = { Text(stringResource(R.string.explore_search_name), style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray500)) },
                textStyle = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Gray900),
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(8.dp), colors = textFieldColors, singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() })
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier.size(56.dp).border(1.dp, Primary500, RoundedCornerShape(8.dp)).background(Color.White, RoundedCornerShape(8.dp)).clickable { onSearchSubmit() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Search, "Cari", tint = Primary500)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AutocompleteField(eventLocation, onEventLocationChange, stringResource(R.string.explore_search_location), Icons.Outlined.LocationOn, locationSuggestions, focusManager, textFieldColors)
        Spacer(modifier = Modifier.height(16.dp))
        AutocompleteField(eventCategory, onEventCategoryChange, stringResource(R.string.explore_search_category), Icons.AutoMirrored.Outlined.FormatListBulleted, categorySuggestions, focusManager, textFieldColors)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteField(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector, suggestions: List<String>, focusManager: androidx.compose.ui.focus.FocusManager, colors: TextFieldColors) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value) { suggestions.filter { it.contains(value, true) } }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value, onValueChange = { onValueChange(it); expanded = it.isNotEmpty() && filtered.isNotEmpty() },
            placeholder = { Text(placeholder, fontSize = 12.sp, color = Gray500) },
            textStyle = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Gray900),
            trailingIcon = { Icon(icon, "Dropdown", tint = Primary500) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(8.dp),
            colors = colors, singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        if (expanded && filtered.isNotEmpty()) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Secondary100)) {
                filtered.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Gray900) },
                        onClick = { onValueChange(option); expanded = false; focusManager.clearFocus() },
                        leadingIcon = { Icon(icon, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreCategories(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val row1 = listOf(
        stringResource(R.string.category_all),
        stringResource(R.string.category_music),
        stringResource(R.string.category_technology),
        stringResource(R.string.category_anime),
        stringResource(R.string.category_hobby)
    )
    val row2 = listOf(
        stringResource(R.string.category_sports),
        stringResource(R.string.category_business),
        stringResource(R.string.category_art),
        stringResource(R.string.category_webinar)
    )
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            items(row1) { cat -> CategoryChip(cat, selectedCategory == cat) { onCategorySelected(cat) } }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(row2) { cat -> CategoryChip(cat, selectedCategory == cat) { onCategorySelected(cat) } }
        }
    }
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(color = if (isSelected) Secondary500 else Gray200, shape = RoundedCornerShape(100.dp), modifier = Modifier.clickable { onClick() }) {
        Text(text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = if (isSelected) Color.White else Gray900, style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 12.sp, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium))
    }
}

@Composable
fun EmptyStateView(
    title: String? = null,
    subtitle: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.SentimentDissatisfied, "Tidak Ditemukan", tint = Gray400, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(title ?: stringResource(R.string.empty_events_not_found), fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Gray600)
        Text(subtitle ?: stringResource(R.string.empty_events_try_other), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp), color = Gray500, fontSize = 14.sp)
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.ErrorOutline, "Error", tint = SemanticErrorBase, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Gray600)
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