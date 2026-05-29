package com.app.lokacara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.app.lokacara.ui.navigation.NavGraph
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isLoggedIn by viewModel.isLoggedIn.collectAsState(initial = null)
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState(initial = null)

            LokacaraMobileTheme {
                if (isLoggedIn != null && isOnboardingCompleted != null) {
                    NavGraph(
                        isLoggedIn = isLoggedIn == true,
                        isOnboardingCompleted = isOnboardingCompleted == true
                    )
                }
            }
        }
    }
}
