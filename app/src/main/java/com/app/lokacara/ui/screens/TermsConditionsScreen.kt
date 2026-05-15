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
import androidx.compose.material.icons.rounded.Gavel
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
fun TermsConditionsScreen(navController: NavController) {
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
                text = "Syarat & Ketentuan",
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
                            imageVector = Icons.Rounded.Gavel,
                            contentDescription = null,
                            tint = Primary500,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Ketentuan Layanan Lokacara",
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

                    TermsSection(
                        title = "1. Penerimaan Ketentuan",
                        content = "Dengan mengunduh atau menggunakan aplikasi Lokacara, Anda setuju untuk terikat oleh Syarat dan Ketentuan ini. Jika Anda tidak setuju dengan bagian apa pun dari ketentuan ini, Anda tidak diperbolehkan menggunakan aplikasi kami."
                    )

                    TermsSection(
                        title = "2. Penggunaan Aplikasi",
                        content = "Anda setuju untuk menggunakan aplikasi hanya untuk tujuan yang sah dan sesuai dengan semua hukum dan peraturan yang berlaku. Anda bertanggung jawab penuh atas aktivitas yang terjadi di bawah akun Anda."
                    )

                    TermsSection(
                        title = "3. Pembuatan Event & Tiket",
                        content = "Lokacara menyediakan platform untuk membuat dan memanajemen event serta pembelian tiket. Kami tidak bertanggung jawab atas isi event yang dibuat oleh pengguna, namun kami berhak menghapus konten yang melanggar kebijakan kami."
                    )

                    TermsSection(
                        title = "4. Pembayaran",
                        content = "Semua transaksi pembayaran diproses melalui penyedia layanan pembayaran pihak ketiga yang aman. Lokacara tidak menyimpan informasi kartu kredit atau detail pembayaran sensitif lainnya."
                    )

                    TermsSection(
                        title = "5. Perubahan Ketentuan",
                        content = "Kami berhak untuk mengubah atau mengganti Syarat dan Ketentuan ini kapan saja. Perubahan akan berlaku segera setelah diposting di aplikasi. Penggunaan berkelanjutan Anda atas aplikasi setelah perubahan tersebut merupakan penerimaan Anda terhadap ketentuan baru."
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun TermsSection(title: String, content: String) {
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
fun TermsConditionsScreenPreview() {
    LokacaraMobileTheme {
        TermsConditionsScreen(navController = rememberNavController())
    }
}