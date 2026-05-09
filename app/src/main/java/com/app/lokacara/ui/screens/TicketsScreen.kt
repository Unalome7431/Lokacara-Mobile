package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.SvgBackground

@Composable
fun TicketsScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SvgBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tiket Saya",
                style = TextStyle(
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.Black
                )
            )
            Text(
                text = "Belum ada tiket aktif",
                style = TextStyle(
                    fontFamily = NunitoFont,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            )
        }
    }
}
