package com.app.lokacara.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.model.Event
import com.app.lokacara.ui.theme.* // Import warna & font

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
                    color = Primary500, // Pakai Primary500
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
                    color = Gray500, // Pakai Gray500
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null, tint = Secondary500, modifier = Modifier.size(16.dp)) // Pakai Secondary500
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.price,
                            color = Secondary500, // Pakai Secondary500
                            style = TextStyle(
                                fontFamily = PlusJakartaSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Gray900, modifier = Modifier.size(20.dp)) // Pakai Gray900
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, contentDescription = null, tint = Gray600, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Gray600,
            style = TextStyle(
                fontFamily = PlusJakartaSansFont,
                fontSize = 12.sp
            )
        )
    }
}