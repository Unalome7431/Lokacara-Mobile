package com.app.lokacara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.lokacara.ui.screens.HomeScreen
import com.app.lokacara.ui.screens.OnboardingScreen
import com.app.lokacara.ui.theme.LokacaraMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LokacaraMobileTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    OnboardingScreen(onFinish = { showSplash = false })
                } else {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LokacaraMobileTheme {
        Greeting("Android")
    }
}