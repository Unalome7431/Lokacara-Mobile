package com.app.lokacara.ui.components.createevent

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.lokacara.ui.theme.*

@Composable
fun CustomToggleSwitch(isOnline: Boolean, onToggle: (Boolean) -> Unit) {
    val onlineBackground by animateColorAsState(
        targetValue = if (isOnline) SvgPrimaryBlue else Color.Transparent,
        label = "onlineToggleBackground"
    )
    val offlineBackground by animateColorAsState(
        targetValue = if (!isOnline) SvgPrimaryBlue else Color.Transparent,
        label = "offlineToggleBackground"
    )
    val onlineTextColor by animateColorAsState(
        targetValue = if (isOnline) Color.White else Gray800,
        label = "onlineToggleText"
    )
    val offlineTextColor by animateColorAsState(
        targetValue = if (!isOnline) Color.White else Gray800,
        label = "offlineToggleText"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)), RoundedCornerShape(50))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(onlineBackground)
                .clickable { onToggle(true) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Online",
                color = onlineTextColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(offlineBackground)
                .clickable { onToggle(false) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Offline",
                color = offlineTextColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
