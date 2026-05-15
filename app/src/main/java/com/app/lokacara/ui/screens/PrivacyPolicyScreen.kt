package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.theme.*

@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    val scrollState = rememberScrollState()

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
                modifier = Modifier.size(20.dp).clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Kebijakan Privasi",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(20.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Primary100, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = Primary500,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Kebijakan Privasi Lokacara",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Gray900,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Terakhir diperbarui: 13 Mei 2026",
                        fontFamily = NunitoFont,
                        fontSize = 12.sp,
                        color = Gray400,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Gray100, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(24.dp))

                    PrivacySection(
                        title = "1. Informasi yang Kami Kumpulkan",
                        content = "Kami mengumpulkan informasi yang Anda berikan langsung kepada kami, seperti saat Anda membuat akun, memperbarui profil, atau melakukan transaksi di aplikasi Lokacara."
                    )

                    PrivacySection(
                        title = "2. Penggunaan Informasi",
                        content = "Kami menggunakan informasi yang dikumpulkan untuk menyediakan, memelihara, dan meningkatkan layanan kami, memproses transaksi, dan mengirimkan komunikasi terkait layanan."
                    )

                    PrivacySection(
                        title = "3. Berbagi Informasi",
                        content = "Kami tidak membagikan informasi pribadi Anda dengan pihak ketiga kecuali sebagaimana dijelaskan dalam kebijakan ini atau dengan persetujuan Anda."
                    )

                    PrivacySection(
                        title = "4. Keamanan Data",
                        content = "Kami mengambil langkah-langkah wajar untuk membantu melindungi informasi tentang Anda dari kehilangan, pencurian, penyalahgunaan, dan akses tidak sah."
                    )

                    PrivacySection(
                        title = "5. Pilihan Anda",
                        content = "Anda dapat memperbarui, memperbaiki, atau menghapus informasi akun Anda kapan saja dengan masuk ke akun Anda atau menghubungi kami."
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text(
            text = title,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Gray900,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            fontFamily = NunitoFont,
            fontSize = 14.sp,
            color = Gray600,
            lineHeight = 22.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyPolicyScreenPreview() {
    LokacaraMobileTheme {
        PrivacyPolicyScreen(navController = rememberNavController())
    }
}