package com.sktech.wastetrack.ui.screens.dashboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.ocr.MandiReceiptSlip
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScanScreen(
    onNavigateBack: () -> Unit,
    viewModel: LedgerScanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    BackHandler(onBack = onNavigateBack)

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var showRawTextSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Physical Slip OCR Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Digitize APMC mandi receipts & weighbridge chits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearResult() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = HarvestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.parsedSlip == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
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
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight(0.70f)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, HarvestGreen)
                        ) {}

                        Text(
                            text = "Align Mandi Receipt or Weighbridge Slip\nwithin green viewfinder",
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        )
                    }

                    if (state.isProcessing) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(32.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = HarvestGreen)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Analyzing Document with Google MLKit OCR...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Extracting Gross, Tare, Commodity and Mandi rates",
                                    fontSize = 12.sp,
                                    color = DarkTextSecondary
                                )
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sample Slip Simulation (Instant Test)",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                OutlinedButton(
                                    onClick = { viewModel.simulateSampleScan(0) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("APMC Cotton Slip", fontSize = 11.sp)
                                }
                            }
                            item {
                                OutlinedButton(
                                    onClick = { viewModel.simulateSampleScan(1) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Soybean Fodder Slip", fontSize = 11.sp)
                                }
                            }
                            item {
                                OutlinedButton(
                                    onClick = { viewModel.simulateSampleScan(2) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Paddy Stubble Chit", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        IconButton(
                            onClick = {
                                val capture = imageCapture
                                if (capture != null) {
                                    capture.takePicture(
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                                val buffer = imageProxy.planes[0].buffer
                                                val bytes = ByteArray(buffer.remaining())
                                                buffer.get(bytes)
                                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                val matrix = Matrix().apply {
                                                    postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                                                }
                                                val rotated = Bitmap.createBitmap(
                                                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                                                )
                                                imageProxy.close()
                                                viewModel.processImage(rotated)
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                viewModel.simulateSampleScan(0)
                                            }
                                        }
                                    )
                                } else {
                                    viewModel.simulateSampleScan(0)
                                }
                            },
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(HarvestGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Capture Document",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            } else {
                state.parsedSlip?.let { slip ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (state.isSavedToLedger) HarvestGreen.copy(alpha = 0.15f) else GoldenWheatLight.copy(alpha = 0.20f),
                                border = BorderStroke(1.dp, if (state.isSavedToLedger) HarvestGreen else GoldenWheatLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (state.isSavedToLedger) Icons.Filled.CheckCircle else Icons.Filled.DocumentScanner,
                                        contentDescription = null,
                                        tint = if (state.isSavedToLedger) HarvestGreenDark else GoldenWheatDark
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (state.isSavedToLedger) "Synced to Electronic Ledger" else "OCR Extraction Successful",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (state.isSavedToLedger) HarvestGreenDark else GoldenWheatDark
                                        )
                                        Text(
                                            text = if (state.isSavedToLedger) "Record permanently cryptographically signed" else "Verify parsed weights and commodity rates below",
                                            fontSize = 11.sp,
                                            color = DarkTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            MandiSlipCard(slip = slip)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showRawTextSheet = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Raw OCR", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { viewModel.saveSlipToLedger() },
                                    modifier = Modifier.weight(1.5f),
                                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !state.isSavedToLedger
                                ) {
                                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (state.isSavedToLedger) "Saved" else "Save to Ledger", fontSize = 12.sp)
                                }
                            }
                        }

                        item {
                            OutlinedButton(
                                onClick = { viewModel.clearResult() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Another Slip")
                            }
                        }

                        item {
                            Text(
                                text = "Recently Digitized Weighbridge Records",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        items(state.recentScannedSlips) { historicSlip ->
                            HistoricSlipRow(slip = historicSlip)
                        }
                    }
                }
            }
        }

        if (showRawTextSheet) {
            AlertDialog(
                onDismissRequest = { showRawTextSheet = false },
                title = { Text("Raw Optical Text Extraction") },
                text = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = state.extractedRawText.ifBlank { "No raw text captured." },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRawTextSheet = false }) {
                        Text("Close", color = HarvestGreen)
                    }
                }
            )
        }
    }
}

@Composable
private fun MandiSlipCard(slip: MandiReceiptSlip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slip.facilityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${slip.slipType} | ${slip.rstNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HarvestGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "VERIFIED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeightMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "Gross Wt",
                    value = "${slip.grossWeightKg.toInt()} kg",
                    color = DarkTextSecondary
                )
                WeightMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "Tare Wt",
                    value = "${slip.tareWeightKg.toInt()} kg",
                    color = DarkTextSecondary
                )
                WeightMetricBox(
                    modifier = Modifier.weight(1.2f),
                    label = "Net Biomass",
                    value = "${slip.netWeightMt} MT",
                    color = HarvestGreenDark,
                    isHighlight = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            DetailKeyValueRow(label = "Commodity Type", value = slip.commodity)
            DetailKeyValueRow(label = "Vehicle Registration", value = slip.vehicleNumber)
            DetailKeyValueRow(label = "Weighing Date & Time", value = "${slip.dateString} at ${slip.timeString}")
            DetailKeyValueRow(label = "Weigher / Operator", value = slip.weigherOperator)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "APMC Mandi Rate", fontSize = 11.sp, color = DarkTextSecondary)
                    Text(text = "INR ${slip.ratePerMtInr.toInt()} / MT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Settlement Value", fontSize = 11.sp, color = DarkTextSecondary)
                    Text(text = "INR ${slip.totalAmountInr.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HarvestGreenDark)
                }
            }
        }
    }
}

@Composable
private fun WeightMetricBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    isHighlight: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isHighlight) HarvestGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isHighlight) BorderStroke(1.dp, HarvestGreen) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = DarkTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DetailKeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HistoricSlipRow(slip: MandiReceiptSlip) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = HarvestGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null,
                            tint = HarvestGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = slip.commodity, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "${slip.facilityName} | ${slip.rstNumber}", fontSize = 11.sp, color = DarkTextSecondary)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${slip.netWeightMt} MT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HarvestGreenDark)
                Text(text = "INR ${slip.totalAmountInr.toInt()}", fontSize = 11.sp, color = DarkTextSecondary)
            }
        }
    }
}
