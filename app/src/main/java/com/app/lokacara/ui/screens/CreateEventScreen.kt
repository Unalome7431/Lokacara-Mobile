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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(onBack: () -> Unit = {}) {
    val lightBlueBg = Color(0xFFD6E4FF)
    val darkerBlueBg = Color(0xFFA1C1FF)
    val primaryOrange = Color(0xFFFFAA00)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8FF))
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Header
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
                color = primaryOrange,
                modifier = Modifier.clickable { /* TODO: Aksi Simpan Draf */ }
            )
        }

        // 2. Poster Event
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
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0xFF666666),
                            style = Stroke(
                                width = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                            ),
                            cornerRadius = CornerRadius(20.dp.toPx())
                        )
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { /* TODO: Aksi Unggah Poster */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Camera",
                            tint = primaryOrange,
                            modifier = Modifier.size(32.dp)
                        )
                        Icon(
                            imageVector = Icons.Outlined.AddCircleOutline,
                            contentDescription = "Add",
                            tint = primaryOrange,
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
            Text(
                text = "ukuran maksimal 5mb, png jpg svg",
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // 3. Rentetan Input Teks Dasar
        var namaEvent by remember { mutableStateOf("") }
        CreateEventTextField(
            value = namaEvent,
            onValueChange = { namaEvent = it },
            label = "Nama Event",
            placeholder = "Nama Event",
            containerColor = lightBlueBg
        )

        var kategori by remember { mutableStateOf("") }
        CreateEventTextField(
            value = kategori,
            onValueChange = { kategori = it },
            label = "Kategori",
            placeholder = "Kategori",
            containerColor = lightBlueBg
        )

        var penyelenggara by remember { mutableStateOf("") }
        CreateEventTextField(
            value = penyelenggara,
            onValueChange = { penyelenggara = it },
            label = "Penyelenggara",
            placeholder = "Nama organisasi / EO",
            containerColor = lightBlueBg
        )

        // 4. Waktu dan Tanggal
        SectionContainer(
            title = "Waktu dan Tanggal",
            backgroundColor = lightBlueBg,
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = "Kalender",
                    tint = primaryOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
        ) {
            var waktuMulai by remember { mutableStateOf("") }
            var waktuSelesai by remember { mutableStateOf("") }

            CreateEventTextField(
                value = waktuMulai,
                onValueChange = { waktuMulai = it },
                label = "Mulai",
                placeholder = "mm/dd/yyyy, --:--",
                containerColor = Color.White,
                labelSize = 14.sp
            )
            CreateEventTextField(
                value = waktuSelesai,
                onValueChange = { waktuSelesai = it },
                label = "Selesai",
                placeholder = "mm/dd/yyyy, --:--",
                containerColor = Color.White,
                labelSize = 14.sp
            )
        }

        // 5 & 6. Detail Event & Deskripsi Event Connected
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            var isOnline by remember { mutableStateOf(true) }

            // 5. Detail Event
            SectionContainer(
                title = "Detail Event",
                backgroundColor = lightBlueBg,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                trailingContent = {
                    CustomToggleSwitch(isOnline = isOnline, onToggle = { isOnline = it })
                }
            ) {
                var aplikasiTempat by remember { mutableStateOf("") }
                var alamat by remember { mutableStateOf("") }

                CreateEventTextField(
                    value = aplikasiTempat,
                    onValueChange = { aplikasiTempat = it },
                    label = if (isOnline) "Aplikasi" else "Tempat",
                    placeholder = if (isOnline) "nama aplikasi" else "nama tempat",
                    containerColor = Color.White,
                    labelSize = 14.sp
                )
                CreateEventTextField(
                    value = alamat,
                    onValueChange = { alamat = it },
                    label = "Alamat/Link",
                    placeholder = "uns.id/ivogamteng",
                    containerColor = Color.White,
                    labelSize = 14.sp
                )
            }

            // 6. Deskripsi Event
            SectionContainer(
                title = "Deskripsi Event",
                backgroundColor = darkerBlueBg,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
            ) {
                var deskripsi by remember { mutableStateOf("") }
                TextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = {
                        Text(
                            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in Lorem ipsum dolor sit amet, consectetur adipiscing elit. Lorem ipsum dolor sit amet,",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            color = Gray500
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 7. Kuota Peserta
        SectionContainer(
            title = "Kuota Peserta",
            backgroundColor = lightBlueBg,
            subtitle = "Batas maksimal pendaftar",
            trailingContent = {
                var kuota by remember { mutableIntStateOf(50) }
                Stepper(value = kuota, onValueChange = { kuota = it })
            }
        )

        // 8. Tombol Terbitkan Event
        Button(
            onClick = { /* TODO: Aksi Terbitkan Event */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D59F2)),
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
                    fontSize = 14.sp,
                    color = Gray500
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                disabledContainerColor = containerColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
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
                .background(if (isOnline) Color(0xFF0D59F2) else Color.Transparent)
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
                .background(if (!isOnline) Color(0xFF0D59F2) else Color.Transparent)
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
                .background(Color(0xFFFFAA00), CircleShape)
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
                .background(Color(0xFF0D59F2), CircleShape)
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
        CreateEventScreen()
    }
}