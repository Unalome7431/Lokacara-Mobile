package com.app.lokacara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.model.NotificationItem
import com.app.lokacara.ui.theme.*

@Composable
fun NotificationCard(notification: NotificationItem) {
    val bgColor = if (!notification.isRead) Primary100 else Secondary100

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp)) // Bentuk Pil murni
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(Gray300, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = buildAnnotatedString {
                    if (notification.senderName.isNotEmpty()) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFont)) {
                            append(notification.senderName)
                        }
                        append(" ")
                    }
                    append(notification.message)
                },
                fontSize = 13.sp,
                fontFamily = PlusJakartaSansFont,
                color = Gray900,
                lineHeight = 18.sp
            )
            Text(
                text = notification.time,
                fontSize = 11.sp,
                fontFamily = PlusJakartaSansFont,
                color = Gray600,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}