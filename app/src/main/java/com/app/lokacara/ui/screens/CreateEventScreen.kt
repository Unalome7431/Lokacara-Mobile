package com.app.lokacara.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.app.lokacara.ui.theme.CreateEventDarkerBlue
import com.app.lokacara.ui.theme.CreateEventDashedBorder
import com.app.lokacara.ui.theme.CreateEventLightBlue
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray800
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SvgBackground
import com.app.lokacara.ui.theme.SvgOrange
import com.app.lokacara.ui.theme.SvgPrimaryBlue
import com.app.lokacara.ui.components.MapSearchPicker
import com.app.lokacara.ui.components.createevent.*
import com.app.lokacara.viewmodel.CreateEventViewModel
import com.app.lokacara.data.completedEventRequirements
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    eventId: Long? = null,
    onBack: () -> Unit = {},
    onPublish: () -> Unit = {},
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val isEditMode = eventId != null && eventId > 0L

    val namaEvent by viewModel.namaEvent.collectAsStateWithLifecycle()
    val selectedCategoryName by viewModel.selectedCategoryName.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val penyelenggara by viewModel.penyelenggara.collectAsStateWithLifecycle()
    val waktuMulai by viewModel.waktuMulai.collectAsStateWithLifecycle()
    val waktuSelesai by viewModel.waktuSelesai.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isFreePrice by viewModel.isFreePrice.collectAsStateWithLifecycle()
    val priceAmount by viewModel.priceAmount.collectAsStateWithLifecycle()
    val aplikasiTempat by viewModel.aplikasiTempat.collectAsStateWithLifecycle()
    val alamat by viewModel.alamat.collectAsStateWithLifecycle()
    val latitude by viewModel.latitude.collectAsStateWithLifecycle()
    val longitude by viewModel.longitude.collectAsStateWithLifecycle()
    val deskripsi by viewModel.deskripsi.collectAsStateWithLifecycle()
    val kuota by viewModel.kuota.collectAsStateWithLifecycle()
    val posterUri by viewModel.posterUri.collectAsStateWithLifecycle()
    val publishSuccess by viewModel.publishSuccess.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val hasDraft by viewModel.hasDraft.collectAsStateWithLifecycle()
    val scheduleReady = waktuMulai.isNotBlank() && waktuSelesai.isNotBlank()
    val locationReady = if (isOnline) {
        aplikasiTempat.isNotBlank() && alamat.isNotBlank()
    } else {
        aplikasiTempat.isNotBlank() && alamat.isNotBlank() && latitude.isNotBlank() && longitude.isNotBlank()
    }
    val priceReady = isFreePrice || priceAmount.isNotBlank()
    val completedRequirements = completedEventRequirements(
        hasName = namaEvent.isNotBlank(),
        hasCategory = selectedCategoryName.isNotBlank(),
        hasSchedule = scheduleReady,
        hasLocation = locationReady,
        hasDescription = deskripsi.isNotBlank(),
        hasPrice = priceReady,
        hasValidCapacity = kuota in 1..100_000
    )
    val totalRequirements = 7
    val formProgress = completedRequirements / totalRequirements.toFloat()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.posterUri.value = it } }

    LaunchedEffect(eventId) {
        if (isEditMode) {
            viewModel.loadEventForEditing(eventId!!)
        }
    }

    LaunchedEffect(publishSuccess) {
        if (publishSuccess) {
            viewModel.resetPublishSuccess()
            onPublish()
        }
    }

    BackHandler {
        if (isEditMode) {
            onBack()
        } else {
            viewModel.saveDraftAndExit(onBack)
        }
    }

    val lightBlueBg = CreateEventLightBlue
    val darkerBlueBg = CreateEventDarkerBlue
    val context = LocalContext.current
    val posterRequest = remember(context, posterUri) {
        ImageRequest.Builder(context)
            .data(posterUri)
            .size(1200)
            .precision(Precision.INEXACT)
            .crossfade(false)
            .build()
    }
    val posterOverlayBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
        )
    }

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
            .padding(start = 24.dp, end = 24.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isEditMode) onBack() else viewModel.saveDraftAndExit(onBack)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isEditMode) Icons.Rounded.ArrowBackIosNew else Icons.Default.Close,
                    contentDescription = if (isEditMode) "Kembali" else "Tutup",
                    modifier = Modifier.size(24.dp),
                    tint = Gray900
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isEditMode) "Edit Detail Acara" else "Buat Event Baru",
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Gray900
            )
            Spacer(modifier = Modifier.weight(1f))
            if (!isEditMode) {
                Text(
                    text = "Simpan Draf",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SvgOrange,
                    modifier = Modifier.clickable { viewModel.saveDraft() }
                )
            }
        }

        if (hasDraft && !isEditMode) {
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
            progress = formProgress
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
        item(key = "poster", contentType = "media") {
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
                                color = CreateEventDashedBorder,
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
                        model = posterRequest,
                        contentDescription = "Poster",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(posterOverlayBrush)
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
        }

        item(key = "basic_information", contentType = "form") {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        CreateEventTextField(
            value = namaEvent,
            onValueChange = { viewModel.namaEvent.value = it },
            label = "Nama Event",
            placeholder = "Masukkan nama acara",
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
            placeholder = "Masukkan nama penyelenggara atau organisasi",
            containerColor = lightBlueBg
        )
        }
        }

        item(key = "schedule", contentType = "form") {
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
                placeholder = "Pilih tanggal dan waktu mulai"
            )
            DatePickerField(
                value = if (waktuSelesai.isNotBlank()) viewModel.getDisplayDateTime(waktuSelesai) else "",
                onClick = { showEndDatePicker = true },
                label = "Selesai",
                placeholder = "Pilih tanggal dan waktu selesai"
            )
        }
        }

        item(key = "price", contentType = "form") {
        SectionContainer(
            title = "Harga Event",
            subtitle = "Gratis atau berbayar",
            backgroundColor = darkerBlueBg
        ) {
            PriceSection(
                isFree = isFreePrice,
                onToggleFree = { viewModel.setPriceMode(it) },
                priceAmount = priceAmount,
                onPriceAmountChange = { viewModel.updatePriceAmount(it) }
            )
        }
        }

        item(key = "event_details", contentType = "form") {
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
                        placeholder = "Masukkan nama platform atau aplikasi",
                        containerColor = Color.White,
                        labelSize = 14.sp
                    )
                    CreateEventTextField(
                        value = alamat,
                        onValueChange = { viewModel.alamat.value = it },
                        label = "Link",
                        placeholder = "Masukkan tautan acara daring",
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
                            text = "Tuliskan deskripsi acara secara lengkap",
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
        }

        item(key = "capacity", contentType = "form") {
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
        }

        if (errorMessage != null) {
            item(key = "error", contentType = "error") {
                ErrorMessageBanner(message = errorMessage.orEmpty())
            }
        }

        item(key = "publish", contentType = "action") {
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
                    text = if (isEditMode) "Menyimpan..." else "Menerbitkan...",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            } else {
                Text(
                    text = when {
                        isEditMode -> "Simpan Perubahan"
                        else -> "Terbitkan"
                    },
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                if (!isEditMode) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = "Publish",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        }

        item(key = "bottom_spacer", contentType = "spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}
