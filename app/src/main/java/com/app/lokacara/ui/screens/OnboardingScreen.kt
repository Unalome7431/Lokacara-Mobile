package com.app.lokacara.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.lokacara.R

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var splashPhase by remember { mutableIntStateOf(1) }

    // Splash Effect: 3 Phases
    LaunchedEffect(key1 = true) {
        kotlinx.coroutines.delay(1000) // Phase 1: Logo only
        splashPhase = 2
        kotlinx.coroutines.delay(1000) // Phase 2: Gray Logo + Gray Text
        splashPhase = 3
        kotlinx.coroutines.delay(1500) // Phase 3: Normal Logo + Normal Text
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8FF)), // Using the SvgBackground color
        contentAlignment = Alignment.Center
    ) {
        when (splashPhase) {
            1 -> {
                // Phase 1: lokacaralogo
                Image(
                    painter = painterResource(id = R.drawable.lokacaralogo),
                    contentDescription = "Splash Logo",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
            }
            2 -> {
                // Phase 2: lokacaralogogray and lokacaragray
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lokacaralogogray),
                        contentDescription = "Splash Logo Gray",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.lokacaragray),
                        contentDescription = "Splash Text Gray",
                        modifier = Modifier.height(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            3 -> {
                // Phase 3: lokacaralogo and lokacara
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lokacaralogo),
                        contentDescription = "Splash Logo",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.lokacara),
                        contentDescription = "Splash Text",
                        modifier = Modifier.height(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(onFinish = {})
}