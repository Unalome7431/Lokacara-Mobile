package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
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
import com.app.lokacara.ui.components.ProfileSubpageSummaryCard
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.navigation.navigateToExplore
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.SvgOrange
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun CertificatesScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val certificates by viewModel.certificates.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDashboard() }

    ProfilePageScaffold(title = "Sertifikat", onBack = { navController.navigateBackOrHome() }) {
        if (isLoading && certificates.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary500)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray50),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item(key = "summary", contentType = "summary") {
                        ProfileSubpageSummaryCard(
                            title = "Sertifikat",
                            subtitle = "Sertifikat event yang sudah kamu selesaikan.",
                            value = certificates.size.toString(),
                            valueLabel = "file",
                            icon = Icons.Rounded.WorkspacePremium,
                            accentColor = SvgOrange
                        )
                    }
                    if (certificates.isEmpty()) {
                        item(key = "empty", contentType = "empty") {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))
                            EmptyEventState(
                                text = "Kamu belum mengumpulkan sertifikat. Ikuti event dan dapatkan sertifikatmu!",
                                onClick = { navController.navigateToExplore() }
                            )
                        }
                    } else {
                        items(
                            items = certificates,
                            key = { cert: CertificateData -> cert.id },
                            contentType = { "certificate" }
                        ) { cert ->
                            CertificateCard(
                                cert = cert,
                                onDownload = { viewModel.downloadCertificate(it) },
                                onRetryPreview = { viewModel.loadCertificatePreview(it, forceRefresh = true) }
                            )
                        }
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
