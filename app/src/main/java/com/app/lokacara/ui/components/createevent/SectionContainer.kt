package com.app.lokacara.ui.components.createevent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.ui.theme.*

@Composable
fun SectionContainer(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFD6E4FF),
    shape: Shape = RoundedCornerShape(20.dp),
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape)
            .clip(shape)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)), shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Gray800
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray600
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
        if (content != null) {
            content()
        }
    }
}
