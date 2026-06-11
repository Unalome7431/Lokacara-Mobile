package com.app.lokacara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .let { mod ->
                if (onClick != null) mod.clickable { onClick() } else mod
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
                    .crossfade(true)
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
                        val bookmarkIcon = if (event.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder

                        Icon(
                            imageVector = bookmarkIcon,
                            contentDescription = "Bookmark",
                            tint = Gray900,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onBookmarkClick() }
                        )
                    }
                }
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun EventCardPreview() {
    LokacaraMobileTheme {
        EventCard(
            event = Event(
                id = 1L, title = "Test Event", description = "Desc", date = "12 Jun 2026",
                location = "Surakarta", price = "Gratis", imageUrl = null, category = "Teknologi"
            )
        )
    }
}