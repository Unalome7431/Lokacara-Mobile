package com.app.lokacara.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Primary500,
    onPrimary = Color.White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary900,
    secondary = Secondary500,
    onSecondary = Color.White,
    secondaryContainer = Secondary100,
    onSecondaryContainer = Secondary900,
    background = Gray100,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    error = SemanticErrorBase,
    onError = Color.White,
    errorContainer = SemanticErrorLight,
    onErrorContainer = SemanticErrorBase
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary400,
    onPrimary = Color.White,
    primaryContainer = Primary800,
    onPrimaryContainer = Primary100,
    secondary = Secondary400,
    onSecondary = Color.White,
    secondaryContainer = Secondary800,
    onSecondaryContainer = Secondary100,
    background = Gray900,
    onBackground = Gray100,
    surface = Gray800,
    onSurface = Gray100,
    error = SemanticErrorBase,
    onError = Color.White
)

@Composable
fun LokacaraMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}