package com.app.lokacara.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.ui.components.LokacaraTextField
import com.app.lokacara.ui.components.ProfileAvatarImage
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.theme.Gray300
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.LokacaraMobileTheme
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.Secondary100
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editField by remember { mutableStateOf<UserSessionManager.Field?>(null) }
    var editFieldValue by remember { mutableStateOf("") }
    var editKeyboardType by remember { mutableStateOf(KeyboardType.Text) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var previousProfileUrl by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                profileImageUri = uri
                previousProfileUrl = userProfile.profileImageUrl
                viewModel.saveProfilePhoto(uri)
            }
        }
    )

    LaunchedEffect(userProfile.profileImageUrl) {
        if (
            profileImageUri != null &&
            userProfile.profileImageUrl != null &&
            userProfile.profileImageUrl != previousProfileUrl
        ) {
            profileImageUri = null
        }
    }

    if (showDialog) {
        EditFieldDialog(
            label = editField?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
            initialValue = editFieldValue,
            keyboardType = editKeyboardType,
            onDismiss = { showDialog = false },
            onSave = { newValue ->
                editField?.let { field -> viewModel.updateProfileField(field, newValue) }
                showDialog = false
            }
        )
    }

    ProfilePageScaffold(
        title = "Edit Profil",
        onBack = { navController.navigateBackOrHome() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                val avatarModifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }

                ProfileAvatarImage(
                    imageModel = profileImageUri ?: userProfile.profileImageUrl,
                    modifier = avatarModifier
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Primary500, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Edit Photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(1.dp, Gray100, RoundedCornerShape(22.dp))
                    .padding(vertical = 8.dp)
            ) {
                ProfileDetailRow(
                    label = "Nama",
                    value = userProfile.name,
                    onClick = {
                        editField = UserSessionManager.Field.NAME
                        editFieldValue = userProfile.name
                        editKeyboardType = KeyboardType.Text
                        showDialog = true
                    }
                )
                HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                ProfileDetailRow(
                    label = "Email",
                    value = userProfile.email,
                    onClick = {
                        editField = UserSessionManager.Field.EMAIL
                        editFieldValue = userProfile.email
                        editKeyboardType = KeyboardType.Email
                        showDialog = true
                    }
                )
                HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                ProfileDetailRow(
                    label = "Nomor",
                    value = userProfile.phone,
                    onClick = {
                        editField = UserSessionManager.Field.PHONE
                        editFieldValue = userProfile.phone
                        editKeyboardType = KeyboardType.Phone
                        showDialog = true
                    }
                )
                HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                ProfileDetailRow(
                    label = "Lokasi",
                    value = userProfile.location,
                    onClick = {
                        editField = UserSessionManager.Field.LOCATION
                        editFieldValue = userProfile.location
                        editKeyboardType = KeyboardType.Text
                        showDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String, onClick: () -> Unit) {
    Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Gray600
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifBlank { "-" },
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null,
                tint = Primary500,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun EditFieldDialog(
    label: String,
    initialValue: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit $label",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray900
            )
        },
        text = {
            LokacaraTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Masukkan $label",
                isOutlined = true,
                keyboardType = keyboardType,
                shape = RoundedCornerShape(12.dp),
                containerColor = Color.White
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text(
                    text = "Simpan",
                    color = Primary500,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = Gray500,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    LokacaraMobileTheme {
        EditProfileScreen(navController = rememberNavController())
    }
}
