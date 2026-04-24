package com.app.lokacara.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.R
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.EventCard
import com.app.lokacara.ui.components.BottomNavbar // Pastikan importnya sesuai
import com.app.lokacara.ui.theme.* // Import warna & font

@Composable
fun HomeScreen() {
    var selectedLocation by remember { mutableStateOf("Solo") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val events = listOf(
        Event("Seminar Ai di Kota Surakarta", "Acara ini dibuat untuk memenuhi tugas mata kuliah...", "25 April 2026", "Pura Mangkunegaran", "Gratis", R.drawable.seminar),
        Event("Sound of Solo Festival", "Konser musik tahunan yang menghadirkan musisi papan atas...", "2 Mei 2026", "Benteng Vastenburg", "Rp 50.000", R.drawable.seminar_2),
        Event("Fullstack Workshop 2026", "Belajar membangun aplikasi modern dari zero ke hero...", "10 Mei 2026", "Solo Techno Park", "Gratis", R.drawable.seminar_3)
    )

    val filteredEvents = if (selectedCategory == "Semua") events else events.take(2)

    Box(modifier = Modifier.fillMaxSize().background(Gray100)) { // Pakai Gray100 untuk background
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item { HomeHeader() }
                item { PopularEventSection() }
                item {
                    NearbyEventsHeader(
                        currentLocation = selectedLocation,
                        selectedCategory = selectedCategory,
                        onLocationChange = { selectedLocation = it },
                        onCategoryChange = { selectedCategory = it }
                    )
                }
                items(filteredEvents) { event ->
                    EventCard(event)
                }
                item { Spacer(modifier = Modifier.height(110.dp)) }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            BottomNavbar()
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_lokacara),
            contentDescription = "Logo",
            modifier = Modifier.height(34.dp)
        )
        Row {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Secondary500, modifier = Modifier.size(26.dp)) // Pakai Secondary500
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = Secondary500, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun PopularEventSection() {
    val popularEvents = listOf(
        Event("Yogyakarta Concert", "Rabu 14 April 2026 | 13.00", "Yogyakarta Arena", "", "", R.drawable.candi),
        Event("Solo Art Exhibition", "Sabtu 18 April 2026 | 10.00", "Taman Budaya Surakarta", "", "", R.drawable.seminar)
    )

    Column {
        Text(
            text = "Event Populer",
            style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Gray900), // Pakai Gray900
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(popularEvents) { event ->
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = event.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Black.copy(0.3f), Color.Transparent, Color.Black.copy(0.5f)), startY = 0f)
                    ))

                    Text(
                        text = event.title, color = Color.White,
                        style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                    )

                    Column(
                        modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(text = event.description, color = Color.White, style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 11.sp))
                        Text(text = event.date, color = Color.White, style = TextStyle(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyEventsHeader(
    currentLocation: String, selectedCategory: String, onLocationChange: (String) -> Unit, onCategoryChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val locations = listOf("Solo", "Yogyakarta", "Semarang", "Jakarta")
    val categories = listOf("Semua", "Musik", "Teknologi", "Anime", "Hobi")

    Column(modifier = Modifier.padding(top = 28.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Event Terdekat di",
                style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Gray900) // Pakai Gray900
            )
            Spacer(modifier = Modifier.width(6.dp))

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expanded = true }
                ) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Secondary500, modifier = Modifier.size(24.dp)) // Pakai Secondary500
                    Text(text = currentLocation, style = TextStyle(fontFamily = PlusJakartaSansFont, color = Secondary500, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    locations.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(location, fontFamily = PlusJakartaSansFont) },
                            onClick = { onLocationChange(location); expanded = false }
                        )
                    }
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Surface(
                    color = if (isSelected) Secondary500 else Gray200, // Pakai Secondary500 & Gray200
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = if (isSelected) 6.dp else 0.dp,
                    modifier = Modifier.clickable { onCategoryChange(category) }
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        color = if (isSelected) Color.White else Gray900, // Pakai Gray900
                        style = TextStyle(fontFamily = PlusJakartaSansFont, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LokacaraMobileTheme {
        HomeScreen()
    }
}