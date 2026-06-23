package com.app.lokacara.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.R
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.PlusJakartaSansFont
import com.app.lokacara.ui.theme.SvgBackground
import com.app.lokacara.ui.theme.SvgPrimaryBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var showLogoPopup by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1400)
        showLogoPopup = false
    }

    fun finish(next: () -> Unit) {
        scope.launch {
            settingsManager.setOnboardingCompleted()
            next()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SvgBackground)
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PulsingLogo(logoRes = R.drawable.logo_lokacara, size = 118)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Lokacara",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                color = SvgPrimaryBlue
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Temukan event terdekat, simpan tiket, dan kelola acara dalam satu aplikasi.",
                fontFamily = PlusJakartaSansFont,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Gray500,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            Spacer(modifier = Modifier.height(34.dp))
            Button(
                onClick = { finish(onContinue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SvgPrimaryBlue)
            ) {
                Text(
                    text = "Mulai",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                onClick = { finish(onSkip) },
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Lewati, saya sudah punya akun",
                    fontFamily = PlusJakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    color = SvgPrimaryBlue
                )
            }
        }

        AnimatedVisibility(
            visible = showLogoPopup,
            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.72f, animationSpec = tween(260)),
            exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.92f, animationSpec = tween(220))
        ) {
            LogoPopup()
        }
    }
}

@Composable
private fun LogoPopup() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SvgBackground),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 34.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PulsingLogo(logoRes = R.drawable.logo_lokacara, size = 126)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Lokacara",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    color = SvgPrimaryBlue
                )
            }
        }
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
        contentDescription = "Logo Lokacara",
        modifier = Modifier
            .size(size.dp)
            .scale(scale.value),
        contentScale = ContentScale.Fit
    )
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(onContinue = {}, onSkip = {})
}
