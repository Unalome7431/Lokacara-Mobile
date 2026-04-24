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
    val imageRes: Int,
    val category: String // Added category for filtering
)

@Composable
fun HomeScreen() {
    var selectedLocation by remember { mutableStateOf("Solo") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showCreateScreen by remember { mutableStateOf(false) }

    if (showCreateScreen) {
        CreateEventScreen(onBack = { showCreateScreen = false })
    } else {
        val events = listOf(
            Event(
                "Seminar Ai di Kota Surakarta",
                "Acara ini dibuat untuk memenuhi tugas mata kuliah kecerdasan buatan...",
                "25 April 2026",
                "Pura Mangkunegaran",
                "Gratis",
                R.drawable.seminar,
                "Teknologi"
            ),
            Event(
                "Sound of Solo Festival",
                "Konser musik tahunan yang menghadirkan musisi papan atas Indonesia...",
                "2 Mei 2026",
                "Benteng Vastenburg",
                "Rp 50.000",
                R.drawable.seminar_2,
                "Musik"
            ),
            Event(
                "Fullstack Workshop 2026",
                "Belajar membangun aplikasi modern dari zero ke hero bersama mentor expert...",
                "10 Mei 2026",
                "Solo Techno Park",
                "Gratis",
                R.drawable.seminar_3,
                "Teknologi"
            ),
            Event(
                "Natsu Matsuri Anime Expo",
                "Festival budaya Jepang terbesar di Solo dengan kompetisi cosplay dan kuliner...",
                "15 Mei 2026",
                "De' Tjolomadoe",
                "Rp 25.000",
                R.drawable.seminar_4,
                "Anime"
            ),
            Event(
                "Personal Branding Talk",
                "Tingkatkan nilai jual diri Anda di dunia kerja profesional bersama pakar HR...",
                "20 Mei 2026",
                "UNS Tower",
                "Gratis",
                R.drawable.seminar_5,
                "Hobi"
            ),
            Event(
                "E-Sports Solo Cup",
                "Turnamen Mobile Legends tingkat kota dengan total hadiah jutaan rupiah...",
                "25 Mei 2026",
                "Solo Square Mall",
                "Gratis",
                R.drawable.seminar_6,
                "Hobi"
            )
        )

        // Filter events based on category
        val filteredEvents = if (selectedCategory == "Semua") {
            events
        } else {
            events.filter { it.category == selectedCategory }
        }

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
                FloatingBottomNav(onCreateClick = { showCreateScreen = true })
            }
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
        Event("Yogyakarta Concert", "Rabu 14 April 2026 | 13.00", "Yogyakarta Arena", "", "", R.drawable.candi, "Musik"),
        Event("Solo Art Exhibition", "Sabtu 18 April 2026 | 10.00", "Taman Budaya Surakarta", "", "", R.drawable.seminar, "Hobi"),
        Event("Tech Summit 2026", "Senin 20 April 2026 | 09.00", "Solo Convention Center", "", "", R.drawable.seminar_3, "Teknologi"),
        Event("Jazz Night Solo", "Jumat 24 April 2026 | 19.00", "Ngarsopuro Night Market", "", "", R.drawable.seminar_2, "Musik")
    )

    // Infinite looping setup
    val pageCount = Int.MAX_VALUE
    val initialPage = pageCount / 2
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    // Auto-slide effect (every 3 seconds)
    LaunchedEffect(key1 = pagerState.currentPage) {
        kotlinx.coroutines.delay(3000)
        pagerState.animateScrollToPage(pagerState.currentPage + 1)
    }

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
        
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            // Map the infinite page index back to our 0-3 list
            val event = popularEvents[page % popularEvents.size]
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                // Gradient Overlay
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
            modifier = Modifier.fillMaxWidth(),
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
fun FloatingBottomNav(onCreateClick: () -> Unit) {
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
        
        // Native FAB implementation replacing the SVGs
        Box(
            modifier = Modifier
                .size(64.dp)
                .offset(y = (12).dp)
                .align(Alignment.TopCenter)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .background(SvgPrimaryBlue, CircleShape)
                .clickable { 
                    selectedItem = 4
                    onCreateClick()
                },
            contentAlignment = Alignment.Center
        ) {
            // White circle with blue plus
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Event",
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
