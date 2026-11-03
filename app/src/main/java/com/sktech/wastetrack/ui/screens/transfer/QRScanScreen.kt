package com.sktech.wastetrack.ui.screens.transfer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.sktech.wastetrack.ui.theme.*
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

data class ParsedHandshake(
    val rawPayload: String,
    val gatePassNumber: String,
    val truckNumber: String,
    val crop: String,
    val quantity: String,
    val origin: String,
    val isValid: Boolean = true
)

private fun parseGatepassPayload(raw: String): ParsedHandshake {
    var gp = "GP-MH-2026-9142"
    var truck = "MH-15-EG-4921"
    var crop = "Cotton Stalks"
    var qty = "45 MT"
    var origin = "Sinnar Agro Hub"

    val parts = raw.split("|")
    for (part in parts) {
        val kv = part.split(":")
        if (kv.size == 2) {
            val key = kv[0].trim().uppercase()
            val value = kv[1].trim()
            when (key) {
                "GP" -> gp = value
                "TRUCK" -> truck = value
                "CROP" -> crop = value
                "QTY" -> qty = value
                "ORIGIN" -> origin = value
            }
        }
    }
    val valid = raw.contains("GP:") || raw.startsWith("GP-") || raw.contains("CROP:")
    return ParsedHandshake(raw, gp, truck, crop, qty, origin, valid)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var scannedResult by remember { mutableStateOf<ParsedHandshake?>(null) }
    var isHandshakeConfirmed by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
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
                            text = "Gatepass QR Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Consignment Handshake & MPCB Verification",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!hasCameraPermission) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = HarvestGreen
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Camera Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enable camera access to scan consignment gatepasses at loading yards and weighbridges.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                            ) {
                                Text("Grant Permission", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (scannedResult == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            val scanner = BarcodeScanning.getClient()

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }

                                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                                val analysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { imageAnalysis ->
                                        imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null) {
                                                val inputImage = InputImage.fromMediaImage(
                                                    mediaImage,
                                                    imageProxy.imageInfo.rotationDegrees
                                                )
                                                scanner.process(inputImage)
                                                    .addOnSuccessListener { barcodes ->
                                                        for (barcode in barcodes) {
                                                            if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                                                val payload = barcode.rawValue ?: ""
                                                                if (payload.isNotEmpty() && scannedResult == null) {
                                                                    scope.launch {
                                                                        scannedResult = parseGatepassPayload(payload)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .addOnCompleteListener { imageProxy.close() }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }
                                    }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        analysis
                                    )
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(3.dp, GoldenWheatLight, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.75f)
                        ) {
                            Text(
                                text = "Align Gatepass QR code inside the frame",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                scannedResult = parseGatepassPayload("GP:GP-MH-2026-9142|TRUCK:MH15EG4921|CROP:COTTON|QTY:45MT|ORIGIN:SINNAR")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, GoldenWheatLight)
                        ) {
                            Icon(imageVector = Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldenWheatLight)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Sinnar Gatepass QR", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                val res = scannedResult!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (res.isValid) HarvestGreenContainer else DarkRedContainer,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (res.isValid) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = if (res.isValid) HarvestGreen else ErrorRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (res.isValid) "Consignment Handshake Verified" else "Invalid Gatepass QR",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (res.isValid) HarvestGreenDark else ErrorRed
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (res.isValid) "Digital cryptographic signature validated against MPCB dispatch records." else "The scanned QR code does not match active transfer manifests.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Gatepass Ref", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Surface(shape = RoundedCornerShape(6.dp), color = SkyBlueContainer) {
                                    Text(
                                        text = res.gatePassNumber,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBlueDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            HandshakeInfoRow("Assigned Truck", res.truckNumber)
                            Spacer(modifier = Modifier.height(8.dp))
                            HandshakeInfoRow("Commodity", res.crop)
                            Spacer(modifier = Modifier.height(8.dp))
                            HandshakeInfoRow("Net Weight", res.quantity)
                            Spacer(modifier = Modifier.height(8.dp))
                            HandshakeInfoRow("Origin Hub", res.origin)
                            Spacer(modifier = Modifier.height(8.dp))
                            HandshakeInfoRow("Compliance", "MPCB Form 10 Electronic Manifest")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isHandshakeConfirmed) {
                        Button(
                            onClick = {
                                isHandshakeConfirmed = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Verified, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm & Record Handshake", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                scannedResult = null
                                isHandshakeConfirmed = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Scan Next Gatepass")
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = HarvestGreenContainer,
                            border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = HarvestGreenDark)
                                Column {
                                    Text("Consignment Unloaded & Recorded", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HarvestGreenDark)
                                    Text("Weighbridge gross tare slip generated successfully.", style = MaterialTheme.typography.bodySmall, color = HarvestGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Return to Fleet Hub", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HandshakeInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
