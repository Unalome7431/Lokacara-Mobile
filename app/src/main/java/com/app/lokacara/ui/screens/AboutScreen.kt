package com.app.lokacara.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.components.SnackbarManager
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.Primary500

@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val highlights = listOf(
        "Discover" to "Cari event yang relevan berdasarkan kategori, lokasi, dan minat.",
        "Join" to "Daftar, simpan, dan ikuti event dari satu aplikasi.",
        "Track" to "Pantau tiket, sertifikat, dan riwayat aktivitas dalam profile area."
    )

    ProfilePageScaffold(title = "Tentang Lokacara", onBack = { navController.navigateBackOrHome() }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Lokacara adalah aplikasi event discovery dan event management untuk pengguna yang ingin menemukan kegiatan, menyimpan event favorit, dan mengelola partisipasi mereka dalam satu tempat.",
                    fontFamily = NunitoFont,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Gray900
                )
            }

            items(highlights, key = { it.first }, contentType = { "highlight" }) { (title, description) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RowContent(
                        icon = when (title) {
                            "Discover" -> Icons.Rounded.Search
                            "Join" -> Icons.Rounded.People
                            else -> Icons.Rounded.Info
                        },
                        title = title,
                        description = description
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Contact",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Gray900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Jika ada pertanyaan atau masukan produk, hubungi tim Lokacara melalui email berikut.",
                            fontFamily = NunitoFont,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Gray500
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@lokacara.my.id")
                                }
                                runCatching { context.startActivity(intent) }
                                    .onFailure { SnackbarManager.showError("Aplikasi email tidak tersedia") }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MailOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Contact Us",
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .background(Primary500.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Primary500,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontFamily = NunitoFont,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Gray500
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    LokacaraMobileTheme {
        AboutScreen(navController = rememberNavController())
    }
}
