package com.app.lokacara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                when {
                    isLoggedIn != null && isOnboardingCompleted != null -> {
                        NavGraph(
                            isLoggedIn = isLoggedIn == true,
                            isOnboardingCompleted = isOnboardingCompleted == true
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFAF8FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading...")
                        }
                    }
                }
            }
        }
    }
}