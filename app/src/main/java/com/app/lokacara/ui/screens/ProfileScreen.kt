package com.app.lokacara.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.lokacara.R
import com.app.lokacara.ui.components.ProfileMenuItem
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.*

@Composable
fun ProfileScreen(navController: NavController, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50) // Or Color(0xFFF9F9FB)
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
                text = "Profil Saya",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(20.dp)) // To balance the back button
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.seminar_2), // Dummy image
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = "Daffa Arrivo",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Gray900
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Group 1
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Rounded.Event,
                        title = "Event Saya",
                        onClick = { navController.navigate(Screen.MyEvents.route) }
                    )
                    HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(
                        icon = Icons.Rounded.Person,
                        title = "Edit Profil",
                        onClick = { navController.navigate(Screen.EditProfile.route) }
                    )
                    HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(
                        icon = Icons.Rounded.Bookmark,
                        title = "Event Tersimpan",
                        onClick = { navController.navigate(Screen.SavedEvents.route) }
                    )
                    HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(
                        icon = Icons.Rounded.WorkspacePremium,
                        title = "Sertifikat",
                        onClick = { navController.navigate(Screen.Certificates.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group 2
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Rounded.Tune,
                        title = "Pengaturan",
                        onClick = { navController.navigate(Screen.Settings.route) }
                    )
                    HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(
                        icon = Icons.Rounded.Info,
                        title = "Tentang Lokacara",
                        onClick = { navController.navigate(Screen.About.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Row(
                modifier = Modifier
                    .clickable { onLogout() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Keluar",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Primary500 // Yellow/Orange ish if looking at design, let's use warning or yellow color. Looking at the image, it's yellow. Let's use a yellow color.
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "Keluar",
                    tint = Primary500,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom navbar
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    LokacaraMobileTheme {
        ProfileScreen(navController = rememberNavController(), onLogout = {})
    }
}
