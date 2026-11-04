package com.sktech.wastetrack.ui.screens.disease

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sktech.wastetrack.domain.model.BiomassDegradationScore
import com.sktech.wastetrack.domain.model.DiagnosticSamplePreset
import com.sktech.wastetrack.domain.model.PathogenSeverity
import com.sktech.wastetrack.domain.model.ResidueEndUseRecommendation
import com.sktech.wastetrack.domain.model.StubbleDiseaseDiagnosis
import com.sktech.wastetrack.ui.theme.DarkTextPrimary
import com.sktech.wastetrack.ui.theme.DarkTextSecondary
import com.sktech.wastetrack.ui.theme.HarvestGreen
import com.sktech.wastetrack.ui.theme.HarvestGreenDark
import com.sktech.wastetrack.ui.theme.HarvestGreenLight
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiseaseDetectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.analyzeImageUri(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Stubble Disease & Moisture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Edge TFLite Diagnostic Radar",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.speakDiagnosticReport() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Voice Guidance",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.toggleFlash() }) {
                        Icon(
                            imageVector = if (uiState.isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flashlight",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HarvestGreenDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val tabs = listOf("Scanner View", "Diagnostic", "Moisture Meter", "Intervention")
            ScrollableTabRow(
                selectedTabIndex = uiState.activeTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = HarvestGreenDark
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.activeTab == index,
                        onClick = { viewModel.setActiveTab(index) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (uiState.activeTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            AnimatedVisibility(visible = uiState.statusMessage != null) {
                uiState.statusMessage?.let { msg ->
                    Surface(
                        color = HarvestGreenLight.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = HarvestGreenDark,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            when (uiState.activeTab) {
                0 -> ScannerViewTab(
                    hasPermission = hasCameraPermission,
                    isAnalyzing = uiState.isAnalyzing,
                    presets = uiState.samplePresets,
                    selectedPresetId = uiState.selectedPresetId,
                    onCaptureBitmap = { bmp, rot -> viewModel.analyzeCapturedBitmap(bmp, rot) },
                    onSelectPreset = { preset -> viewModel.selectPreset(preset) },
                    onOpenGallery = { galleryLauncher.launch("image/*") }
                )
                1 -> DiagnosticTab(
                    diagnosis = uiState.diagnosis,
                    degradation = uiState.degradation,
                    onRetake = { viewModel.setActiveTab(0) }
                )
                2 -> MoistureMeterTab(
                    degradation = uiState.degradation,
                    diagnosis = uiState.diagnosis,
                    onNavigateToIntervention = { viewModel.setActiveTab(3) }
                )
                3 -> InterventionTab(
                    diagnosis = uiState.diagnosis,
                    onSpeak = { viewModel.speakDiagnosticReport() }
                )
            }
        }
    }
}

@Composable
private fun ScannerViewTab(
    hasPermission: Boolean,
    isAnalyzing: Boolean,
    presets: List<DiagnosticSamplePreset>,
    selectedPresetId: String?,
    onCaptureBitmap: (Bitmap, Int) -> Unit,
    onSelectPreset: (DiagnosticSamplePreset) -> Unit,
    onOpenGallery: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            if (hasPermission) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    capture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Camera permission required for live edge scanner",
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Transparent)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, HarvestGreenLight)
                ) {}

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = (260 * scanProgress).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    HarvestGreen,
                                    HarvestGreenLight,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = HarvestGreenLight)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing Stubble via Edge TFLite...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Offline Edge Sample Presets:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(presets) { preset ->
                        val isSelected = preset.id == selectedPresetId
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectPreset(preset) },
                            label = { Text(preset.cropName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreenLight,
                                selectedLabelColor = HarvestGreenDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOpenGallery,
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = HarvestGreenDark
                        )
                    }

                    Button(
                        onClick = {
                            val capture = imageCapture ?: return@Button
                            capture.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        val buffer = imageProxy.planes[0].buffer
                                        val bytes = ByteArray(buffer.remaining())
                                        buffer.get(bytes)
                                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        val rotation = imageProxy.imageInfo.rotationDegrees
                                        imageProxy.close()
                                        if (bitmap != null) {
                                            ContextCompat.getMainExecutor(context).execute {
                                                onCaptureBitmap(bitmap, rotation)
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        exception.printStackTrace()
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture Stubble Image",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(52.dp))
                }
            }
        }
    }
}

@Composable
private fun DiagnosticTab(
    diagnosis: StubbleDiseaseDiagnosis?,
    degradation: BiomassDegradationScore?,
    onRetake: () -> Unit
) {
    if (diagnosis == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = HarvestGreen,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No scan diagnostic available yet", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRetake, colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)) {
                    Text("Launch Camera Scanner")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (diagnosis.isHealthy) HarvestGreenLight.copy(alpha = 0.35f) else Color(0xFFFFEBEE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (diagnosis.isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (diagnosis.isHealthy) HarvestGreenDark else Color(0xFFC62828),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = diagnosis.cropName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = diagnosis.diseaseName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (diagnosis.isHealthy) HarvestGreenDark else Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Neural Confidence: ${diagnosis.confidence}% • ${diagnosis.modelSource}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextSecondary
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pathogen Infection Severity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = diagnosis.severityLevel.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${diagnosis.severityPercentage}%",
                            fontWeight = FontWeight.Bold,
                            color = if (diagnosis.isHealthy) HarvestGreenDark else Color(0xFFD32F2F)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (diagnosis.severityPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (diagnosis.isHealthy) HarvestGreen else Color(0xFFD32F2F),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        if (degradation != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Stubble Matrix Degradation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Moisture Level", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                                Text("${degradation.estimatedMoisturePercent}%", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column {
                                Text("Dry Matter", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                                Text("${degradation.biomassDryMatterPercent}%", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column {
                                Text("Rot Risk", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                                Text("${degradation.rotRiskPercentage}%", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFE65100))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = degradation.degradationStatus,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = HarvestGreenDark
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = HarvestGreenDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Downstream Utilization Match",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = diagnosis.endUseRecommendation.title,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = diagnosis.endUseRecommendation.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MoistureMeterTab(
    degradation: BiomassDegradationScore?,
    diagnosis: StubbleDiseaseDiagnosis?,
    onNavigateToIntervention: () -> Unit
) {
    if (degradation == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Capture or select a sample to calculate moisture telemetry")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = if (degradation.estimatedMoisturePercent <= 20f) HarvestGreenDark else Color(0xFF1976D2),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${degradation.estimatedMoisturePercent}% Moisture",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                    Text(
                        text = degradation.qualityGrade,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = HarvestGreenDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { (degradation.estimatedMoisturePercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp)),
                        color = if (degradation.estimatedMoisturePercent <= 20f) HarvestGreen else Color(0xFF1976D2),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Biomass Physical Properties",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Biomass Dry Matter", color = DarkTextSecondary)
                        Text("${degradation.biomassDryMatterPercent}%", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fibrous Matrix Score", color = DarkTextSecondary)
                        Text("${(degradation.fibrousDensityScore * 100).toInt()}/100", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Overall Quality Score", color = DarkTextSecondary)
                        Text("${degradation.qualityScore}/100", fontWeight = FontWeight.Bold, color = HarvestGreenDark)
                    }

                    if (degradation.contaminationNotice != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Notice: ${degradation.contaminationNotice}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onNavigateToIntervention,
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreenDark),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Agronomic Remedies & Protocol", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InterventionTab(
    diagnosis: StubbleDiseaseDiagnosis?,
    onSpeak: () -> Unit
) {
    if (diagnosis == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No diagnostic selected to display remedies")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HarvestGreenLight.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Organic Biological Remedy",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = HarvestGreenDark
                        )
                        IconButton(onClick = onSpeak) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen",
                                tint = HarvestGreenDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = diagnosis.organicRemedy,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextPrimary
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Targeted Chemical Intervention",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = diagnosis.chemicalTreatment,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextPrimary
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preventative Cultural Practices",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    diagnosis.preventativeSteps.forEachIndexed { idx, step ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "${idx + 1}. ",
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreenDark
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
