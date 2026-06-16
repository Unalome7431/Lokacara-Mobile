package com.app.lokacara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.model.ReminderScheduleFormatter
import com.app.lokacara.model.ReminderScheduleItem
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.theme.Gray400
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray700
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.PlusJakartaSansFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.Secondary100
import com.app.lokacara.ui.theme.Secondary500

@Composable
fun ReminderSchedulePanel(
    startDatetime: String,
    modifier: Modifier = Modifier
) {
    val schedule = remember(startDatetime) {
        ReminderScheduleFormatter.build(startDatetime)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Primary100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = Primary500,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pengingat otomatis",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Gray900
                    )
                    Text(
                        text = "Email + push terjadwal",
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 12.sp,
                        color = Gray500
                    )
                }
                Text(
                    text = "Aktif",
                    fontFamily = PlusJakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Secondary500,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Secondary100)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            if (schedule.isEmpty()) {
                Text(
                    text = "Jadwal belum tersedia",
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 13.sp,
                    color = Gray600
                )
            } else {
                schedule.forEach { item ->
                    ReminderScheduleRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun ReminderScheduleRow(item: ReminderScheduleItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (item.hasPassed) Gray400 else Primary500)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Gray900
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.scheduledAt,
                fontFamily = PlusJakartaSansFont,
                fontSize = 12.sp,
                color = Gray600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = if (item.hasPassed) "Jadwal lewat" else "Menunggu",
            fontFamily = PlusJakartaSansFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (item.hasPassed) Gray700 else Primary500,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (item.hasPassed) Gray100 else Primary100)
                .padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}
