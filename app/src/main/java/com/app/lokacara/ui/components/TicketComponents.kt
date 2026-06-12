package com.app.lokacara.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.app.lokacara.R
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.ui.theme.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

@Composable
fun BigTicketCard(
    title: String,
    date: String,
    time: String,
    location: String,
    uniqueCode: String,
    qrData: String = uniqueCode,
    userName: String,
    onQrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Primary700, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
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

        Box(modifier = Modifier.fillMaxWidth().height(20.dp).background(Primary700)) {
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Secondary500, shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onQrClick() },
                    contentAlignment = Alignment.Center
                ) {
                    QrCodeImage(
                        data = qrData.ifEmpty { uniqueCode.ifEmpty { "lokacara" } },
                        modifier = Modifier.fillMaxSize().padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.tickets_nama), fontSize = 12.sp, color = Secondary800, fontFamily = PlusJakartaSansFont)
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(14.dp), tint = Gray600)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${event.date} ${event.time}", fontSize = 12.sp, color = Gray600)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(14.dp), tint = Gray600)
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
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, "Detail", modifier = Modifier.size(16.dp), tint = Gray600)
        }
    }
}

@Composable
fun HistoryDetailDialog(
    event: HistoryEvent,
    onDismiss: () -> Unit,
    onDownload: () -> Unit = {},
    isDownloaded: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp)) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.title, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier
                            .background(if (isDownloaded) Secondary500 else Primary500, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = if (isDownloaded) "Sudah diunduh" else "Unduh sertifikat",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, "Tanggal", modifier = Modifier.size(14.dp), tint = Gray600)
                            Text(" ${event.date} ${event.time}", fontSize = 12.sp, color = Gray600)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, "Lokasi", modifier = Modifier.size(14.dp), tint = Gray600)
                            Text(" ${event.location}", fontSize = 12.sp, color = Gray600)
                        }
                    }
                    Box(modifier = Modifier.background(Secondary500, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text(event.category, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QrCodeDialog(qrData: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.size(300.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                QrCodeImage(
                    data = qrData.ifEmpty { "lokacara" },
                    modifier = Modifier.fillMaxSize().padding(32.dp)
                )
            }
        }
    }
}

@Composable
private fun QrCodeImage(data: String, modifier: Modifier = Modifier) {
    val bitmap = remember(data) { createQrBitmap(data) }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR Code",
        modifier = modifier
    )
}

private fun createQrBitmap(data: String, size: Int = 512): Bitmap {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
    )
    val matrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints)
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        val offset = y * size
        for (x in 0 until size) {
            pixels[offset + x] = if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, size, 0, 0, size, size)
    }
}
