package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.app.lokacara.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lokacara.viewmodel.CreateEventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBack: () -> Unit = {},
    onPublish: () -> Unit = {},
    viewModel: CreateEventViewModel = viewModel()
) {
    val namaEvent by viewModel.namaEvent.collectAsState()
    val kategori by viewModel.kategori.collectAsState()
    val penyelenggara by viewModel.penyelenggara.collectAsState()
    val waktuMulai by viewModel.waktuMulai.collectAsState()
    val waktuSelesai by viewModel.waktuSelesai.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val aplikasiTempat by viewModel.aplikasiTempat.collectAsState()
    val alamat by viewModel.alamat.collectAsState()
    val deskripsi by viewModel.deskripsi.collectAsState()
    val kuota by viewModel.kuota.collectAsState()
    val posterUri by viewModel.posterUri.collectAsState()
    val publishSuccess by viewModel.publishSuccess.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.posterUri.value = it } }
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(publishSuccess) {
        if (publishSuccess) {
            viewModel.resetPublishSuccess()
            onPublish()
        }
    }

    val lightBlueBg = Color(0xFFD6E4FF)
    val darkerBlueBg = Color(0xFFA1C1FF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvgBackground)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() },
                tint = Gray900
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Buat Event Baru",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Simpan Draf",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SvgOrange,
                modifier = Modifier.clickable { }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Poster Event",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray800
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .then(
                        if (posterUri == null) Modifier.drawBehind {
                            drawRoundRect(
                                color = Color(0xFF666666),
                                style = Stroke(
                                    width = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                                ),
                                cornerRadius = CornerRadius(20.dp.toPx())
                            )
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (posterUri != null) {
                    AsyncImage(
                        model = posterUri,
                        contentDescription = "Poster",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Camera",
                                tint = SvgOrange,
                                modifier = Modifier.size(32.dp)
                            )
                            Icon(
                                imageVector = Icons.Outlined.AddCircleOutline,
                                contentDescription = "Add",
                                tint = SvgOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "Unggah Poster (16:9)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray800
                        )
                    }
                }
            }
            Text(
                text = "ukuran maksimal 5mb, png jpg svg",
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        CreateEventTextField(
            value = namaEvent,
            onValueChange = { viewModel.namaEvent.value = it },
            label = "Nama Event",
            placeholder = "Nama Event",
            containerColor = lightBlueBg
        )

        CreateEventTextField(
            value = kategori,
            onValueChange = { viewModel.kategori.value = it },
            label = "Kategori",
            placeholder = "Kategori",
            containerColor = lightBlueBg
        )

        CreateEventTextField(
            value = penyelenggara,
            onValueChange = { viewModel.penyelenggara.value = it },
            label = "Penyelenggara",
            placeholder = "Nama organisasi / EO",
            containerColor = lightBlueBg
        )

        SectionContainer(
            title = "Waktu dan Tanggal",
            backgroundColor = lightBlueBg,
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = "Kalender",
                    tint = SvgOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
        ) {
            CreateEventTextField(
                value = waktuMulai,
                onValueChange = { viewModel.waktuMulai.value = it },
                label = "Mulai",
                placeholder = "mm/dd/yyyy, --:--",
                containerColor = Color.White,
                labelSize = 14.sp
            )
            CreateEventTextField(
                value = waktuSelesai,
                onValueChange = { viewModel.waktuSelesai.value = it },
                label = "Selesai",
                placeholder = "mm/dd/yyyy, --:--",
                containerColor = Color.White,
                labelSize = 14.sp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

            SectionContainer(
                title = "Detail Event",
                backgroundColor = lightBlueBg,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                trailingContent = {
                    CustomToggleSwitch(isOnline = isOnline, onToggle = { viewModel.isOnline.value = it })
                }
            ) {
                CreateEventTextField(
                    value = aplikasiTempat,
                    onValueChange = { viewModel.aplikasiTempat.value = it },
                    label = if (isOnline) "Aplikasi" else "Tempat",
                    placeholder = if (isOnline) "nama aplikasi" else "nama tempat",
                    containerColor = Color.White,
                    labelSize = 14.sp
                )
                CreateEventTextField(
                    value = alamat,
                    onValueChange = { viewModel.alamat.value = it },
                    label = "Alamat/Link",
                    placeholder = "uns.id/ivogamteng",
                    containerColor = Color.White,
                    labelSize = 14.sp
                )
            }

            SectionContainer(
                title = "Deskripsi Event",
                backgroundColor = darkerBlueBg,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
            ) {
                TextField(
                    value = deskripsi,
                    onValueChange = { viewModel.deskripsi.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = {
                        Text(
                            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in Lorem ipsum dolor sit amet, consectetur adipiscing elit. Lorem ipsum dolor sit amet,",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = Gray500
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Gray900,
                        unfocusedTextColor = Gray900
                    ),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }

        SectionContainer(
            title = "Kuota Peserta",
            backgroundColor = lightBlueBg,
            subtitle = "Batas maksimal pendaftar",
            trailingContent = {
                Stepper(value = kuota, onValueChange = { viewModel.kuota.value = it })
            }
        )

        errorMessage?.let { msg ->
            Text(
                text = msg,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SemanticErrorBase,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { viewModel.publish() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SvgPrimaryBlue),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Terbitkan Event",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.FileUpload,
                contentDescription = "Publish",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SectionContainer(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFD6E4FF),
    shape: Shape = RoundedCornerShape(20.dp),
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Gray800
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray600
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
        if (content != null) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    containerColor: Color = Color(0xFFD6E4FF),
    labelSize: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = labelSize,
                color = Gray800
            )
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = Gray500
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                disabledContainerColor = containerColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Gray900,
                unfocusedTextColor = Gray900
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CustomToggleSwitch(isOnline: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isOnline) SvgPrimaryBlue else Color.Transparent)
                .clickable { onToggle(true) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Online",
                color = if (isOnline) Color.White else Gray800,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (!isOnline) SvgPrimaryBlue else Color.Transparent)
                .clickable { onToggle(false) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Offline",
                color = if (!isOnline) Color.White else Gray800,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun Stepper(value: Int, onValueChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(SvgOrange, CircleShape)
                .clickable { if (value > 0) onValueChange(value - 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.offset(y = (-2).dp))
        }
        Text(
            text = value.toString(),
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Gray900
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(SvgPrimaryBlue, CircleShape)
                .clickable { onValueChange(value + 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.offset(y = (-1).dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CreateEventScreenPreview() {
    com.app.lokacara.ui.theme.LokacaraMobileTheme {
        CreateEventScreen(
            onBack = {},
            onPublish = {},
            viewModel = viewModel()
        )
    }
}