package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.R
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.BottomNavbar
import com.app.lokacara.ui.components.EventCard
import com.app.lokacara.ui.theme.*
import androidx.compose.foundation.Image

@Composable
fun ExploreScreen() {
    // State untuk mode pencarian
    var isSearchExpanded by remember { mutableStateOf(false) }

    // State untuk nilai input pencarian
    var eventName by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var eventCategory by remember { mutableStateOf("") }

    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    // Data event dummy
    val events = listOf(
        Event("Seminar Ai di Kota Surakarta", "Acara ini dibuat untuk memenuhi tugas mata kuliah...", "25 April 2026", "Pura Mangkunegaran", "Gratis", R.drawable.seminar),
        Event("Sound of Solo Festival", "Konser musik tahunan...", "2 Mei 2026", "Benteng Vastenburg", "Rp 50.000", R.drawable.seminar_2),
        Event("Fullstack Workshop 2026", "Belajar membangun aplikasi modern...", "10 Mei 2026", "Solo Techno Park", "Gratis", R.drawable.seminar_3)
    )

    Box(modifier = Modifier.fillMaxSize().background(Gray100)) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item { ExploreHeader() }

                item {
                    // Logika Switch Tampilan Pencarian
                    if (isSearchExpanded) {
                        ExpandedSearchSection(
                            eventName = eventName,
                            onEventNameChange = { eventName = it },
                            eventLocation = eventLocation,
                            onEventLocationChange = { eventLocation = it },
                            eventCategory = eventCategory,
                            onEventCategoryChange = { eventCategory = it },
                            onSearchSubmit = { isSearchExpanded = false } // Tutup saat tombol search diklik (Opsional)
                        )
                    } else {
                        CollapsedSearchBar(
                            onClick = { isSearchExpanded = true } // Buka mode pencarian
                        )
                    }
                }

                // Sembunyikan rekomendasi jika sedang fokus mencari (agar bersih seperti di gambar)
                if (!isSearchExpanded) {
                    item {
                        Text(
                            text = "Lagi Ramai🔥",
                            style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Primary500),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }
                    item {
                        ExploreCategories(
                            selectedCategory = selectedCategoryFilter,
                            onCategorySelected = { selectedCategoryFilter = it }
                        )
                    }
                    items(events) { event ->
                        EventCard(event = event)
                    }
                }

                item { Spacer(modifier = Modifier.height(110.dp)) }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
            BottomNavbar()
        }
    }
}

@Composable
private fun ExploreHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(id = R.drawable.logo_lokacara), contentDescription = "Logo", modifier = Modifier.height(34.dp))
        Row {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Secondary500, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = Secondary500, modifier = Modifier.size(26.dp))
        }
    }
}

// Komponen Search Bar saat belum di-klik (Bentuknya dibuat mirip TextField tapi berupa Box Button agar tidak langsung memunculkan keyboard)
@Composable
private fun CollapsedSearchBar(onClick: () -> Unit) {
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
            Icon(Icons.Outlined.Search, contentDescription = "Cari", tint = Primary500)
        }
    }
}

@Composable
private fun ExpandedSearchSection(
    eventName: String, onEventNameChange: (String) -> Unit,
    eventLocation: String, onEventLocationChange: (String) -> Unit,
    eventCategory: String, onEventCategoryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit
) {
    // Data Dummy Saran Autocomplete
    val locationSuggestions = listOf("Surabaya", "Surakarta", "Jakarta", "Semarang")
    val categorySuggestions = listOf("Workshop", "Wanita", "Webinar", "Anime", "Musik")

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = eventName,
                onValueChange = onEventNameChange,
                placeholder = { Text("Nama Event", style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Gray500)) },
                modifier = Modifier.weight(1f).height(56.dp), // Disamakan tingginya
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Secondary100, unfocusedContainerColor = Secondary100,
                    focusedBorderColor = Primary500, unfocusedBorderColor = Primary500
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Tombol Cari Kotak
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(1.dp, Primary500, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .clickable { onSearchSubmit() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Search, contentDescription = "Cari", tint = Primary500)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Lokasi Event (Dengan Autocomplete Dropdown)
        AutocompleteField(
            value = eventLocation,
            onValueChange = onEventLocationChange,
            placeholder = "Lokasi Event",
            icon = Icons.Outlined.LocationOn,
            suggestions = locationSuggestions
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Kategori Event (Dengan Autocomplete Dropdown)
        AutocompleteField(
            value = eventCategory,
            onValueChange = onEventCategoryChange,
            placeholder = "Kategori",
            icon = Icons.Outlined.FormatListBulleted, // Ikon List
            suggestions = categorySuggestions
        )
    }
}

// Komponen Reusable untuk Autocomplete Dropdown
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    suggestions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    // Filter saran berdasarkan input pengguna
    val filteredSuggestions = suggestions.filter { it.contains(value, ignoreCase = true) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                // Buka dropdown otomatis jika ada input yang cocok
                expanded = it.isNotEmpty() && filteredSuggestions.isNotEmpty()
            },
            placeholder = { Text(placeholder, style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Gray500)) },
            trailingIcon = { Icon(icon, contentDescription = null, tint = Primary500) },
            modifier = Modifier.fillMaxWidth().menuAnchor(), // menuAnchor() wajib untuk trigger dropdown
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Secondary100,
                unfocusedContainerColor = Secondary100,
                focusedBorderColor = Primary500,
                unfocusedBorderColor = Primary500,
                cursorColor = Primary500
            ),
            singleLine = true
        )

        // Dropdown Menu yang melayang
        if (filteredSuggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Secondary100) // Warna background kuning muda
                    .border(1.dp, Primary500, RoundedCornerShape(8.dp)) // Border biru
            ) {
                filteredSuggestions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption, style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Gray900)) },
                        onClick = {
                            onValueChange(selectionOption) // Set teks ke pilihan
                            expanded = false // Tutup dropdown
                        },
                        leadingIcon = {
                            Icon(icon, contentDescription = null, tint = Gray900, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }
    }
}

// (Kode ExploreCategories dan CategoryChip di bawah ini sama seperti sebelumnya, tidak dirubah)
@Composable
private fun ExploreCategories(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categoriesRow1 = listOf("Semua", "Musik", "Teknologi", "Anime", "Hobi")
    val categoriesRow2 = listOf("Semua", "Musik", "Teknologi", "Anime")
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            items(categoriesRow1) { category -> CategoryChip(category, selectedCategory == category) { onCategorySelected(category) } }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categoriesRow2) { category -> CategoryChip(category, selectedCategory == category) { onCategorySelected(category) } }
        }
    }
}

@Composable
private fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(color = if (isSelected) Secondary500 else Gray200, shape = RoundedCornerShape(100.dp), modifier = Modifier.clickable { onClick() }) {
        Text(text = text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = if (isSelected) Color.White else Gray900, style = TextStyle(fontFamily = PlusJakartaSansFont, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExploreScreenPreview() {
    LokacaraMobileTheme {
        ExploreScreen()
    }
}