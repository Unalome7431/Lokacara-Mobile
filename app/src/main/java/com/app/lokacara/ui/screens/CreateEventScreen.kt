

package com.app.lokacara.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.DpOffset
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray800
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SvgBackground
import com.app.lokacara.ui.theme.SvgOrange
import com.app.lokacara.ui.theme.SvgPrimaryBlue
import com.app.lokacara.ui.components.MapSearchPicker
import com.app.lokacara.ui.components.MapLocation
import com.app.lokacara.viewmodel.CreateEventViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBack: () -> Unit = {},
    onPublish: () -> Unit = {},
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val namaEvent by viewModel.namaEvent.collectAsState()
    val selectedCategoryName by viewModel.selectedCategoryName.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val penyelenggara by viewModel.penyelenggara.collectAsState()
    val waktuMulai by viewModel.waktuMulai.collectAsState()
    val waktuSelesai by viewModel.waktuSelesai.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val aplikasiTempat by viewModel.aplikasiTempat.collectAsState()
    val alamat by viewModel.alamat.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val deskripsi by viewModel.deskripsi.collectAsState()
    val kuota by viewModel.kuota.collectAsState()
    val posterUri by viewModel.posterUri.collectAsState()
    val publishSuccess by viewModel.publishSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val hasDraft by viewModel.hasDraft.collectAsState()
    val scheduleReady = waktuMulai.isNotBlank() && waktuSelesai.isNotBlank()
    val locationReady = if (isOnline) {
        aplikasiTempat.isNotBlank() && alamat.isNotBlank()
    } else {
        aplikasiTempat.isNotBlank() && alamat.isNotBlank() && latitude.isNotBlank() && longitude.isNotBlank()
    }
    val requiredChecks = listOf(
        namaEvent.isNotBlank(),
        selectedCategoryName.isNotBlank(),
        scheduleReady,
        locationReady,
        deskripsi.isNotBlank(),
        kuota in 1..100_000
    )
    val completedRequirements = requiredChecks.count { it }
    val totalRequirements = requiredChecks.size
    val formProgress = completedRequirements / totalRequirements.toFloat()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.posterUri.value = it } }

    LaunchedEffect(publishSuccess) {
        if (publishSuccess) {
            viewModel.resetPublishSuccess()
            onPublish()
        }
    }

    BackHandler {
        viewModel.saveDraftAndExit(onBack)
    }

    val lightBlueBg = Color(0xFFD6E4FF)
    val darkerBlueBg = Color(0xFFA1C1FF)

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var tempStartDateMillis by remember { mutableLongStateOf(0L) }

    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var tempEndDateMillis by remember { mutableLongStateOf(0L) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        tempStartDateMillis = millis
                        showStartDatePicker = false
                        showStartTimePicker = true
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 12,
            initialMinute = 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Pilih Waktu Mulai", fontFamily = NunitoFont, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tempStartDateMillis)
                    val timeStr = String.format(Locale.US, "%02d:%02d:00", timePickerState.hour, timePickerState.minute)
                    viewModel.setDateTime(isStart = true, date = dateStr, time = timeStr)
                    showStartTimePicker = false
                    tempStartDateMillis = 0L
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false; tempStartDateMillis = 0L }) { Text("Batal") }
            }
        )
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        tempEndDateMillis = millis
                        showEndDatePicker = false
                        showEndTimePicker = true
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 12,
            initialMinute = 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("Pilih Waktu Selesai", fontFamily = NunitoFont, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tempEndDateMillis)
                    val timeStr = String.format(Locale.US, "%02d:%02d:00", timePickerState.hour, timePickerState.minute)
                    viewModel.setDateTime(isStart = false, date = dateStr, time = timeStr)
                    showEndTimePicker = false
                    tempEndDateMillis = 0L
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false; tempEndDateMillis = 0L }) { Text("Batal") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvgBackground)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
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
                    .clickable { viewModel.saveDraftAndExit(onBack) },
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
                modifier = Modifier.clickable { viewModel.saveDraft() }
            )
        }

        if (hasDraft) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SvgOrange.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ada draf tersimpan",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SvgOrange
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Lanjutkan",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SvgPrimaryBlue,
                            modifier = Modifier.clickable { viewModel.loadDraft() }
                        )
                        Text(
                            text = "Hapus",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SemanticErrorBase,
                            modifier = Modifier.clickable { viewModel.deleteDraft() }
                        )
                    }
                }
            }
        }

        EventReadinessCard(
            completed = completedRequirements,
            total = totalRequirements,
            progress = formProgress,
            isOnline = isOnline,
            scheduleReady = scheduleReady,
            locationReady = locationReady
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Poster Event",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gray800
            )
            val posterElevation by animateDpAsState(
                targetValue = if (posterUri != null) 5.dp else 0.dp,
                label = "posterElevation"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .shadow(posterElevation, RoundedCornerShape(20.dp))
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Ganti poster",
                                tint = SvgOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Ganti Poster",
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Gray900
                            )
                        }
                    }
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
                text = "Poster akan dikompresi otomatis. Maksimal 10 MB.",
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
            containerColor = lightBlueBg,
            supportingText = "${namaEvent.length}/255",
            supportingColor = if (namaEvent.length > 255) SemanticErrorBase else Gray500
        )

        CategoryDropdownField(
            selectedCategoryName = selectedCategoryName,
            categories = categories,
            onCategorySelected = { viewModel.selectedCategoryId.value = it.id },
            label = "Kategori",
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
            DatePickerField(
                value = if (waktuMulai.isNotBlank()) viewModel.getDisplayDateTime(waktuMulai) else "",
                onClick = { showStartDatePicker = true },
                label = "Mulai",
                placeholder = "dd MMM yyyy, --:--"
            )
            DatePickerField(
                value = if (waktuSelesai.isNotBlank()) viewModel.getDisplayDateTime(waktuSelesai) else "",
                onClick = { showEndDatePicker = true },
                label = "Selesai",
                placeholder = "dd MMM yyyy, --:--"
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

            SectionContainer(
                title = "Detail Event",
                subtitle = if (isOnline) "Platform dan tautan event" else "Venue dan alamat dari peta",
                backgroundColor = lightBlueBg,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                trailingContent = {
                    CustomToggleSwitch(isOnline = isOnline, onToggle = { viewModel.setEventMode(it) })
                }
            ) {
                if (isOnline) {
                    CreateEventTextField(
                        value = aplikasiTempat,
                        onValueChange = { viewModel.aplikasiTempat.value = it },
                        label = "Aplikasi",
                        placeholder = "nama aplikasi",
                        containerColor = Color.White,
                        labelSize = 14.sp
                    )
                    CreateEventTextField(
                        value = alamat,
                        onValueChange = { viewModel.alamat.value = it },
                        label = "Link",
                        placeholder = "uns.id/ivogamteng",
                        containerColor = Color.White,
                        labelSize = 14.sp
                    )
                } else {
                    MapSearchPicker(
                        selectedLocationName = aplikasiTempat,
                        selectedLocationAddress = alamat,
                        selectedLatitude = latitude.toDoubleOrNull(),
                        selectedLongitude = longitude.toDoubleOrNull(),
                        onLocationSelected = { location ->
                            viewModel.setLocationFromMap(location)
                        }
                    )
                }
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
                            text = "Deskripsikan event Anda secara detail...",
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
                Text(
                    text = "${deskripsi.length}/5000",
                    fontFamily = NunitoFont,
                    fontSize = 11.sp,
                    color = if (deskripsi.length > 5000) SemanticErrorBase else Gray600,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }

        SectionContainer(
            title = "Kuota Peserta",
            backgroundColor = lightBlueBg,
            subtitle = "Batas maksimal pendaftar"
        ) {
            CapacityControl(
                value = kuota,
                onValueChange = { nextValue ->
                    viewModel.kuota.value = nextValue.coerceIn(1, 100_000)
                }
            )
        }

        errorMessage?.let { msg ->
            ErrorMessageBanner(message = msg)
            Spacer(modifier = Modifier.height(12.dp))
        }

        val publishElevation by animateDpAsState(
            targetValue = if (isLoading) 0.dp else 4.dp,
            label = "publishElevation"
        )
        Button(
            onClick = { viewModel.publish() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(publishElevation, RoundedCornerShape(28.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = SvgPrimaryBlue,
                disabledContainerColor = SvgPrimaryBlue.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Menerbitkan...",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            } else {
                Text(
                    text = if (formProgress >= 1f) "Terbitkan Event" else "Cek dan Terbitkan",
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
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DatePickerField(
    value: String,
    onClick: () -> Unit,
    label: String,
    placeholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Gray800
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) Gray500 else Gray900,
                fontSize = 14.sp,
                fontFamily = NunitoFont
            )
        }
    }
}

@Composable
fun EventReadinessCard(
    completed: Int,
    total: Int,
    progress: Float,
    isOnline: Boolean,
    scheduleReady: Boolean,
    locationReady: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kelengkapan Event",
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Gray900
                    )
                    Text(
                        text = "$completed/$total detail wajib terisi",
                        fontFamily = NunitoFont,
                        fontSize = 12.sp,
                        color = Gray600
                    )
                }
                Surface(
                    color = if (completed == total) SvgOrange.copy(alpha = 0.14f) else SvgPrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (completed == total) "Siap" else "${(progress * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (completed == total) SvgOrange else SvgPrimaryBlue
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (completed == total) SvgOrange else SvgPrimaryBlue,
                trackColor = Color(0xFFE7ECF7)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(
                    label = if (isOnline) "Online" else "Offline",
                    isComplete = true,
                    icon = if (isOnline) Icons.Outlined.FileUpload else Icons.Outlined.Place,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = if (scheduleReady) "Jadwal OK" else "Jadwal",
                    isComplete = scheduleReady,
                    icon = Icons.Outlined.DateRange,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = if (locationReady) "Lokasi OK" else "Lokasi",
                    isComplete = locationReady,
                    icon = if (isOnline) Icons.Outlined.FileUpload else Icons.Outlined.MyLocation,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    isComplete: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isComplete) SvgPrimaryBlue.copy(alpha = 0.11f) else Color(0xFFF2F4F8),
        label = "statusPillBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isComplete) SvgPrimaryBlue else Gray600,
        label = "statusPillContent"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 9.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isComplete) Icons.Outlined.CheckCircle else icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ErrorMessageBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SemanticErrorBase.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, SemanticErrorBase.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Error",
                tint = SemanticErrorBase,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SemanticErrorBase,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CapacityControl(value: Int, onValueChange: (Int) -> Unit) {
    var textValue by remember { mutableStateOf(value.toString()) }

    LaunchedEffect(value) {
        val normalized = value.toString()
        if (textValue != normalized) {
            textValue = normalized
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CapacityIconButton(
                icon = Icons.Default.Remove,
                enabled = value > 1,
                onClick = { onValueChange((value - 1).coerceAtLeast(1)) }
            )
            TextField(
                value = textValue,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(6)
                    textValue = digits
                    digits.toIntOrNull()?.let { parsed ->
                        onValueChange(parsed.coerceIn(1, 100_000))
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF6F8FC),
                    unfocusedContainerColor = Color(0xFFF6F8FC),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Gray900,
                    unfocusedTextColor = Gray900
                ),
                shape = RoundedCornerShape(12.dp)
            )
            CapacityIconButton(
                icon = Icons.Default.Add,
                enabled = value < 100_000,
                onClick = { onValueChange((value + 1).coerceAtMost(100_000)) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(25, 50, 100, 250).forEach { preset ->
                val selected = value == preset
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onValueChange(preset) },
                    shape = RoundedCornerShape(50),
                    color = if (selected) SvgPrimaryBlue else Color.White,
                    border = BorderStroke(1.dp, if (selected) SvgPrimaryBlue else Color.White.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = preset.toString(),
                        modifier = Modifier.padding(vertical = 9.dp),
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else Gray800,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CapacityIconButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (enabled) SvgOrange else Gray500.copy(alpha = 0.25f),
        label = "capacityButtonBackground"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CategoryDropdownField(
    selectedCategoryName: String,
    categories: List<CategoryDto>,
    onCategorySelected: (CategoryDto) -> Unit,
    label: String,
    containerColor: Color
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gray800)
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { showDialog = true },
            shape = RoundedCornerShape(16.dp),
            color = containerColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategoryName.ifEmpty { "Pilih Kategori" },
                    color = if (selectedCategoryName.isEmpty()) Gray500 else Gray900,
                    fontSize = 14.sp, fontFamily = NunitoFont
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = SvgOrange, modifier = Modifier.size(24.dp))
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Pilih Kategori", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategoryName == cat.name
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onCategorySelected(cat); showDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Primary500.copy(alpha = 0.1f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.name,
                                    fontFamily = NunitoFont, fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Primary500 else Gray900
                                )
                                if (isSelected) Icon(Icons.Default.Check, "Selected", tint = Primary500, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
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
            .shadow(2.dp, shape)
            .clip(shape)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)), shape)
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
    labelSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    supportingText: String? = null,
    supportingColor: Color = Gray500
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
        if (supportingText != null) {
            Text(
                text = supportingText,
                fontFamily = NunitoFont,
                fontSize = 11.sp,
                color = supportingColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun CustomToggleSwitch(isOnline: Boolean, onToggle: (Boolean) -> Unit) {
    val onlineBackground by animateColorAsState(
        targetValue = if (isOnline) SvgPrimaryBlue else Color.Transparent,
        label = "onlineToggleBackground"
    )
    val offlineBackground by animateColorAsState(
        targetValue = if (!isOnline) SvgPrimaryBlue else Color.Transparent,
        label = "offlineToggleBackground"
    )
    val onlineTextColor by animateColorAsState(
        targetValue = if (isOnline) Color.White else Gray800,
        label = "onlineToggleText"
    )
    val offlineTextColor by animateColorAsState(
        targetValue = if (!isOnline) Color.White else Gray800,
        label = "offlineToggleText"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)), RoundedCornerShape(50))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(onlineBackground)
                .clickable { onToggle(true) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Online",
                color = onlineTextColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(offlineBackground)
                .clickable { onToggle(false) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Offline",
                color = offlineTextColor,
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
            viewModel = hiltViewModel()
        )
    }
}
