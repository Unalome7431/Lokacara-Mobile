package com.app.lokacara.ui.screens
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.R
import com.app.lokacara.ui.components.ProfileAvatarImage
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.navigation.navigateToLoginAndClearMain
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    rootNavController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val myEvents by viewModel.myEvents.collectAsStateWithLifecycle()
    val certificates by viewModel.certificates.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val hasProfileIdentity = userProfile.name.isNotBlank() || userProfile.email.isNotBlank()
    var showLogoutConfirm by remember { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDashboard() }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Keluar Akun", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Apakah Anda yakin ingin keluar dari akun ini?", fontFamily = PlusJakartaSansFont, color = Gray700) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout { rootNavController.navigateToLoginAndClearMain() }
                }) {
                    Text("Keluar", color = SemanticErrorBase, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Batal", color = Gray600)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvgBackground)
            .statusBarsPadding()
    ) {
        if (isLoading && !hasProfileIdentity) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary500)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item(key = "identity", contentType = "identity") {
                FlatProfileIdentity(
                    name = userProfile.name.ifBlank { "Pengguna" },
                    email = userProfile.email,
                    location = userProfile.location,
                    imageUrl = userProfile.profileImageUrl,
                    myEventCount = myEvents.size,
                    certificateCount = certificates.size,
                    onEditClick = { navController.navigate(Screen.EditProfile.route) }
                )
            }

            item(key = "activity", contentType = "shortcuts") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Aktivitas Saya",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Gray900,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileShortcutTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Event,
                            title = stringResource(R.string.profile_my_events),
                            subtitle = "${myEvents.size} event",
                            accentColor = Primary500,
                            onClick = { navController.navigate(Screen.MyEvents.route) }
                        )
                        ProfileShortcutTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Bookmark,
                            title = stringResource(R.string.profile_saved_events),
                            subtitle = "Event favorit",
                            accentColor = Secondary500,
                            onClick = { navController.navigate(Screen.SavedEvents.route) }
                        )
                    }
                    ProfileShortcutTile(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.WorkspacePremium,
                        title = stringResource(R.string.profile_certificates),
                        subtitle = "${certificates.size} sertifikat dikumpulkan",
                        accentColor = SvgOrange,
                        onClick = { navController.navigate(Screen.Certificates.route) }
                    )
                }
            }

            item(key = "settings", contentType = "settings") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Informasi & Pengaturan",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Gray900,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ProfileUtilityRow(
                                icon = Icons.Rounded.Settings,
                                title = stringResource(R.string.profile_settings),
                                subtitle = "Keamanan dan preferensi",
                                onClick = { navController.navigate(Screen.Settings.route) },
                                accentColor = Primary500,
                                showDivider = true
                            )
                            ProfileUtilityRow(
                                icon = Icons.Rounded.Info,
                                title = stringResource(R.string.profile_about),
                                subtitle = "Tentang Lokacara",
                                onClick = { navController.navigate(Screen.About.route) },
                                accentColor = Secondary500,
                                showDivider = false
                            )
                        }
                    }
                }
            }

            item {
                ProfileLogoutRow(onClick = { showLogoutConfirm = true })
            }
        }
    }
}

@Composable
private fun FlatProfileIdentity(
    name: String,
    email: String,
    location: String,
    imageUrl: String?,
    myEventCount: Int,
    certificateCount: Int,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .padding(2.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                ProfileAvatarImage(
                    imageModel = imageUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable { onEditClick() }
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Primary500, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .clip(CircleShape)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Ubah Profil",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = name,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = Gray900,
            textAlign = TextAlign.Center
        )

        Text(
            text = email.ifBlank { "Email belum tersedia" },
            fontFamily = PlusJakartaSansFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Gray500,
            textAlign = TextAlign.Center
        )

        if (location.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = Primary500,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = location,
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray600
                )
            }
        }
    }
}

@Composable
private fun ProfileStatBox(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(accentColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, accentColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = accentColor
        )
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Gray500,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ProfileShortcutTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accentColor.copy(alpha = 0.12f),
                            Color.White,
                            Color.White
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.78f), CircleShape)
                        .border(1.dp, accentColor.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileUtilityRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    accentColor: Color = Primary500,
    showDivider: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(21.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Gray900
                )
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 12.sp,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray300,
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = Gray100
            )
        }
    }
}

@Composable
private fun ProfileLogoutRow(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SemanticErrorLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = stringResource(R.string.profile_logout),
                    tint = SemanticErrorBase,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.profile_logout),
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = SemanticErrorBase
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFFAF8FF)
@Composable
fun ProfileScreenPreview() {
    LokacaraMobileTheme {
        if (LocalInspectionMode.current) {
            Box(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    FlatProfileIdentity(
                        name = "Pengguna Lokacara",
                        email = "user@lokacara.my.id",
                        location = "Jakarta",
                        imageUrl = null,
                        myEventCount = 3,
                        certificateCount = 1,
                        onEditClick = {}
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileShortcutTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Event,
                            title = "Event Saya", subtitle = "3 event",
                            accentColor = Primary500, onClick = {}
                        )
                        ProfileShortcutTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Bookmark,
                            title = "Event Tersimpan", subtitle = "Event favorit",
                            accentColor = Secondary500, onClick = {}
                        )
                    }
                }
            }
        } else {
            ProfileScreen(navController = rememberNavController(), rootNavController = rememberNavController())
        }
    }
}
