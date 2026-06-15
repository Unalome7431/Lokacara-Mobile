package com.app.lokacara.ui.components.createevent

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.ui.theme.*

@Composable
fun EventReadinessCard(
    completed: Int,
    total: Int,
    progress: Float,
    isOnline: Boolean,
    scheduleReady: Boolean,
    locationReady: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kelengkapan Event",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Gray900
                    )
                    Text(
                        text = "$completed/$total detail wajib terisi",
                        fontFamily = NunitoFont,
                        fontSize = 12.sp,
                        color = Gray600
                    )
                }
                Surface(
                    color = if (completed == total) SvgOrange.copy(alpha = 0.14f) else SvgPrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (completed == total) "Siap" else "${(progress * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (completed == total) SvgOrange else SvgPrimaryBlue
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (completed == total) SvgOrange else SvgPrimaryBlue,
                trackColor = CreateEventNeutralTrack
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(
                    label = if (isOnline) "Online" else "Offline",
                    isComplete = true,
                    icon = if (isOnline) Icons.Outlined.FileUpload else Icons.Outlined.Place,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = if (scheduleReady) "Jadwal OK" else "Jadwal",
                    isComplete = scheduleReady,
                    icon = Icons.Outlined.DateRange,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = if (locationReady) "Lokasi OK" else "Lokasi",
                    isComplete = locationReady,
                    icon = if (isOnline) Icons.Outlined.FileUpload else Icons.Outlined.MyLocation,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    isComplete: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isComplete) SvgPrimaryBlue.copy(alpha = 0.11f) else CreateEventNeutralPill,
        label = "statusPillBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isComplete) SvgPrimaryBlue else Gray600,
        label = "statusPillContent"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 9.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isComplete) Icons.Outlined.CheckCircle else icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
