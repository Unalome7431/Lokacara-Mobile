package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.theme.*

@Composable
fun SettingsScreen(navController: NavController) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

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
                text = "Pengaturan",
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
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Preferensi
            SettingsSectionTitle(title = "Preferensi")
            SettingsCard {
                SettingsToggleRow(
                    title = "Notifikasi",
                    isChecked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Keamanan
            SettingsSectionTitle(title = "Keamanan")
            SettingsCard {
                SettingsActionRow(title = "Ubah Kata Sandi", onClick = { /* Navigate to Change Password */ })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Bantuan & Informasi
            SettingsSectionTitle(title = "Bantuan & Informasi")
            SettingsCard {
                SettingsActionRow(title = "Pusat Bantuan", onClick = { /* Navigate to Help Center */ })
                HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(title = "Syarat & Ketentuan", onClick = { /* Navigate to T&C */ })
                HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(title = "Kebijakan Privasi", onClick = { /* Navigate to Privacy Policy */ })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Danger Zone
            SettingsSectionTitle(title = "Lainnya")
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Show Delete Account Dialog */ }
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hapus Akun",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Red // Red for danger
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Gray500,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsToggleRow(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Gray900
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary500,
                uncheckedThumbColor = Gray400,
                uncheckedTrackColor = Gray100
            )
        )
    }
}

@Composable
fun SettingsActionRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Gray900
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    LokacaraMobileTheme {
        SettingsScreen(navController = rememberNavController())
    }
}
