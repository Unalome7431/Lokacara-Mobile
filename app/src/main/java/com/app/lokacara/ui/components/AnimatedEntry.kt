package com.app.lokacara.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun AnimatedEntry(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 240,
    offsetY: Int = 24,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(delayMillis) {
        if (!visible) {
            if (delayMillis > 0) {
                kotlinx.coroutines.delay(delayMillis.toLong())
            }
            visible = true
        }
    }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(durationMillis, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { offsetY },
                        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
                    )
        ) {
            content()
        }
    }
}
