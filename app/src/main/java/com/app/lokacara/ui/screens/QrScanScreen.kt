package com.app.lokacara.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.lokacara.R
import com.app.lokacara.data.remote.dto.ScanResponse
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
import com.app.lokacara.ui.theme.Secondary100
import com.app.lokacara.ui.theme.Secondary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.ui.theme.SemanticErrorLight
import com.app.lokacara.ui.theme.SemanticSuccessBase
import com.app.lokacara.ui.theme.SemanticSuccessLight
import com.app.lokacara.ui.theme.SvgBackground
import com.app.lokacara.viewmodel.QrScanViewModel
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

private enum class QrMode(val label: String) {
    CAMERA("Kamera"),
    MANUAL("Manual")
}

@Composable
fun QrScanScreen(
    navController: NavController,
    eventId: Long,
    viewModel: QrScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val qrToken by viewModel.qrToken.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val result by viewModel.result.collectAsState()
    val error by viewModel.error.collectAsState()

    var mode by remember { mutableStateOf(QrMode.CAMERA) }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var lastScannedToken by remember { mutableStateOf<String?>(null) }
    var lastScanAt by remember { mutableLongStateOf(0L) }

    var showSuccessOverlay by remember { mutableStateOf(false) }
    var showErrorOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        if (result != null && mode == QrMode.CAMERA) {
            showSuccessOverlay = true
            kotlinx.coroutines.delay(1500)
            showSuccessOverlay = false
        }
    }

    LaunchedEffect(error) {
        if (error != null && mode == QrMode.CAMERA) {
            showErrorOverlay = true
            kotlinx.coroutines.delay(1500)
            showErrorOverlay = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        if (!granted) mode = QrMode.MANUAL
    }

    LaunchedEffect(eventId) {
        viewModel.setEventId(eventId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SvgBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        QrTopBar(onBack = { navController.navigateBackOrHome() })

        QrModeSelector(
            selectedMode = mode,
            onModeSelected = {
                mode = it
                viewModel.clearError()
                showSuccessOverlay = false
                showErrorOverlay = false
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (mode == QrMode.CAMERA) {
            if (hasCameraPermission) {
                QrCameraScanner(
                    isProcessing = isLoading,
                    onQrFound = { token ->
                        val now = SystemClock.elapsedRealtime()
                        val canScan = token != lastScannedToken || now - lastScanAt > 2_500L
                        if (canScan) {
                            lastScannedToken = token
                            lastScanAt = now
                            viewModel.scan(token)
                        }
                    },
                    showSuccessOverlay = showSuccessOverlay,
                    showErrorOverlay = showErrorOverlay
                )
            } else {
                CameraPermissionCard(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onUseManual = { mode = QrMode.MANUAL }
                )
            }
        } else {
            ManualTokenCard(
                token = qrToken,
                isLoading = isLoading,
                onTokenChange = { viewModel.updateQrToken(it) },
                onSubmit = { viewModel.scan() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        result?.let {
            QrResultCard(scan = it, onReset = {
                lastScannedToken = null
                lastScanAt = 0L
                viewModel.reset()
            })
        }

        error?.let {
            QrErrorCard(message = it, onDismiss = { viewModel.clearError() })
        }
    }
}

@Composable
private fun QrTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(62.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color.White, Primary100.copy(alpha = 0.48f), Secondary100.copy(alpha = 0.42f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.size(21.dp),
                    tint = Primary500
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.qr_scan_title),
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Gray900
                )
                Text(
                    text = "Validasi kehadiran peserta",
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 11.sp,
                    color = Gray500
                )
            }
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun QrModeSelector(
    selectedMode: QrMode,
    onModeSelected: (QrMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(1.dp, Gray100, RoundedCornerShape(50))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QrMode.entries.forEach { item ->
            val selected = selectedMode == item
            val background by androidx.compose.animation.animateColorAsState(
                targetValue = if (selected) Primary500 else Color.Transparent,
                label = "mode_bg"
            )
            val contentColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (selected) Color.White else Gray600,
                label = "mode_fg"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(background)
                    .clickable { onModeSelected(item) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = if (item == QrMode.CAMERA) Icons.Outlined.PhotoCamera else Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.label,
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun QrCameraScanner(
    isProcessing: Boolean,
    onQrFound: (String) -> Unit,
    showSuccessOverlay: Boolean,
    showErrorOverlay: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            QrCameraPreview(onQrFound = onQrFound)
            
            // Camera viewport frame
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(214.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(2.dp, Color.White.copy(alpha = 0.74f), RoundedCornerShape(28.dp))
            )

            // Result Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = showSuccessOverlay || showErrorOverlay,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.8f),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(targetScale = 1.2f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            if (showSuccessOverlay) SemanticSuccessBase.copy(alpha = 0.9f)
                            else SemanticErrorBase.copy(alpha = 0.9f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showSuccessOverlay) Icons.Default.CheckCircle else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Memverifikasi QR...", color = Color.White, fontFamily = PlusJakartaSansFont, fontSize = 13.sp)
                } else {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Arahkan kamera ke QR peserta", color = Color.White, fontFamily = PlusJakartaSansFont, fontWeight = FontWeight.Bold)
                    Text("Mode manual tetap tersedia jika QR sulit terbaca", color = Color.White.copy(alpha = 0.72f), fontFamily = PlusJakartaSansFont, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun QrCameraPreview(onQrFound: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(context, lifecycleOwner, previewView) {
        var cameraProvider: ProcessCameraProvider? = null
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider
            val preview = CameraPreview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        try {
                            decodeQrCode(imageProxy)?.let { token ->
                                mainExecutor.execute { onQrFound(token) }
                            }
                        } finally {
                            imageProxy.close()
                        }
                    }
                }

            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)
        }, mainExecutor)

        onDispose {
            cameraProvider?.unbindAll()
            analysisExecutor.shutdown()
        }
    }
}

@Composable
private fun CameraPermissionCard(onRequestPermission: () -> Unit, onUseManual: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(26.dp))
            .border(1.dp, Gray100, RoundedCornerShape(26.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(58.dp).clip(CircleShape).background(Primary100),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Primary500, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("Izin kamera diperlukan", fontFamily = NunitoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Gray900)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Aktifkan kamera untuk scan QR langsung, atau gunakan mode manual untuk memasukkan token.",
            fontFamily = PlusJakartaSansFont,
            fontSize = 13.sp,
            color = Gray600,
            lineHeight = 19.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
        ) {
            Text("Aktifkan Kamera", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onUseManual) {
            Text("Gunakan token manual", color = Gray600, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ManualTokenCard(
    token: String,
    isLoading: Boolean,
    onTokenChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(26.dp))
            .border(1.dp, Gray100, RoundedCornerShape(26.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        com.app.lokacara.ui.components.LokacaraTextField(
            value = token,
            onValueChange = onTokenChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.qr_token_placeholder),
            label = stringResource(R.string.qr_token_label),
            isOutlined = true,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            leadingIcon = { Icon(Icons.Outlined.ContentPaste, null, tint = Primary500, modifier = Modifier.size(20.dp)) }
        )
        
        Button(
            onClick = onSubmit,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.qr_verify_button), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QrResultCard(scan: ScanResponse, onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SemanticSuccessLight.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .border(1.dp, SemanticSuccessLight, RoundedCornerShape(22.dp))
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticSuccessBase, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(scan.message, fontFamily = NunitoFont, fontWeight = FontWeight.Bold, color = Gray900)
                Text("Status: ${scan.registration.status}", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray600)
                scan.registration.checked_in_at?.let {
                    Text("Check-in: $it", fontFamily = PlusJakartaSansFont, fontSize = 12.sp, color = Gray600)
                }
            }
            TextButton(onClick = onReset) {
                Text("Scan lagi", color = Primary500, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QrErrorCard(message: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(SemanticErrorLight.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .border(1.dp, SemanticErrorLight, RoundedCornerShape(18.dp))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = SemanticErrorBase)
            Spacer(modifier = Modifier.width(10.dp))
            Text(message, modifier = Modifier.weight(1f), color = Gray700, fontFamily = PlusJakartaSansFont, fontSize = 13.sp)
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = SemanticErrorBase, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun decodeQrCode(imageProxy: ImageProxy): String? {
    val width = imageProxy.width
    val height = imageProxy.height
    val yPlane = imageProxy.planes.firstOrNull() ?: return null
    val rowStride = yPlane.rowStride
    val buffer = yPlane.buffer
    val yData = ByteArray(width * height)

    for (row in 0 until height) {
        val sourceOffset = row * rowStride
        if (sourceOffset + width > buffer.capacity()) break
        buffer.position(sourceOffset)
        buffer.get(yData, row * width, width)
    }

    val rotation = imageProxy.imageInfo.rotationDegrees
    val rotatedData: ByteArray
    val rotatedWidth: Int
    val rotatedHeight: Int
    when (rotation) {
        90 -> {
            rotatedData = rotateYuv90(yData, width, height)
            rotatedWidth = height
            rotatedHeight = width
        }
        180 -> {
            rotatedData = rotateYuv180(yData, width, height)
            rotatedWidth = width
            rotatedHeight = height
        }
        270 -> {
            rotatedData = rotateYuv270(yData, width, height)
            rotatedWidth = height
            rotatedHeight = width
        }
        else -> {
            rotatedData = yData
            rotatedWidth = width
            rotatedHeight = height
        }
    }

    return try {
        val source = PlanarYUVLuminanceSource(rotatedData, rotatedWidth, rotatedHeight, 0, 0, rotatedWidth, rotatedHeight, false)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        MultiFormatReader().decodeWithState(bitmap).text
    } catch (_: NotFoundException) {
        null
    } catch (_: Exception) {
        null
    }
}

private fun rotateYuv90(data: ByteArray, width: Int, height: Int): ByteArray {
    val output = ByteArray(data.size)
    var index = 0
    for (x in 0 until width) {
        for (y in height - 1 downTo 0) {
            output[index++] = data[y * width + x]
        }
    }
    return output
}

private fun rotateYuv180(data: ByteArray, width: Int, height: Int): ByteArray {
    val output = ByteArray(data.size)
    var index = 0
    for (y in height - 1 downTo 0) {
        for (x in width - 1 downTo 0) {
            output[index++] = data[y * width + x]
        }
    }
    return output
}

private fun rotateYuv270(data: ByteArray, width: Int, height: Int): ByteArray {
    val output = ByteArray(data.size)
    var index = 0
    for (x in width - 1 downTo 0) {
        for (y in 0 until height) {
            output[index++] = data[y * width + x]
        }
    }
    return output
}
