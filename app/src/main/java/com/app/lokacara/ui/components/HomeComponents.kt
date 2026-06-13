package com.app.lokacara.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.lokacara.R
import com.app.lokacara.model.Event
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeHeader(navController: NavController) {
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
        Row(verticalAlignment = Alignment.CenterVertically) {

            IconButton(
                onClick = { navController.navigate(Screen.Notification.route) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    tint = Secondary500,
                    modifier = Modifier.size(26.dp)
                )
            }

            IconButton(
                onClick = { navController.navigate(Screen.Bookmark.route) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = "Event Tersimpan",
                    tint = Secondary500,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun PopularEventSection(popularEvents: List<Event>, onEventClick: (Event) -> Unit = {}) {
    if (popularEvents.isEmpty()) return

    val pageCount = popularEvents.size * 10
    val initialPage = pageCount / 2
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })

    val cardShape = remember { RoundedCornerShape(24.dp) }
    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color.Black.copy(0.1f), Color.Transparent, Color.Black.copy(0.6f), Color.Black.copy(0.7f)),
            startY = 0f
        )
    }
    val context = LocalContext.current

    var paused by remember { mutableStateOf(false) }

    // Pause auto-slide when user interacts with the pager
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }
            .collect { scrolling ->
                if (scrolling) {
                    paused = true
                }
            }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(if (paused) 3000L else 4000L)
            if (paused) { paused = false; continue }
            if (pageCount > 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    Column {
        Text(
            text = stringResource(R.string.home_popular_events),
            style = TextStyle(
                fontFamily = NunitoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color.Black
            ),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
        )

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth(),
            beyondViewportPageCount = 1,
            userScrollEnabled = true
        ) { page ->
            key(page) {
                val event = popularEvents[page % popularEvents.size]

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { onEventClick(event) },
                    shape = cardShape,
                    color = Color.Transparent,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(cardShape)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(event.imageUrl)
                                .size(400)
                                .crossfade(true)
                                .build(),
                            contentDescription = event.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(modifier = Modifier.fillMaxSize().background(gradientBrush))

                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.align(Alignment.Start).widthIn(max = 200.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SvgOrange.copy(alpha = 0.9f)
                                ) {
                                    Text(
                                        text = event.category,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = PlusJakartaSansFont,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = event.title,
                                    color = Color.White,
                                    style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = event.date, color = Color.White.copy(alpha = 0.8f), style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 12.sp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Page indicator dots
        val currentPage = pagerState.currentPage % popularEvents.size
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            popularEvents.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == currentPage) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (index == currentPage) SvgPrimaryBlue else Gray300)
                )
            }
        }
    }
}

@Composable
fun NearbyEventsHeader(
    currentLocation: String = ""
) {
    Column(modifier = Modifier.padding(top = 28.dp)) {
        Text(
            text = "Event di Sekitar Anda",
            style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 24.dp, top = 4.dp)) {
            Icon(Icons.Outlined.LocationOn, contentDescription = "Lokasi", tint = Secondary500, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            val displayText = if (currentLocation.isNotBlank()) "di $currentLocation" else "di Sekitar Anda"
            Text(
                text = displayText,
                style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 14.sp, color = Gray600)
            )
        }
    }
}

@Composable
fun CategoryEventSection(
    categoryName: String,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    onSeeAll: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, animationSpec = tween(350), label = "fade")

    Column(modifier = Modifier.padding(top = 24.dp).graphicsLayer(alpha = alpha)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryName,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            TextButton(onClick = onSeeAll) {
                Text("Lihat Semua →", fontFamily = PlusJakartaSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SvgOrange)
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events, key = { it.id }) { event ->
                EventCardCompact(event = event, onClick = { onEventClick(event) })
            }
        }
        }
    }
