package com.app.lokacara.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.theme.Gray200

@Composable
fun shimmerBrush(): Brush {
    return remember {
        Brush.linearGradient(
            colors = listOf(
                Gray200.copy(alpha = 0.6f),
                Gray100.copy(alpha = 0.3f),
                Gray200.copy(alpha = 0.6f)
            ),
            start = Offset.Zero,
            end = Offset(900f, 900f)
        )
    }
}
