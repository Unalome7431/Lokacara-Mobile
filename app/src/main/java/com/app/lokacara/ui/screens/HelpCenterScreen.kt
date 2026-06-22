package com.app.lokacara.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.components.LokacaraTextField
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.components.SnackbarManager
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.theme.*

@Composable
fun HelpCenterScreen(navController: NavController) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val allFaqs = remember {
        listOf(
            "Bagaimana cara membuat event?" to "Anda dapat membuat event dengan menekan tombol '+' di navbar bawah dan mengisi detail event yang diperlukan.",
            "Bagaimana cara membeli tiket?" to "Cari event yang Anda inginkan di halaman Explore, lalu tekan tombol 'Beli Tiket' dan ikuti langkah pembayarannya.",
            "Di mana saya bisa melihat sertifikat?" to "Sertifikat dapat dilihat di menu Profile > Sertifikat Saya setelah Anda menyelesaikan event terkait.",
            "Bagaimana cara membatalkan pesanan tiket?" to "Pembatalan tiket dapat dilakukan melalui menu Tiket Saya sebelum batas waktu yang ditentukan oleh penyelenggara.",
            "Apakah data saya aman?" to "Ya, kami berkomitmen untuk melindungi privasi dan keamanan data Anda sesuai dengan kebijakan privasi kami."
        )
    }
    val faqs = remember(searchQuery) {
        if (searchQuery.isBlank()) allFaqs
        else allFaqs.filter { (q, a) ->
            q.contains(searchQuery, ignoreCase = true) || a.contains(searchQuery, ignoreCase = true)
        }
    }

    ProfilePageScaffold(
        title = "Pusat Bantuan",
        onBack = { navController.navigateBackOrHome() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "header", contentType = "header") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier.size(100.dp).background(Primary100, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.SupportAgent, null, tint = Primary500, modifier = Modifier.size(50.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Halo, ada yang bisa kami bantu?",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Gray900,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    LokacaraTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Cari topik bantuan...",
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Gray400) },
                        isOutlined = true,
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Pertanyaan Sering Diajukan",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Gray900,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                }
            }

            if (faqs.isEmpty()) {
                item(key = "empty", contentType = "empty") {
                    HelpEmptySearchCard(query = searchQuery, onReset = { searchQuery = "" })
                }
            } else {
                items(faqs, key = { it.first }, contentType = { "faq" }) { (question, answer) ->
                    FAQItem(question = question, answer = answer, modifier = Modifier.padding(bottom = 12.dp))
                }
            }

            item(key = "contact", contentType = "contact") {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Primary100),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.HeadsetMic, null, tint = Primary500, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Masih butuh bantuan?", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primary900)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tim dukungan kami siap membantu Anda 24/7.", fontFamily = NunitoFont, fontSize = 14.sp, color = Primary700, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))
                        val context = LocalContext.current
                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:lokacara.team@gmail.com")
                                }
                                runCatching { context.startActivity(intent) }
                                    .onFailure { SnackbarManager.showError("Aplikasi email tidak tersedia") }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Hubungi Kami", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun FAQItem(question: String, answer: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(question) { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 4.dp else 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(question, fontFamily = NunitoFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray900, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = null, tint = Primary500)
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Gray100, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(answer, fontFamily = NunitoFont, fontSize = 14.sp, color = Gray600, lineHeight = 22.sp)
                }
            }
        }
    }
}

@Composable
private fun HelpEmptySearchCard(query: String, onReset: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.SearchOff, contentDescription = null, tint = Gray400, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Tidak ada hasil untuk \"$query\"",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Gray900,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onReset) {
                Text("Reset pencarian", color = Primary500, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HelpCenterScreenPreview() {
    LokacaraMobileTheme {
        HelpCenterScreen(navController = rememberNavController())
    }
}
