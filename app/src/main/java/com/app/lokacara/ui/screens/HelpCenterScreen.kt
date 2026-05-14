package com.app.lokacara.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun HelpCenterScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val faqs = listOf(
        "Bagaimana cara membuat event?" to "Anda dapat membuat event dengan menekan tombol '+' di navbar bawah dan mengisi detail event yang diperlukan.",
        "Bagaimana cara membeli tiket?" to "Cari event yang Anda inginkan di halaman Explore, lalu tekan tombol 'Beli Tiket' dan ikuti langkah pembayarannya.",
        "Di mana saya bisa melihat sertifikat?" to "Sertifikat dapat dilihat di menu Profile > Sertifikat Saya setelah Anda menyelesaikan event terkait.",
        "Bagaimana cara membatalkan pesanan tiket?" to "Pembatalan tiket dapat dilakukan melalui menu Tiket Saya sebelum batas waktu yang ditentukan oleh penyelenggara.",
        "Apakah data saya aman?" to "Ya, kami berkomitmen untuk melindungi privasi dan keamanan data Anda sesuai dengan kebijakan privasi kami."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Top Bar
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
                text = "Pusat Bantuan",
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
            Spacer(modifier = Modifier.height(24.dp))
            
            // Illustration / Icon Header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Primary100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SupportAgent,
                    contentDescription = null,
                    tint = Primary500,
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Halo, ada yang bisa kami bantu?",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Gray900,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari topik bantuan...", color = Gray400, fontFamily = NunitoFont, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Gray400) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Primary500,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = "Pertanyaan Sering Diajukan",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Gray900,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            faqs.forEach { (question, answer) ->
                FAQItem(question = question, answer = answer)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Contact Support
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
                    Icon(
                        imageVector = Icons.Rounded.HeadsetMic,
                        contentDescription = null,
                        tint = Primary500,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Masih butuh bantuan?",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Primary900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tim dukungan kami siap membantu Anda 24/7.",
                        fontFamily = NunitoFont,
                        fontSize = 14.sp,
                        color = Primary700,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { /* Contact Support */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Hubungi Kami",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 4.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Gray900,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = Primary500
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Gray100, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = answer,
                        fontFamily = NunitoFont,
                        fontSize = 14.sp,
                        color = Gray600,
                        lineHeight = 22.sp
                    )
                }
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