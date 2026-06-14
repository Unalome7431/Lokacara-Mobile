package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.R
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SemanticErrorLight
import com.app.lokacara.ui.navigation.Screen
import com.app.lokacara.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsScreen(
    navController: NavController,
    rootNavController: NavController? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val deleteError by viewModel.deleteError.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            viewModel.resetDeleteSuccess()
            scope.launch {
                viewModel.logout()
                (rootNavController ?: navController).navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    if (showDeleteDialog) {
        var password by remember { mutableStateOf("") }

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
                        text = stringResource(R.string.delete_account_password_hint),
                        fontFamily = NunitoFont,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.auth_password_placeholder), fontFamily = NunitoFont) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = NunitoFont)
                    )
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
                    onClick = { viewModel.deleteAccount(password) },
                    enabled = !isDeleting && password.isNotBlank()
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = SemanticErrorBase
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.delete),
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

    ProfilePageScaffold(title = "Pengaturan", onBack = { navController.popBackStack() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_preferences))
            SettingsCard {
                SettingsToggleRow(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.settings_notifications),
                    isChecked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_security))
            SettingsCard {
                SettingsActionRow(
                    icon = Icons.Rounded.Lock,
                    title = stringResource(R.string.settings_change_password),
                    onClick = { navController.navigate(Screen.ChangePassword.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_help_info))
            SettingsCard {
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Rounded.HelpOutline,
                    title = stringResource(R.string.settings_help_center),
                    onClick = { navController.navigate(Screen.HelpCenter.route) }
                )
                HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Rounded.Article,
                    title = stringResource(R.string.settings_terms_conditions),
                    onClick = { navController.navigate(Screen.TermsConditions.route) }
                )
                HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsActionRow(
                    icon = Icons.Rounded.PrivacyTip,
                    title = stringResource(R.string.settings_privacy_policy),
                    onClick = { navController.navigate(Screen.PrivacyPolicy.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_others))
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                viewModel.logout()
                                (rootNavController ?: navController).navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Primary100, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                            contentDescription = "Keluar",
                            tint = Primary500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.profile_logout),
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Gray900
                    )
                }
                HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteDialog = true }
                        .padding(horizontal = 16.dp, vertical = 18.dp),
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.settings_delete_account),
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = SemanticErrorBase
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
fun SettingsToggleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Primary100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Primary500,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Gray900
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary500,
                uncheckedThumbColor = Gray500,
                uncheckedTrackColor = Gray100
            )
        )
    }
}

@Composable
fun SettingsActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Primary100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Primary500,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Gray900
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Navigasi",
            tint = Gray500,
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
