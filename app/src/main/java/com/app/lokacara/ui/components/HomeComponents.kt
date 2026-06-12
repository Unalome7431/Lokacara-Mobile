package com.app.lokacara.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val pageCount = popularEvents.size * 400
    val initialPage = pageCount / 2
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })

    val cardShape = remember { RoundedCornerShape(24.dp) }
    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color.Black.copy(0.3f), Color.Transparent, Color.Black.copy(0.5f)),
            startY = 0f
        )
    }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
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
            beyondViewportPageCount = 1
        ) { page ->
            key(page) {
                val event = popularEvents[page % popularEvents.size]

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
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

                        Text(
                            text = event.title,
                            color = Color.White,
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
}

@Composable
fun NearbyEventsHeader(
    selectedCategory: String,
    categories: List<String>,
    onCategoryChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 28.dp)) {
        Text(
            text = stringResource(R.string.home_nearby_events),
            style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories, key = { it }) { category ->
                val isSelected = category == selectedCategory
                Surface(
                    color = if (isSelected) Secondary500 else Gray100.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { onCategoryChange(category) }
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        color = if (isSelected) Color.White else Gray900,
                        style = TextStyle(fontFamily = PlusJakartaSansFont, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
                    )
                }
            }
        }
    }
}