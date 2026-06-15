package com.app.lokacara.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.lokacara.R
import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.MyEventData
import com.app.lokacara.ui.theme.*
import java.io.File
import androidx.compose.material.icons.outlined.Group
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun ProfilePageScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    backgroundColor: Color = Gray50,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 18.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.White,
                            Primary100.copy(alpha = 0.34f),
                            Secondary100.copy(alpha = 0.44f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.78f), RoundedCornerShape(18.dp))
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Kembali",
                        tint = Primary500,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }

            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )

            Spacer(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(28.dp)
            )
        }

        content()
    }
}

@Composable
fun ProfileStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Gray50, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Gray900
        )
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Gray500
        )
    }
}

@Composable
fun ProfileSubpageSummaryCard(
    title: String,
    subtitle: String,
    value: String,
    valueLabel: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(accentColor.copy(alpha = 0.14f), Color.White, Color.White)
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 12.sp,
                    color = Gray500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = value,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = accentColor
                )
                Text(
                    text = valueLabel,
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 11.sp,
                    color = Gray500
                )
            }
        }
    }
}

@Composable
fun ProfileAvatarPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Primary100, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = "Profile Picture",
            tint = Primary500,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun ProfileAvatarImage(
    imageModel: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String = "Profile Picture"
) {
    val resolvedModel = remember(imageModel) {
        when (imageModel) {
            is String -> imageModel
                .trim()
                .takeIf { it.isNotEmpty() }
                ?.let { value ->
                    if (value.startsWith("/") && File(value).exists()) File(value) else value
                }
            else -> imageModel
        }
    }
    var hasError by remember(resolvedModel) { mutableStateOf(false) }

    if (resolvedModel == null || hasError) {
        ProfileAvatarPlaceholder(modifier = modifier)
    } else {
        AsyncImage(
            model = resolvedModel,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            onError = { hasError = true },
            modifier = modifier
        )
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Primary100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Primary500,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Gray900
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Navigasi",
            tint = Gray400,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Gray500
        )
        Text(
            text = value,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Gray900
        )
    }
}

@Composable
fun EmptyEventState(
    text: String = stringResource(R.string.empty_no_events),
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White,
                        Primary100.copy(alpha = 0.38f),
                        Secondary100.copy(alpha = 0.44f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 34.dp, horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(Color.White.copy(alpha = 0.86f), CircleShape)
                .border(1.dp, Primary100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Primary500,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = text,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Gray900,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MyEventCard(event: MyEventData, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .let { mod ->
                if (onClick != null) mod.clickable { onClick() } else mod
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = Primary500,
                    style = TextStyle(
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                MyEventDetailItem(Icons.Outlined.CalendarToday, event.date)
                MyEventDetailItem(Icons.Outlined.Group, event.attendees)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.status,
                    color = Secondary500,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
fun MyEventDetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, contentDescription = text, tint = Gray600, modifier = Modifier.size(13.dp))
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

@Composable
fun CertificateCard(
    cert: CertificateData,
    onDownload: (CertificateData) -> Unit = {}
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Primary300, Secondary400)
    )
    var showDialog by remember { mutableStateOf(false) }
    val isDownloaded = cert.filePath != null

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = cert.imageUrl ?: cert.filePath,
                    contentDescription = "Full Certificate",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .border(width = 1.5.dp, brush = gradientBrush, shape = RoundedCornerShape(20.dp))
            .clickable { showDialog = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = cert.imageUrl ?: cert.filePath,
                contentDescription = cert.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = cert.title,
                color = Gray900,
                style = TextStyle(
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CertDetailItem(Icons.Outlined.CalendarToday, cert.date)
                        Spacer(modifier = Modifier.width(16.dp))
                        CertDetailItem(Icons.Outlined.AccessTime, cert.time)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CertDetailItem(Icons.Outlined.LocationOn, cert.location)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Secondary500, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cert.category,
                            color = Color.White,
                            fontFamily = PlusJakartaSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onDownload(cert) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = if (isDownloaded) "Sudah diunduh" else "Unduh sertifikat",
                            tint = if (isDownloaded) Primary500 else Gray500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CertDetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = text, tint = Gray600, modifier = Modifier.size(14.dp))
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
