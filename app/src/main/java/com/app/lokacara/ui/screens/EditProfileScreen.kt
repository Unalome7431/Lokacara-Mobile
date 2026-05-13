package com.app.lokacara.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.app.lokacara.R
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var editFieldLabel by remember { mutableStateOf("") }
    var editFieldValue by remember { mutableStateOf("") }
    var editKeyboardType by remember { mutableStateOf(KeyboardType.Text) }

    // State for photo picker
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) profileImageUri = uri }
    )

    val scrollState = rememberScrollState()

    if (showDialog) {
        EditFieldDialog(
            label = editFieldLabel,
            initialValue = editFieldValue,
            keyboardType = editKeyboardType,
            onDismiss = { showDialog = false },
            onSave = { newValue ->
                viewModel.updateProfileField(editFieldLabel, newValue)
                showDialog = false
            }
        )
    }

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
                text = "Edit Profil",
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
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Picture with Camera Icon
            Box(contentAlignment = Alignment.BottomEnd) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                    )
                } else {
                    Image(
                        painter = painterResource(id = userProfile.profileImageRes ?: R.drawable.profileicon),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .clickable {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Edit Photo",
                        tint = Gray600,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name with Edit Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userProfile.name,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Gray900
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit Name",
                    tint = Gray600,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            editFieldLabel = "Nama Lengkap"
                            editFieldValue = userProfile.name
                            editKeyboardType = KeyboardType.Text
                            showDialog = true
                        }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    ProfileDetailRow(label = "Email", value = userProfile.email, onClick = {
                        editFieldLabel = "Email"
                        editFieldValue = userProfile.email
                        editKeyboardType = KeyboardType.Email
                        showDialog = true
                    })
                    HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileDetailRow(label = "Nomor", value = userProfile.phone, onClick = {
                        editFieldLabel = "Nomor"
                        editFieldValue = userProfile.phone
                        editKeyboardType = KeyboardType.Phone
                        showDialog = true
                    })
                    HorizontalDivider(color = Gray100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileDetailRow(label = "Lokasi", value = userProfile.location, onClick = {
                        editFieldLabel = "Lokasi"
                        editFieldValue = userProfile.location
                        editKeyboardType = KeyboardType.Text
                        showDialog = true
                    })
                }
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
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Gray500
        )
        Text(
            text = value,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Gray900
        )
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
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary500,
                    unfocusedBorderColor = Gray300,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Gray900,
                    unfocusedTextColor = Gray900
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
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
