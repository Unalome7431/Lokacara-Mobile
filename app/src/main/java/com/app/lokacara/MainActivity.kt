package com.app.lokacara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lokacara.ui.navigation.NavGraph
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LokacaraMobileTheme {
                val mainViewModel: MainViewModel = viewModel()
                val targetDestination by mainViewModel.startDestination.collectAsState()

                if (targetDestination != null) {
                    NavGraph(targetDestination = targetDestination!!)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LokacaraMobileTheme { Greeting("Android") }
}