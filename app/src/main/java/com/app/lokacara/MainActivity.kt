package com.app.lokacara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.lokacara.ui.components.SnackbarManager
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
            val snackbarHostState = remember { SnackbarHostState().also { SnackbarManager.init(it) } }
            val isLoggedIn by viewModel.isLoggedIn.collectAsState(initial = null)
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState(initial = null)

            LokacaraMobileTheme {
                Box(Modifier.fillMaxSize()) {
                    when {
                        isLoggedIn != null && isOnboardingCompleted != null -> {
                            NavGraph(
                                isLoggedIn = isLoggedIn == true,
                                isOnboardingCompleted = isOnboardingCompleted == true
                            )
                        }
                        else -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Loading...")
                            }
                        }
                    }

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(start = 24.dp, end = 24.dp, top = 48.dp)
                    ) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = Color(0xFF323232),
                            contentColor = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.widthIn(max = 400.dp)
                        )
                    }
                }
            }
        }
    }
}
