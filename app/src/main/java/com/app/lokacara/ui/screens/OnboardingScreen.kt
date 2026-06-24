package com.app.lokacara.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.R
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.ui.theme.Gray400
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.SvgBackground
import com.app.lokacara.ui.theme.SvgPrimaryBlue

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context.applicationContext) }
    var splashPhase by remember { mutableIntStateOf(1) }

    LaunchedEffect(key1 = true) {
        kotlinx.coroutines.delay(1000)
        splashPhase = 2
        kotlinx.coroutines.delay(1000)
        splashPhase = 3
        kotlinx.coroutines.delay(2000)
        settingsManager.setOnboardingCompleted()
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SvgBackground),
        contentAlignment = Alignment.Center
    ) {
        SplashContent(splashPhase = splashPhase)
    }
}

@Composable
private fun SplashContent(splashPhase: Int) {
    val isGrayPhase = splashPhase == 2

    AnimatedContent(
        targetState = splashPhase,
        transitionSpec = {
            when {
                targetState > initialState -> {
                    (fadeIn(tween(500)) + scaleIn(initialScale = 0.92f, animationSpec = tween(500)))
                        .togetherWith(fadeOut(tween(400)) + scaleOut(targetScale = 0.92f, animationSpec = tween(400)))
                }
                else -> {
                    (fadeIn(tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)))
                        .togetherWith(fadeOut(tween(200)) + scaleOut(targetScale = 0.92f, animationSpec = tween(200)))
                }
            }
        },
        label = "SplashPhase"
    ) { phase ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (phase) {
                1 -> PulsingLogo(logoRes = R.drawable.logo_lokacara, size = 120)
                2 -> PhaseContent(logoRes = R.drawable.logo_lokacara_gray, textColor = Gray400, showText = true)
                3 -> PhaseContent(logoRes = R.drawable.logo_lokacara, textColor = SvgPrimaryBlue, showText = true)
            }
        }
    }
}

@Composable
private fun PhaseContent(logoRes: Int, textColor: Color, showText: Boolean) {
    Image(
        painter = painterResource(id = logoRes),
        contentDescription = "Logo",
        modifier = Modifier.size(120.dp),
        contentScale = ContentScale.Fit
    )
    if (showText) {
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Text(
            text = "Lokacara",
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            color = textColor
        )
    }
}

@Composable
private fun PulsingLogo(logoRes: Int, size: Int) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1.05f, tween(500))
        scale.animateTo(1f, tween(500))
    }

    Image(
        painter = painterResource(id = logoRes),
        contentDescription = "Logo",
        modifier = Modifier
            .size(size.dp)
            .scale(scale.value),
        contentScale = ContentScale.Fit
    )
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(onFinish = {})
}
