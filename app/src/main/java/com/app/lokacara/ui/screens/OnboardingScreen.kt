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

    LaunchedEffect(key1 = true) {
        kotlinx.coroutines.delay(1000)
        splashPhase = 2
        kotlinx.coroutines.delay(1000)
        splashPhase = 3
        kotlinx.coroutines.delay(1500)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8FF)),
        contentAlignment = Alignment.Center
    ) {
        when (splashPhase) {
            1 -> {
                Image(
                    painter = painterResource(id = R.drawable.lokacaralogo),
                    contentDescription = "Splash Logo",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
            }
            2 -> {
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