package com.app.lokacara.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.lokacara.model.Event
import com.app.lokacara.ui.theme.*

@Composable
fun EventCard(
    event: Event,
    onBookmarkClick: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    showBookmark: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, animationSpec = spring(dampingRatio = 0.5f), label = "scale")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .let { mod ->
                if (onClick != null) mod.clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, radius = 300.dp)
                ) { onClick() } else mod
            },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.imageUrl)
                    .size(110)
                    .build(),
                contentDescription = event.title,
                modifier = Modifier.size(110.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, color = Primary500, style = TextStyle(fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(event.description, color = Gray500, style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(vertical = 4.dp))

                DetailItem(Icons.Outlined.CalendarToday, event.date)
                DetailItem(Icons.Outlined.LocationOn, event.location)

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ConfirmationNumber, contentDescription = "Tiket", tint = Secondary500, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(event.price, color = Secondary500, style = TextStyle(fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp))
                    }

                    if (showBookmark) {
                        Crossfade(
                            targetState = event.isBookmarked,
                            animationSpec = tween(200)
                        ) { bookmarked ->
                            Icon(
                                imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = Gray900,
                                modifier = Modifier.size(24.dp).clickable { onBookmarkClick() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCardCompact(
    event: Event,
    onClick: () -> Unit = {},
    isBookmarked: Boolean = event.isBookmarked,
    onBookmarkClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, animationSpec = spring(dampingRatio = 0.5f), label = "scale")

    Surface(
        modifier = Modifier
            .width(160.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(interactionSource = interactionSource, indication = ripple(bounded = true, radius = 160.dp)) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(event.imageUrl)
                        .size(160)
                        .build(),
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = Secondary500,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = event.category,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable { onBookmarkClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Secondary500 else Gray500,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = event.title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Gray900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Gray400, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = event.location,
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 11.sp,
                        color = Gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.date,
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 11.sp,
                        color = Gray400,
                        maxLines = 1
                    )
                    Text(
                        text = event.price,
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (event.price == "Gratis") SemanticSuccessBase else Secondary500,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerSkeletonCard() {
    val shimmerColors = listOf(
        Gray200.copy(alpha = 0.6f),
        Gray100.copy(alpha = 0.3f),
        Gray200.copy(alpha = 0.6f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(translateAnim.value, translateAnim.value)
    )

    Surface(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(brush)
            )
            Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, contentDescription = text, tint = Gray600, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = Gray600, style = TextStyle(fontFamily = PlusJakartaSansFont, fontSize = 12.sp))
    }
}
