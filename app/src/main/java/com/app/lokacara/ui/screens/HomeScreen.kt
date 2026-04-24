package com.app.lokacara.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.R
import com.app.lokacara.ui.theme.*

// Colors extracted from SVG
val SvgBackground = Color(0xFFFAF8FF)
val SvgPrimaryBlue = Color(0xFF0D59F2)
val SvgOrange = Color(0xFFFFAA00)
val SvgTextDark = Color(0xFF333333)
val SvgChipGray = Color(0xFFCCCCCC)

// Data model for Event
data class Event(
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val price: String,
    val imageRes: Int
)

@Composable
fun HomeScreen() {
    var selectedLocation by remember { mutableStateOf("Solo") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    
    val events = listOf(
        Event(
            "Seminar Ai di Kota Surakarta",
            "Acara ini dibuat untuk memenuhi tugas mata kuliah kecerdasan buatan...",
            "25 April 2026",
            "Pura Mangkunegaran",
            "Gratis",
            R.drawable.seminar
        ),
        Event(
            "Sound of Solo Festival",
            "Konser musik tahunan yang menghadirkan musisi papan atas Indonesia...",
            "2 Mei 2026",
            "Benteng Vastenburg",
            "Rp 50.000",
            R.drawable.seminar_2
        ),
        Event(
            "Fullstack Workshop 2026",
            "Belajar membangun aplikasi modern dari zero ke hero bersama mentor expert...",
            "10 Mei 2026",
            "Solo Techno Park",
            "Gratis",
            R.drawable.seminar_3
        ),
        Event(
            "Natsu Matsuri Anime Expo",
            "Festival budaya Jepang terbesar di Solo dengan kompetisi cosplay dan kuliner...",
            "15 Mei 2026",
            "De' Tjolomadoe",
            "Rp 25.000",
            R.drawable.seminar_4
        ),
        Event(
            "Personal Branding Talk",
            "Tingkatkan nilai jual diri Anda di dunia kerja profesional bersama pakar HR...",
            "20 Mei 2026",
            "UNS Tower",
            "Gratis",
            R.drawable.seminar_5
        ),
        Event(
            "E-Sports Solo Cup",
            "Turnamen Mobile Legends tingkat kota dengan total hadiah jutaan rupiah...",
            "25 Mei 2026",
            "Solo Square Mall",
            "Gratis",
            R.drawable.seminar_6
        )
    )

    // Filter events based on category (simplified for demo)
    val filteredEvents = if (selectedCategory == "Semua") events else events.take(2)

    Box(modifier = Modifier.fillMaxSize().background(SvgBackground)) {
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
        
        // Floating Bottom Nav Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            FloatingBottomNav()
        }
    }
}

@Composable
fun HomeHeader() {
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
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = SvgOrange,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = SvgOrange,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun PopularEventSection() {
    val popularEvents = listOf(
        Event("Yogyakarta Concert", "Rabu 14 April 2026 | 13.00", "Yogyakarta Arena", "", "", R.drawable.candi),
        Event("Solo Art Exhibition", "Sabtu 18 April 2026 | 10.00", "Taman Budaya Surakarta", "", "", R.drawable.seminar),
        Event("Tech Summit 2026", "Senin 20 April 2026 | 09.00", "Solo Convention Center", "", "", R.drawable.seminar_3),
        Event("Jazz Night Solo", "Jumat 24 April 2026 | 19.00", "Ngarsopuro Night Market", "", "", R.drawable.seminar_2)
    )

    Column {
        Text(
            text = "Event Populer",
            style = TextStyle(
                fontFamily = NunitoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color.Black
            ),
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
                    // Gradient Overlay as per SVG
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(0.3f), Color.Transparent, Color.Black.copy(0.5f)),
                            startY = 0f
                        )
                    ))
                    
                    Text(
                        text = event.title,
                        color = Color.White,
                        style = TextStyle(
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                    )
                    
                    Column(
                        modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = event.description,
                            color = Color.White, 
                            style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 11.sp)
                        )
                        Text(
                            text = event.date,
                            color = Color.White, 
                            style = TextStyle(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyEventsHeader(
    currentLocation: String,
    selectedCategory: String,
    onLocationChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val locations = listOf("Solo", "Yogyakarta", "Semarang", "Jakarta")

    Column(modifier = Modifier.padding(top = 28.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Event Terdekat di",
                style = TextStyle(
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expanded = true }
                ) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SvgOrange, modifier = Modifier.size(24.dp))
                    Text(
                        text = currentLocation, 
                        style = TextStyle(fontFamily = PlusJakartaSansFont, color = SvgOrange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    locations.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(location, fontFamily = PlusJakartaSansFont) },
                            onClick = {
                                onLocationChange(location)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        
        val categories = listOf("Semua", "Musik", "Teknologi", "Anime", "Hobi")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Surface(
                    color = if (isSelected) SvgOrange else SvgChipGray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = if (isSelected) 6.dp else 0.dp,
                    modifier = Modifier.clickable { onCategoryChange(category) }
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        color = if (isSelected) Color.White else SvgTextDark,
                        style = TextStyle(
                            fontFamily = PlusJakartaSansFont,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = event.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = SvgPrimaryBlue,
                    style = TextStyle(
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.description,
                    color = Color.Gray,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                DetailItem(Icons.Outlined.CalendarToday, event.date)
                DetailItem(Icons.Outlined.LocationOn, event.location)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null, tint = SvgOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.price, 
                            color = SvgOrange, 
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = SvgTextDark, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text, 
            color = Color.DarkGray, 
            style = TextStyle(
                fontFamily = PlusJakartaSansFont,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
fun FloatingBottomNav() {
    var selectedItem by remember { mutableIntStateOf(0) }
    
    Box(
        modifier = Modifier
            .width(338.dp) // SVG Width
            .height(100.dp), // Height to allow protrusion
        contentAlignment = Alignment.BottomCenter
    ) {
        // The White Pill Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(71.dp), // SVG Height
            shape = RoundedCornerShape(35.5.dp), // SVG rx
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Icon
                NavIconItem(Icons.Default.Home, selectedItem == 0) { selectedItem = 0 }
                
                // Explore Icon
                NavIconItem(Icons.Outlined.Explore, selectedItem == 1) { selectedItem = 1 }
                
                // Gap for the FAB
                Spacer(modifier = Modifier.width(64.dp))
                
                // Tickets Icon
                NavIconItem(Icons.Outlined.ConfirmationNumber, selectedItem == 2) { selectedItem = 2 }
                
                // Profile Icon
                NavIconItem(Icons.Outlined.Person, selectedItem == 3) { selectedItem = 3 }
            }
        }
        
        // The FAB (Overlapping the bar)
        Box(
            modifier = Modifier
                .size(64.dp) // SVG size
                .offset(y = (12).dp) // Lowered (turunin dikit) from -19dp
                .align(Alignment.TopCenter)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .background(SvgPrimaryBlue, CircleShape)
                .clickable { /* Action */ },
            contentAlignment = Alignment.Center
        ) {
            // White circle with blue plus (Matches the "cut-out" look of the SVG)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = SvgPrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun NavIconItem(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) SvgPrimaryBlue else Color(0xFF999999),
        modifier = Modifier
            .size(26.dp)
            .clickable { onClick() }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LokacaraMobileTheme {
        HomeScreen()
    }
}
