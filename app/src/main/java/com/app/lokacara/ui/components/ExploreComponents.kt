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
            Text("Mencari nama, lokasi, kategori event", style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray500))
            Icon(Icons.Outlined.Search, null, tint = Primary500)
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
        Text("Lagi Ramai🔥", style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Primary500))

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Outlined.FilterList, null, tint = Gray900)
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
                placeholder = { Text("Nama Event", style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray500)) },
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
                Icon(Icons.Outlined.Search, null, tint = Primary500)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AutocompleteField(eventLocation, onEventLocationChange, "Lokasi Event", Icons.Outlined.LocationOn, locationSuggestions, focusManager, textFieldColors)
        Spacer(modifier = Modifier.height(16.dp))
        AutocompleteField(eventCategory, onEventCategoryChange, "Kategori", Icons.Outlined.FormatListBulleted, categorySuggestions, focusManager, textFieldColors)
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
            trailingIcon = { Icon(icon, null, tint = Primary500) },
            modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(8.dp),
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
    val row1 = listOf("Semua", "Musik", "Teknologi", "Anime", "Hobi")
    val row2 = listOf("Olahraga", "Bisnis", "Seni", "Webinar")
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
fun EmptyStateView() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.SentimentDissatisfied, null, tint = Gray400, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Yahh.. Event tidak ditemukan", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Gray600)
        Text("Coba gunakan kata kunci atau kategori lain", textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp), color = Gray500, fontSize = 14.sp)
    }
}