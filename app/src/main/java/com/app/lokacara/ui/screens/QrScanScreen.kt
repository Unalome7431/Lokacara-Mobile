package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.lokacara.ui.components.LokacaraTextField
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.QrScanViewModel

@Composable
fun QrScanScreen(
    navController: NavController,
    eventId: Long,
    viewModel: QrScanViewModel = hiltViewModel()
) {
    val qrToken by viewModel.qrToken.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val result by viewModel.result.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.setEventId(eventId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp).clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Scan QR Peserta",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(20.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Masukkan Token QR",
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Gray800
        )

        Spacer(modifier = Modifier.height(12.dp))

        LokacaraTextField(
            value = qrToken,
            onValueChange = { viewModel.qrToken.value = it },
            placeholder = "Tempel kode QR di sini"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.scan() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verifikasi QR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        result?.let { scan ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = Secondary500, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        scan.message,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Secondary500
                    )
                    scan.registration?.let { reg ->
                        Text("Status: ${reg.status}", fontSize = 14.sp, color = Gray700)
                        reg.checked_in_at?.let {
                            Text("Check-in: $it", fontSize = 12.sp, color = Gray500)
                        }
                    }
                }
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = SemanticErrorBase, fontSize = 14.sp)
        }
    }
}
