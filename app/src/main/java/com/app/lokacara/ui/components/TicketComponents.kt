package com.app.lokacara.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.lokacara.ui.theme.*
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.model.HistoryEvent



@Composable
fun BigTicketCard(
    title: String,
    date: String,
    time: String,
    location: String,
    uniqueCode: String,
    userName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Top Blue Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Primary700,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TicketInfoItem("Tanggal", date, Color.White)
                    TicketInfoItem("Jam", time, Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TicketInfoItem("Tempat", location, Color.White)
                    TicketInfoItem("Kode Unik", uniqueCode, Color.White)
                }
            }
        }

        // Dashed Line Separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Primary700)
        ) {
            Box(modifier = Modifier.align(Alignment.CenterStart).offset(x = (-10).dp).size(20.dp).background(Color.White, CircleShape))
            Box(modifier = Modifier.align(Alignment.CenterEnd).offset(x = 10.dp).size(20.dp).background(Color.White, CircleShape))
            Canvas(modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 20.dp)) {
                drawLine(
                    color = Color.White,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            }
        }

        // Bottom Yellow Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Secondary500,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("QR YA", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Nama", fontSize = 12.sp, color = Secondary800, fontFamily = PlusJakartaSansFont)
                    Text(userName, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFont)
                }
            }
        }
    }
}

@Composable
fun TicketInfoItem(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.7f), fontFamily = PlusJakartaSansFont)
        Text(value, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFont)
    }
}

@Composable
fun SmallUpcomingEventCard(event: UpcomingEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Image(
                painter = painterResource(id = event.imageRes),
                contentDescription = event.title,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)), // Biar sudutnya tetap melengkung
                contentScale = ContentScale.Crop // Biar gambar proporsional dan tidak gepeng
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(event.title, fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Gray900)
                Text("${event.date} | ${event.time}", fontSize = 12.sp, color = Gray500, modifier = Modifier.padding(vertical = 4.dp))
                Text(event.type, fontSize = 12.sp, color = Secondary500, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HistoryItemCard(event: HistoryEvent, onClick: () -> Unit) {
    val bgColor = if (event.isBlueBg) Primary100 else Secondary100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gray900)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = Gray600)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${event.date}  ${event.time}", fontSize = 12.sp, color = Gray600)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Gray600)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.location, fontSize = 12.sp, color = Gray600)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(if (event.isBlueBg) Secondary500 else Primary500, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(event.category, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Detail", modifier = Modifier.size(16.dp), tint = Gray600)
        }
    }
}

@Composable
fun HistoryDetailDialog(event: HistoryEvent, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Image(
                    painter = painterResource(id = event.imageRes),
                    contentDescription = "Sertifikat",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Gray900,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Primary500, CircleShape)
                            .clickable { /* Handle Download */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = Gray600)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${event.date}  ${event.time}", fontSize = 12.sp, color = Gray600)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Gray600)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(event.location, fontSize = 12.sp, color = Gray600)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Secondary500, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(event.category, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}