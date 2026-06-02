package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.lokacara.ui.navigation.NavigationActions
import com.app.lokacara.model.CertificateData
import com.app.lokacara.ui.components.CertificateCard
import com.app.lokacara.ui.components.EmptyEventState
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun CertificatesScreen(
    navActions: NavigationActions,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val certificates by viewModel.certificates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp).clickable { navActions.goBack() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Sertifikat",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(20.dp))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary500
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (certificates.isEmpty()) {
                        item {
                            EmptyEventState(
                                text = "Belum Ada Sertifikat\nIkuti Event Untuk Mendapatkannya",
                                onClick = { navActions.navigateTo(Screen.Explore.route) }
                            )
                        }
                    } else {
                        items(certificates, key = { it.id }) { cert ->
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
}

@Preview(showBackground = true)
@Composable
fun CertificatesScreenPreview() {
    LokacaraMobileTheme {
        CertificatesScreen(navActions = NavigationActions(
            navigateTo = { },
            goBack = { }
        ))
    }
}
