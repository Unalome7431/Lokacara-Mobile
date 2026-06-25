package com.app.lokacara.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.R
import com.app.lokacara.ui.components.LokacaraTextField
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.components.SnackbarManager
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.theme.Gray200
import com.app.lokacara.ui.theme.Gray400
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SemanticErrorLight
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.navigation.navigateToLoginAndClearMain
import com.app.lokacara.viewmodel.SettingsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsScreen(
    navController: NavController,
    rootNavController: NavController? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val isDeleting by viewModel.isDeleting.collectAsStateWithLifecycle()
    val deleteError by viewModel.deleteError.collectAsStateWithLifecycle()
    val deleteSuccess by viewModel.deleteSuccess.collectAsStateWithLifecycle()
    val isGoogleAuth by viewModel.isGoogleAuth.collectAsStateWithLifecycle()
    val hasLocalPassword by viewModel.hasLocalPassword.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val googleWebClientId = stringResource(R.string.google_web_client_id)
    val googleSignInOptions = remember(googleWebClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).apply {
            requestEmail()
            if (googleWebClientId.isNotBlank()) requestIdToken(googleWebClientId)
        }.build()
    }
    val googleSignInClient = remember(googleWebClientId) { GoogleSignIn.getClient(context, googleSignInOptions) }
    val googleDeleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val idToken = account.result?.idToken
            if (!idToken.isNullOrBlank()) {
                viewModel.deleteGoogleAccount(idToken)
            } else {
                SnackbarManager.showError("Verifikasi Google gagal. Coba lagi.")
            }
        } catch (_: Exception) {
            SnackbarManager.showError("Gagal memverifikasi akun Google")
        }
    }
    val isGooglePasswordless = isGoogleAuth && !hasLocalPassword

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            viewModel.resetDeleteSuccess()
            scope.launch {
                viewModel.logout()
                (rootNavController ?: navController).navigateToLoginAndClearMain()
            }
        }
    }

    if (showDeleteDialog) {
        var password by rememberSaveable { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; password = "" },
            title = {
                Text(
                    text = stringResource(R.string.settings_delete_account),
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isGoogleAuth) {
                            "Akun Lokacara Anda akan dihapus permanen. Lanjutkan dengan verifikasi ulang Google untuk mengonfirmasi penghapusan akun."
                        } else {
                            "Akun Lokacara Anda akan dihapus permanen. Data profil, tiket, event, dan sertifikat yang terkait dengan akun ini dapat ikut terdampak. Masukkan kata sandi untuk melanjutkan."
                        },
                        fontFamily = NunitoFont,
                        fontSize = 14.sp,
                        color = Gray600,
                        lineHeight = 20.sp
                    )
                    if (!isGoogleAuth) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LokacaraTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = stringResource(R.string.auth_password_placeholder),
                            isPassword = true,
                            isOutlined = true,
                            containerColor = Color.White
                        )
                    } else if (googleWebClientId.isBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Login Google belum dikonfigurasi di perangkat ini.",
                            fontFamily = NunitoFont,
                            fontSize = 13.sp,
                            color = SemanticErrorBase
                        )
                    }
                    if (deleteError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = deleteError!!,
                            fontFamily = NunitoFont,
                            fontSize = 13.sp,
                            color = SemanticErrorBase
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isGoogleAuth) {
                            googleDeleteLauncher.launch(googleSignInClient.signInIntent)
                        } else {
                            viewModel.deleteAccount(password)
                        }
                    },
                    enabled = !isDeleting && if (isGoogleAuth) googleWebClientId.isNotBlank() else password.isNotBlank()
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = SemanticErrorBase
                        )
                    } else {
                        Text(
                            text = if (isGoogleAuth) "Lanjutkan" else stringResource(R.string.delete),
                            color = SemanticErrorBase,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.clearDeleteError(); password = "" }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = Gray500
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    ProfilePageScaffold(title = "Pengaturan", onBack = { navController.navigateBackOrHome() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_preferences),
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Gray900,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(1.dp, Gray100, RoundedCornerShape(22.dp))
                    .padding(vertical = 8.dp)
            ) {
                SettingsToggleRow(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.settings_notifications),
                    subtitle = "Atur notifikasi aktivitas, tiket, dan pengingat event",
                    isChecked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_security),
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Gray900,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(1.dp, Gray100, RoundedCornerShape(22.dp))
                    .padding(vertical = 8.dp)
            ) {
                if (isGoogleAuth) {
                    GoogleAuthInfoCard(hasLocalPassword = hasLocalPassword)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 1.dp, color = Gray100)
                }
                SettingsActionRow(
                    icon = Icons.Rounded.Lock,
                    title = stringResource(R.string.settings_change_password),
                    subtitle = when {
                        isGooglePasswordless -> "Tambahkan kata sandi lokal untuk akun Google"
                        isGoogleAuth -> "Perbarui kata sandi lokal akun Google Anda"
                        else -> "Perbarui kata sandi masuk akun"
                    },
                    onClick = { navController.navigate(Screen.ChangePassword.route) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 1.dp, color = Gray100)
                SettingsActionRow(
                    icon = Icons.Rounded.PrivacyTip,
                    title = stringResource(R.string.settings_privacy_policy),
                    subtitle = "Baca cara Lokacara mengelola data Anda",
                    onClick = { navController.navigate(Screen.PrivacyPolicy.route) } // Assuming PrivacyPolicy is the right route based on the original code
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_help_info),
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Gray900,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(1.dp, Gray100, RoundedCornerShape(22.dp))
                    .padding(vertical = 8.dp)
            ) {
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Rounded.HelpOutline,
                    title = stringResource(R.string.settings_help_center),
                    subtitle = "Cari jawaban dan hubungi dukungan",
                    onClick = { navController.navigate(Screen.HelpCenter.route) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 1.dp, color = Gray100)
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Rounded.Article,
                    title = stringResource(R.string.settings_terms_conditions),
                    subtitle = "Ketentuan penggunaan layanan Lokacara",
                    onClick = { navController.navigate(Screen.TermsConditions.route) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(1.dp, Gray100, RoundedCornerShape(22.dp))
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteDialog = true }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SemanticErrorLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Hapus Akun",
                            tint = SemanticErrorBase,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.settings_delete_account),
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SemanticErrorBase
                        )
                        Text(
                            text = if (isGoogleAuth) {
                                "Verifikasi ulang dengan Google untuk hapus akun"
                            } else {
                                "Hapus permanen akun dan data terkait"
                            },
                            fontFamily = NunitoFont,
                            fontSize = 12.sp,
                            color = Gray500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun GoogleAuthInfoCard(hasLocalPassword: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Primary100.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Akun Google Terdeteksi",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Gray900
            )
            Text(
                text = if (hasLocalPassword) {
                    "Akun ini tetap terhubung ke Google, dan sekarang juga bisa memakai kata sandi lokal."
                } else {
                    "Akun ini terhubung ke Google. Anda bisa menambahkan kata sandi lokal tanpa melepas login Google."
                },
                fontFamily = NunitoFont,
                fontSize = 12.sp,
                color = Gray600,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(Primary100.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary500, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Gray900)
                Text(subtitle, fontFamily = NunitoFont, fontSize = 12.sp, color = Gray500)
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary500,
                uncheckedThumbColor = Gray400,
                uncheckedTrackColor = Gray200,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(Gray100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Gray600, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Gray900)
                Text(subtitle, fontFamily = NunitoFont, fontSize = 12.sp, color = Gray500)
            }
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Gray400, modifier = Modifier.size(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    LokacaraMobileTheme {
        SettingsScreen(navController = rememberNavController())
    }
}
