package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.app.lokacara.R
import com.app.lokacara.ui.components.ProfileAvatarPlaceholder
import com.app.lokacara.ui.components.ProfileStatChip
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.PlusJakartaSansFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.Secondary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SemanticErrorLight
import com.app.lokacara.ui.theme.SvgBackground
import com.app.lokacara.ui.theme.SvgOrange
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    rootNavController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val myEvents by viewModel.myEvents.collectAsState()
    val certificates by viewModel.certificates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasProfileIdentity = userProfile.name.isNotBlank() || userProfile.email.isNotBlank()

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ProfileIdentityCard(
                    name = userProfile.name.ifBlank { "Pengguna" },
                    email = userProfile.email,
                    location = userProfile.location,
                    imageUrl = userProfile.profileImageUrl,
                    myEventCount = myEvents.size,
                    certificateCount = certificates.size
                )
            }

            item {
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
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileShortcutTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.WorkspacePremium,
                        title = stringResource(R.string.profile_certificates),
                        subtitle = "${certificates.size} sertifikat",
                        accentColor = SvgOrange,
                        onClick = { navController.navigate(Screen.Certificates.route) }
                    )
                    ProfileShortcutTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Person,
                        title = stringResource(R.string.profile_edit_profile),
                        subtitle = "Data akun",
                        accentColor = Primary500,
                        onClick = { navController.navigate(Screen.EditProfile.route) }
                    )
                }
            }

            item {
                ProfileUtilityRow(
                    icon = Icons.Rounded.Settings,
                    title = stringResource(R.string.profile_settings),
                    subtitle = "Keamanan, preferensi, dan bantuan",
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }

            item {
                ProfileUtilityRow(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.profile_about),
                    subtitle = "Informasi aplikasi Lokacara",
                    onClick = { navController.navigate(Screen.About.route) }
                )
            }

            item {
                ProfileLogoutRow(onClick = { viewModel.logout { rootNavController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } } })
            }
        }
    }
}

@Composable
private fun ProfileIdentityCard(
    name: String,
    email: String,
    location: String,
    imageUrl: String?,
    myEventCount: Int,
    certificateCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White, Primary100.copy(alpha = 0.36f), Color.White)
                    )
                )
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (imageUrl.isNullOrBlank()) {
                    ProfileAvatarPlaceholder(modifier = Modifier.size(82.dp))
                } else {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(82.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Gray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email.ifBlank { "Email belum tersedia" },
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Gray600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (location.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = Gray500,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location,
                                fontFamily = PlusJakartaSansFont,
                                fontSize = 12.sp,
                                color = Gray500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileStatChip(
                    label = "Event Saya",
                    value = myEventCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatChip(
                    label = "Sertifikat",
                    value = certificateCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
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
            .height(126.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
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
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    .background(Gray50, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = Primary500, modifier = Modifier.size(21.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 12.sp,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    LokacaraMobileTheme {
        ProfileScreen(navController = rememberNavController(), rootNavController = rememberNavController())
    }
}
