package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.model.CertificateData
import com.app.lokacara.ui.components.CertificateCard
import com.app.lokacara.ui.components.EmptyEventState
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun CertificatesScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val certificates by viewModel.certificates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    ProfilePageScaffold(title = "Sertifikat", onBack = { navController.popBackStack() }) {
        if (isLoading && certificates.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary500)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gray50),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                if (certificates.isEmpty()) {
                    item {
                        EmptyEventState(
                            text = "Belum Ada Sertifikat\nIkuti Event Untuk Mendapatkannya",
                            onClick = { navController.navigate(Screen.Explore.createRoute("")) }
                        )
                    }
                } else {
                    items(
                        items = certificates,
                        key = { cert: CertificateData -> cert.id }
                    ) { cert ->
                        CertificateCard(
                            cert = cert,
                            onDownload = { viewModel.downloadCertificate(it) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CertificatesScreenPreview() {
    LokacaraMobileTheme {
        CertificatesScreen(navController = rememberNavController())
    }
}
