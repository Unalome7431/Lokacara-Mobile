package com.app.lokacara.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InsertPhoto
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.app.lokacara.ui.components.ProfilePageScaffold
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.theme.Gray100
import com.app.lokacara.ui.theme.Gray200
import com.app.lokacara.ui.theme.Gray400
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray600
import com.app.lokacara.ui.theme.Gray700
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.PlusJakartaSansFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SemanticErrorLight
import com.app.lokacara.ui.theme.SemanticInfoBase
import com.app.lokacara.ui.theme.SemanticInfoLight
import com.app.lokacara.ui.theme.SemanticSuccessBase
import com.app.lokacara.ui.theme.SemanticSuccessLight
import com.app.lokacara.ui.theme.SemanticWarningBase
import com.app.lokacara.ui.theme.SemanticWarningLight
import com.app.lokacara.viewmodel.CertificateManagementUiState
import com.app.lokacara.viewmodel.CertificateManagementViewModel
import java.util.Locale

private val fontOptions = listOf("Roboto", "Montserrat", "Playfair", "GreatVibes", "Oswald")
private val fontSizeOptions = listOf("Small", "Medium", "Large")
private val colorOptions = listOf("#000000", "#1E3A8A", "#2563EB", "#B45309", "#DC2626")

@Composable
fun CertificateManagementScreen(
    navController: NavController,
    eventId: Long,
    viewModel: CertificateManagementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfirmation by rememberSaveable { mutableStateOf(false) }
    var colorInput by rememberSaveable { mutableStateOf(state.fontColor) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.selectTemplate(it)
    }

    LaunchedEffect(eventId) { viewModel.initialize(eventId) }
    LaunchedEffect(state.fontColor) { colorInput = state.fontColor }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Kirim Sertifikat", fontFamily = NunitoFont, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Sertifikat akan dibuat dan dikirim kepada ${state.presentAttendeeCount} peserta hadir. Pastikan posisi nama sudah sesuai.",
                    fontFamily = PlusJakartaSansFont,
                    color = Gray600
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmation = false
                    viewModel.distributeCertificates()
                }) {
                    Text("Kirim", color = Primary500, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) { Text("Batal", color = Gray500) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    ProfilePageScaffold(
        title = "Kelola Sertifikat",
        onBack = { navController.navigateBackOrHome() }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { EventEligibilitySection(state) }
            item {
                TemplateSection(
                    state = state,
                    onPick = { picker.launch("image/*") },
                    onUpload = viewModel::uploadTemplate
                )
            }
            if (state.selectedUri != null || state.restoredTemplatePath != null) {
                item { CertificatePreview(state) }
            }
            if (state.distributionStatus != null) item { DistributionSummary(state) }
            item {
                LayoutControls(
                    state = state,
                    colorInput = colorInput,
                    onColorInputChange = { colorInput = it },
                    onApplyColor = { viewModel.setFontColor(colorInput) },
                    onFontFamilyChange = viewModel::setFontFamily,
                    onFontSizeChange = viewModel::setFontSize,
                    onColorChange = viewModel::setFontColor,
                    onXCenteredChange = viewModel::setXCentered,
                    onYCenteredChange = viewModel::setYCentered,
                    onXPositionChange = viewModel::setXPosition,
                    onYPositionChange = viewModel::setYPosition
                )
            }
            state.errorMessage?.let { message ->
                item { StatusMessage(message, isError = true, onDismiss = viewModel::clearMessage) }
            }
            state.successMessage?.let { message ->
                item { StatusMessage(message, isError = false, onDismiss = viewModel::clearMessage) }
            }
            item {
                SendSection(
                    state = state,
                    onSend = { showConfirmation = true }
                )
            }
        }
    }
}

@Composable
private fun EventEligibilitySection(state: CertificateManagementUiState) {
    SectionSurface {
        Text(
            state.eventTitle.ifBlank { "Memuat informasi event..." },
            fontFamily = NunitoFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = Gray900,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(12.dp))
        if (state.isLoadingEligibility) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Primary500, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Memeriksa kelayakan pengiriman...", fontFamily = PlusJakartaSansFont, color = Gray600)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EligibilityChip(
                    label = if (state.isEventFinished) "Event selesai" else "Event belum selesai",
                    positive = state.isEventFinished,
                    modifier = Modifier.weight(1f)
                )
                EligibilityChip(
                    label = "${state.presentAttendeeCount} peserta hadir",
                    positive = state.presentAttendeeCount > 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EligibilityChip(label: String, positive: Boolean, modifier: Modifier = Modifier) {
    val background = if (positive) SemanticSuccessLight else SemanticWarningLight
    val foreground = if (positive) SemanticSuccessBase else Color(0xFF9A6700)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            if (positive) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(17.dp)
        )
        Text(label, fontFamily = PlusJakartaSansFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = foreground)
    }
}

@Composable
private fun TemplateSection(
    state: CertificateManagementUiState,
    onPick: () -> Unit,
    onUpload: () -> Unit
) {
    SectionSurface {
        SectionTitle("Template Sertifikat", Icons.Outlined.InsertPhoto)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onPick,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray200)
        ) {
            Icon(Icons.Outlined.InsertPhoto, contentDescription = null, tint = Primary500)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    state.selectedFileName.ifBlank { "Pilih file template" },
                    fontFamily = PlusJakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (state.selectedFileSize > 0) formatFileSize(state.selectedFileSize) else "JPG, JPEG, atau PNG, maksimal 5 MB",
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 11.sp,
                    color = Gray500
                )
            }
            Text(if (state.selectedUri == null) "Pilih" else "Ganti", color = Primary500, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onUpload,
            enabled = state.canUpload,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
        ) {
            if (state.isUploading) {
                CircularProgressIndicator(Modifier.size(19.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Outlined.CloudUpload, contentDescription = null, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                if (state.templatePath == null && state.restoredTemplatePath == null) "Unggah Template" else "Unggah Ulang Template",
                fontFamily = PlusJakartaSansFont,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CertificatePreview(state: CertificateManagementUiState) {
    val context = LocalContext.current
    val previewSource = state.selectedUri ?: state.restoredTemplatePath
    val previewRequest = remember(context, previewSource) {
        ImageRequest.Builder(context)
            .data(previewSource)
            .size(1400)
            .precision(Precision.INEXACT)
            .crossfade(false)
            .build()
    }
    SectionSurface {
        SectionTitle("Pratinjau", Icons.Outlined.Info)
        Spacer(Modifier.height(12.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(Gray100)
                .border(1.dp, Gray200, RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = previewRequest,
                contentDescription = "Pratinjau template sertifikat",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            val xOffset = if (state.isXCentered) 0.dp else maxWidth * ((state.xPosition - 50f) / 100f)
            val yOffset = if (state.isYCentered) 0.dp else maxHeight * ((state.yPosition - 50f) / 100f)
            Text(
                text = "Nama Peserta",
                modifier = Modifier.align(Alignment.Center).offset(x = xOffset, y = yOffset),
                color = parseColor(state.fontColor),
                fontFamily = previewFontFamily(state.fontFamily),
                fontSize = when (state.fontSize) {
                    "Small" -> 14.sp
                    "Large" -> 26.sp
                    else -> 20.sp
                },
                fontWeight = if (state.fontFamily == "GreatVibes") FontWeight.Normal else FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Pratinjau dibuat di perangkat. Hasil akhir dapat memiliki sedikit perbedaan pada bentuk font.",
            fontFamily = PlusJakartaSansFont,
            fontSize = 11.sp,
            color = Gray500
        )
    }
}

@Composable
private fun DistributionSummary(state: CertificateManagementUiState) {
    SectionSurface {
        SectionTitle("Status Pengiriman", Icons.Outlined.CheckCircle)
        Spacer(Modifier.height(10.dp))
        Text(
            when (state.distributionStatus) {
                "processing" -> "Sertifikat sedang dibuat dan didistribusikan."
                "distributed" -> "Sertifikat sebelumnya sudah didistribusikan."
                else -> "Konfigurasi sertifikat sebelumnya tersedia."
            },
            fontFamily = PlusJakartaSansFont,
            color = Gray700
        )
        if (state.isLocalFallback) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Data dipulihkan dari perangkat ini.",
                fontFamily = PlusJakartaSansFont,
                fontSize = 11.sp,
                color = Gray500
            )
        }
    }
}

@Composable
private fun LayoutControls(
    state: CertificateManagementUiState,
    colorInput: String,
    onColorInputChange: (String) -> Unit,
    onApplyColor: () -> Unit,
    onFontFamilyChange: (String) -> Unit,
    onFontSizeChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onXCenteredChange: (Boolean) -> Unit,
    onYCenteredChange: (Boolean) -> Unit,
    onXPositionChange: (Float) -> Unit,
    onYPositionChange: (Float) -> Unit
) {
    SectionSurface {
        Text("Pengaturan Nama Peserta", fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Gray900)
        Spacer(Modifier.height(16.dp))
        ControlLabel("Jenis font")
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fontOptions.forEach { font ->
                FilterChip(selected = state.fontFamily == font, onClick = { onFontFamilyChange(font) }, label = { Text(font) })
            }
        }
        Spacer(Modifier.height(14.dp))
        ControlLabel("Ukuran font")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            fontSizeOptions.forEach { size ->
                FilterChip(
                    selected = state.fontSize == size,
                    onClick = { onFontSizeChange(size) },
                    label = { Text(sizeLabel(size)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        ControlLabel("Warna font")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            colorOptions.forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(parseColor(hex))
                        .border(if (state.fontColor == hex) 3.dp else 1.dp, if (state.fontColor == hex) Primary100 else Gray200, CircleShape)
                        .clickable { onColorChange(hex) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = colorInput,
                onValueChange = { if (it.length <= 7) onColorInputChange(it) },
                modifier = Modifier.weight(1f),
                label = { Text("Kode warna") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedButton(onClick = onApplyColor, shape = RoundedCornerShape(12.dp), modifier = Modifier.height(56.dp)) {
                Text("Terapkan")
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Gray100)
        PositionControl("Posisi horizontal", state.isXCentered, state.xPosition, onXCenteredChange, onXPositionChange)
        Spacer(Modifier.height(16.dp))
        PositionControl("Posisi vertikal", state.isYCentered, state.yPosition, onYCenteredChange, onYPositionChange)
    }
}

@Composable
private fun PositionControl(
    title: String,
    centered: Boolean,
    position: Float,
    onCenteredChange: (Boolean) -> Unit,
    onPositionChange: (Float) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            ControlLabel(title)
            Text(if (centered) "Rata tengah" else "${position.toInt()}%", fontFamily = PlusJakartaSansFont, fontSize = 11.sp, color = Gray500)
        }
        Text("Tengah", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray600)
        Spacer(Modifier.width(6.dp))
        Switch(checked = centered, onCheckedChange = onCenteredChange)
    }
    Slider(value = position, onValueChange = onPositionChange, valueRange = 0f..100f, enabled = !centered)
}

@Composable
private fun StatusMessage(message: String, isError: Boolean, onDismiss: () -> Unit) {
    val background = if (isError) SemanticErrorLight else SemanticSuccessLight
    val foreground = if (isError) SemanticErrorBase else Color(0xFF4D7C0F)
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(background).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(if (isError) Icons.Outlined.WarningAmber else Icons.Outlined.CheckCircle, null, tint = foreground)
        Text(message, Modifier.weight(1f), fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = foreground)
        Text("Tutup", Modifier.clickable(onClick = onDismiss), fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = foreground)
    }
}

@Composable
private fun SendSection(state: CertificateManagementUiState, onSend: () -> Unit) {
    val explanation = when {
        state.templatePath.isNullOrBlank() && state.restoredTemplatePath.isNullOrBlank() -> "Unggah template untuk melanjutkan."
        state.templatePath.isNullOrBlank() && state.restoredTemplatePath != null -> "Template lokal sudah dipulihkan, unggah ulang untuk mengirim sertifikat."
        !state.isEventFinished -> "Pengiriman tersedia setelah event selesai."
        state.presentAttendeeCount <= 0 -> "Belum ada peserta hadir yang dapat menerima sertifikat."
        else -> "Sertifikat akan dikirim kepada ${state.presentAttendeeCount} peserta hadir."
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(SemanticInfoLight).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Info, null, tint = SemanticInfoBase, modifier = Modifier.size(18.dp))
            Text(explanation, fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray600)
        }
        Button(
            onClick = onSend,
            enabled = state.canDistribute,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500, disabledContainerColor = Gray200)
        ) {
            if (state.isDistributing) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            else Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Kirim Sertifikat", fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionSurface(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = Primary500, modifier = Modifier.size(20.dp))
        Text(title, fontFamily = NunitoFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Gray900)
    }
}

@Composable
private fun ControlLabel(value: String) {
    Text(value, fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Gray900)
}

private fun sizeLabel(value: String): String = when (value) {
    "Small" -> "Kecil"
    "Large" -> "Besar"
    else -> "Sedang"
}

private fun previewFontFamily(value: String): FontFamily = when (value) {
    "Playfair" -> FontFamily.Serif
    "GreatVibes" -> FontFamily.Cursive
    "Oswald" -> FontFamily.Monospace
    else -> FontFamily.SansSerif
}

private fun parseColor(value: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(value))
}.getOrDefault(Color.Black)

private fun formatFileSize(bytes: Long): String = String.format(Locale.US, "%.2f MB", bytes / (1024f * 1024f))
