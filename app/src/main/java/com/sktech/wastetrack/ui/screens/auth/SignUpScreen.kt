package com.sktech.wastetrack.ui.screens.auth

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.R
import com.sktech.wastetrack.domain.model.UserRole
import com.sktech.wastetrack.domain.model.VehicleType
import com.sktech.wastetrack.ui.theme.*
import com.sktech.wastetrack.util.localizedDisplayName
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onProfileCompleted: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        com.sktech.wastetrack.util.CityHelper.loadCities(context)
    }

    var showEnableGpsCard by remember { mutableStateOf(false) }

    fun doLocationFetch() {
        if (!com.sktech.wastetrack.util.LocationHelper.isGpsEnabled(context)) {
            showEnableGpsCard = true
            return
        }
        showEnableGpsCard = false
        scope.launch {
            viewModel.setFetchingLocation(true)
            val loc = com.sktech.wastetrack.util.LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                val (city, locality) = com.sktech.wastetrack.util.LocationHelper.getCityAndLocality(context, loc)
                val detectedCity = city ?: com.sktech.wastetrack.util.LocationHelper.getFormattedLocationName(context, loc.latitude, loc.longitude) ?: "Nashik"
                viewModel.onLocationDetected(detectedCity, locality, loc.latitude, loc.longitude)
            } else {
                viewModel.setLocationError("Unable to fetch GPS coordinates. Please check device location settings.")
            }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && showEnableGpsCard) {
                if (com.sktech.wastetrack.util.LocationHelper.isGpsEnabled(context)) {
                    showEnableGpsCard = false
                    if (com.sktech.wastetrack.util.LocationHelper.hasLocationPermission(context)) {
                        doLocationFetch()
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (com.sktech.wastetrack.util.LocationHelper.isGpsEnabled(context)) {
                doLocationFetch()
            } else {
                showEnableGpsCard = true
            }
        } else {
            viewModel.setLocationError("Location permission denied. You can select your city manually.")
        }
    }

    val onFetchLocation: () -> Unit = {
        if (com.sktech.wastetrack.util.LocationHelper.hasLocationPermission(context)) {
            if (!com.sktech.wastetrack.util.LocationHelper.isGpsEnabled(context)) {
                showEnableGpsCard = true
            } else {
                doLocationFetch()
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var activeVoiceCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }
    var activeVoiceFieldTitle by remember { mutableStateOf<String>("") }
    var pendingVoicePrompt by remember { mutableStateOf<String?>(null) }
    var pendingCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                activeVoiceCallback?.invoke(spoken)
                viewModel.setLastVoiceField(activeVoiceFieldTitle, spoken)
                Toast.makeText(context, "🎙️ Captured: \"$spoken\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchVoiceRecognitionIntent(prompt: String, callback: (String) -> Unit, fieldTitle: String) {
        activeVoiceCallback = callback
        activeVoiceFieldTitle = fieldTitle
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Voice recognition service not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val prompt = pendingVoicePrompt ?: "Speak now (English / हिंदी / मराठी)..."
            val cb = pendingCallback
            if (cb != null) {
                launchVoiceRecognitionIntent(prompt, cb, activeVoiceFieldTitle)
            }
        } else {
            Toast.makeText(context, "Microphone permission required for Voice Log", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerVoiceInput(prompt: String, fieldTitle: String, onTextCaptured: (String) -> Unit) {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasMicPermission) {
            launchVoiceRecognitionIntent(prompt, onTextCaptured, fieldTitle)
        } else {
            pendingVoicePrompt = prompt
            pendingCallback = onTextCaptured
            activeVoiceFieldTitle = fieldTitle
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onProfileCompleted()
        }
    }

    val roleThemeColor = when (state.role) {
        UserRole.FARMER -> HarvestGreen
        UserRole.BUYER -> Color(0xFF0F766E)
        UserRole.DRIVER -> Color(0xFFD97706)
        else -> HarvestGreen
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (state.role) {
                                UserRole.FARMER -> "Farmer & Producer Registration"
                                UserRole.BUYER -> "Buyer & Industry Registration"
                                UserRole.DRIVER -> "Transporter & Fleet Registration"
                                else -> stringResource(R.string.org_registration_title)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (state.role) {
                                UserRole.FARMER -> "🌾 Agro-Residue Holdings & Direct Mandi Connect"
                                UserRole.BUYER -> "🏭 Biomass Auctions & Industrial Procurement"
                                UserRole.DRIVER -> "🚚 Logistics Fleet, Weighbridge & Transit Escrow"
                                else -> stringResource(R.string.profile_setup_subtitle, state.role.displayName)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = roleThemeColor,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = HarvestGreenContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = null,
                                tint = HarvestGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Voice Ready",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreen,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = when (state.role) {
                    UserRole.FARMER -> adaptiveHarvestContainer()
                    UserRole.BUYER -> Color(0xFFE6F4F1)
                    UserRole.DRIVER -> Color(0xFFFEF3C7)
                    else -> adaptiveHarvestContainer()
                },
                border = BorderStroke(1.dp, roleThemeColor.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = roleThemeColor,
                            modifier = Modifier.size(46.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    when (state.role) {
                                        UserRole.FARMER -> Icons.Filled.Agriculture
                                        UserRole.BUYER -> Icons.Filled.Storefront
                                        UserRole.DRIVER -> Icons.Filled.LocalShipping
                                        else -> Icons.Filled.Factory
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = when (state.role) {
                                        UserRole.FARMER -> "Farmer & Producer"
                                        UserRole.BUYER -> "Buyer & Industry Dealer"
                                        UserRole.DRIVER -> "Transporter & Fleet"
                                        else -> state.role.displayName
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = roleThemeColor
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = roleThemeColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Verified",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = roleThemeColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                text = when (state.role) {
                                    UserRole.FARMER -> "Direct Mandi residue auctions, AI crop scanner & instant DBT payments"
                                    UserRole.BUYER -> "Bulk crop waste procurement, verified weighbridge & ESG carbon offsets"
                                    UserRole.DRIVER -> "GPS load dispatch, digital weighbridge fast-pass & transit escrow protection"
                                    else -> stringResource(R.string.role_recycler_setup_desc)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val pills = when (state.role) {
                            UserRole.FARMER -> listOf("🌱 Direct Mandi", "📸 AI Scanner", "⚡ Fast Payout")
                            UserRole.BUYER -> listOf("📊 Live Auctions", "🏭 Bulk Sourcing", "🌿 ESG Offsets")
                            UserRole.DRIVER -> listOf("🚚 Real-Time Loads", "⚖️ Weighbridge", "🛡️ Escrow Safe")
                            else -> listOf("AgriStock Ecosystem")
                        }
                        pills.forEach { pill ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                border = BorderStroke(0.5.dp, roleThemeColor.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = pill,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Account Role Type",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RoleTabButton(
                                role = UserRole.FARMER,
                                label = "Farmer",
                                subtitle = "Producer",
                                icon = Icons.Filled.Agriculture,
                                isSelected = state.role == UserRole.FARMER,
                                onSelect = { viewModel.onRoleSelected(UserRole.FARMER) },
                                modifier = Modifier.weight(1f)
                            )
                            RoleTabButton(
                                role = UserRole.BUYER,
                                label = "Buyer",
                                subtitle = "Dealer",
                                icon = Icons.Filled.Storefront,
                                isSelected = state.role == UserRole.BUYER,
                                onSelect = { viewModel.onRoleSelected(UserRole.BUYER) },
                                modifier = Modifier.weight(1f)
                            )
                            RoleTabButton(
                                role = UserRole.DRIVER,
                                label = "Transporter",
                                subtitle = "Fleet",
                                icon = Icons.Filled.LocalShipping,
                                isSelected = state.role == UserRole.DRIVER,
                                onSelect = { viewModel.onRoleSelected(UserRole.DRIVER) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = HarvestGreenContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = HarvestGreen,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.Mic,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            "Multilingual Voice Log",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = HarvestGreenDark
                                        )
                                        Text(
                                            "Tap mic on any field or use Quick Fill",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HarvestGreenDark.copy(alpha = 0.8f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        triggerVoiceInput(
                                            prompt = "Speak details (e.g. Name Ramesh, Farm Patil Agro, City Nashik)",
                                            fieldTitle = "Smart Voice Fill"
                                        ) { spokenText ->
                                            val count = viewModel.parseAndApplyVoiceLog(spokenText)
                                            if (count == 0) {
                                                viewModel.onNameChanged(spokenText.capitalizeWords())
                                                viewModel.setLastVoiceField("Name", spokenText)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HarvestGreen,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quick Fill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Supported:", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = HarvestGreenDark.copy(alpha = 0.7f))
                                listOf("🇬🇧 English", "🇮🇳 हिन्दी", "🇮🇳 मराठी").forEach { lang ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.8f)
                                    ) {
                                        Text(
                                            text = lang,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = HarvestGreenDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (!state.lastVoiceField.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = BorderStroke(0.5.dp, HarvestGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(14.dp))
                                        Text(
                                            text = "🎙️ Logged: ${state.lastVoiceField} ${state.lastVoiceText?.let { "(\"$it\")" }.orEmpty()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HarvestGreenDark,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.error != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = adaptiveRedContainer(),
                            border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Error, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                                Text(state.error!!, color = AlertRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    AgriVoiceInputField(
                        label = when (state.role) {
                            UserRole.FARMER -> "Farmer / Authorized Name"
                            UserRole.BUYER -> "Authorized Representative Name"
                            UserRole.DRIVER -> "Fleet Driver / Owner Name"
                            else -> "Authorized Representative Name"
                        },
                        value = state.name,
                        onValueChange = viewModel::onNameChanged,
                        placeholder = "e.g. Rajesh Sharma / रमेश पाटील",
                        leadingIcon = Icons.Outlined.Person,
                        isRequired = true,
                        roleThemeColor = roleThemeColor,
                        onMicClick = {
                            triggerVoiceInput(
                                prompt = "Speak full name (अपना नाम बोलें / पूर्ण नाव सांगा)",
                                fieldTitle = "Name"
                            ) { spoken ->
                                viewModel.onNameChanged(spoken.capitalizeWords())
                            }
                        }
                    )

                    AgriVoiceInputField(
                        label = when (state.role) {
                            UserRole.FARMER -> "Farm / Holding Name"
                            UserRole.BUYER -> "Business / Company Name"
                            UserRole.DRIVER -> "Transport Agency / Fleet Name"
                            else -> "Organization Name"
                        },
                        value = state.organizationName,
                        onValueChange = viewModel::onOrganizationNameChanged,
                        placeholder = when (state.role) {
                            UserRole.FARMER -> "e.g. Green Valley Agro Farms (Nashik)"
                            UserRole.BUYER -> "e.g. AgroBio Processing Pvt. Ltd."
                            UserRole.DRIVER -> "e.g. Godavari Express Logistics"
                            else -> "e.g. AgriStock Enterprise"
                        },
                        leadingIcon = when (state.role) {
                            UserRole.FARMER -> Icons.Outlined.Agriculture
                            UserRole.BUYER -> Icons.Outlined.Business
                            UserRole.DRIVER -> Icons.Outlined.LocalShipping
                            else -> Icons.Outlined.Business
                        },
                        isRequired = true,
                        roleThemeColor = roleThemeColor,
                        onMicClick = {
                            val rolePrompt = when (state.role) {
                                UserRole.FARMER -> "Speak farm name (शेताचे नाव बोलें)"
                                UserRole.BUYER -> "Speak company name (कंपनीचे नाव बोलें)"
                                UserRole.DRIVER -> "Speak transport agency name (ट्रान्सपोर्ट नाव बोलें)"
                                else -> "Speak organization name"
                            }
                            triggerVoiceInput(
                                prompt = rolePrompt,
                                fieldTitle = "Organization"
                            ) { spoken ->
                                viewModel.onOrganizationNameChanged(spoken.capitalizeWords())
                            }
                        }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (state.role) {
                                    UserRole.FARMER -> "Mandi Area / APMC / City *"
                                    UserRole.BUYER -> "Mandi Area / City *"
                                    UserRole.DRIVER -> "Operating City / Transport Hub *"
                                    else -> stringResource(R.string.industrial_zone_city)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = HarvestGreenContainer,
                                border = BorderStroke(0.5.dp, HarvestGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable(enabled = !state.isFetchingLocation) {
                                    onFetchLocation()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (state.isFetchingLocation) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = HarvestGreen)
                                        Text("Detecting...", style = MaterialTheme.typography.labelSmall, color = HarvestGreen, fontSize = 11.sp)
                                    } else {
                                        Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.size(13.dp), tint = HarvestGreen)
                                        Text("Detect Location", style = MaterialTheme.typography.labelSmall, color = HarvestGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        var cityExpanded by remember { mutableStateOf(false) }
                        val suggestions = com.sktech.wastetrack.util.CityHelper.getSuggestions(state.city)

                        ExposedDropdownMenuBox(
                            expanded = cityExpanded && suggestions.isNotEmpty(),
                            onExpandedChange = { cityExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = state.city,
                                onValueChange = {
                                    viewModel.onCityChanged(it)
                                    cityExpanded = true
                                },
                                placeholder = { Text("e.g. Lasalgaon APMC, Nashik") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.LocationCity, contentDescription = null, tint = roleThemeColor)
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                triggerVoiceInput(
                                                    prompt = "Speak city or mandi (शहर किंवा मंडीचे नाव बोलें)",
                                                    fieldTitle = "City / Mandi"
                                                ) { spoken ->
                                                    viewModel.onCityChanged(spoken.capitalizeWords())
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.Mic,
                                                contentDescription = "Voice Dictate City",
                                                tint = HarvestGreen
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = roleThemeColor,
                                    focusedLeadingIconColor = roleThemeColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = cityExpanded && suggestions.isNotEmpty(),
                                onDismissRequest = { cityExpanded = false }
                            ) {
                                suggestions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            viewModel.onCityChanged(option)
                                            cityExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (!state.locationMessage.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HarvestGreenContainer,
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = HarvestGreen)
                                    Text(state.locationMessage.orEmpty(), style = MaterialTheme.typography.labelSmall, color = HarvestGreenDark)
                                }
                            }
                        }

                        if (showEnableGpsCard) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.LocationOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.gps_device_turned_off_title),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = stringResource(R.string.gps_device_turned_off_desc),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                        )
                                    }
                                    Button(
                                        onClick = { com.sktech.wastetrack.util.LocationHelper.openGpsSettings(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.btn_turn_on),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AgriVoiceInputField(
                        label = when (state.role) {
                            UserRole.FARMER -> "Farm Address / Survey / Gat No. Landmark *"
                            UserRole.BUYER -> "Industrial Area / Locality / Plant Landmark *"
                            UserRole.DRIVER -> "Depot / Transport Hub / Parking Landmark *"
                            else -> "Industrial Area / Locality / Plant Landmark"
                        },
                        value = state.industrialArea,
                        onValueChange = viewModel::onIndustrialAreaChanged,
                        placeholder = when (state.role) {
                            UserRole.FARMER -> "e.g. Gat No. 42, Near Dindori Canal, Niphad"
                            UserRole.BUYER -> "e.g. Ambad MIDC Phase 2, Plot 14"
                            UserRole.DRIVER -> "e.g. Mumbai-Agra Highway Hub, Dindori Naka"
                            else -> "e.g. Ambad MIDC, Nashik"
                        },
                        leadingIcon = Icons.Outlined.Place,
                        isRequired = true,
                        roleThemeColor = roleThemeColor,
                        onMicClick = {
                            triggerVoiceInput(
                                prompt = "Speak locality or landmark (पत्ता किंवा लँडमार्क बोलें)",
                                fieldTitle = "Landmark / Locality"
                            ) { spoken ->
                                viewModel.onIndustrialAreaChanged(spoken.capitalizeWords())
                            }
                        }
                    )

                    if (state.role == UserRole.DRIVER) {
                        AgriVoiceInputField(
                            label = "Vehicle / Truck Registration Number *",
                            value = state.vehicleNumber,
                            onValueChange = viewModel::onVehicleNumberChanged,
                            placeholder = "e.g. MH-15-EG-8291",
                            leadingIcon = Icons.Outlined.LocalShipping,
                            isRequired = true,
                            roleThemeColor = roleThemeColor,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            onMicClick = {
                                triggerVoiceInput(
                                    prompt = "Speak vehicle registration number (वाहन क्रमांक बोलें)",
                                    fieldTitle = "Vehicle Number"
                                ) { spoken ->
                                    val cleaned = spoken.uppercase().replace(" ", "").replace("-", "")
                                    viewModel.onVehicleNumberChanged(cleaned)
                                }
                            }
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Truck Fleet Capacity & Classification *",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            var vehicleTypeExpanded by remember { mutableStateOf(false) }
                            val allVehicleTypes = VehicleType.getAllTypes()
                            val selectedType = VehicleType.fromString(state.vehicleType)

                            ExposedDropdownMenuBox(
                                expanded = vehicleTypeExpanded,
                                onExpandedChange = { vehicleTypeExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = "${selectedType.emoji} ${selectedType.localizedDisplayName()}",
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = { Icon(Icons.Outlined.Commute, contentDescription = null, tint = roleThemeColor) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleTypeExpanded) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = roleThemeColor,
                                        focusedLeadingIconColor = roleThemeColor
                                    ),
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = vehicleTypeExpanded,
                                    onDismissRequest = { vehicleTypeExpanded = false }
                                ) {
                                    allVehicleTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                    Text("${type.emoji} ${type.localizedDisplayName()}", fontWeight = FontWeight.Bold)
                                                    Text(stringResource(type.descRes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                viewModel.onVehicleTypeChanged(type.name)
                                                vehicleTypeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AgriVoiceInputField(
                        label = when (state.role) {
                            UserRole.FARMER -> "7/12 Extract / Khasra No / GST (Optional)"
                            UserRole.BUYER -> "Pollution / CPCB Reg. Number"
                            UserRole.DRIVER -> "Commercial Driving License / Fleet Permit (Optional)"
                            else -> "Registration Number"
                        },
                        value = state.registrationNumber,
                        onValueChange = viewModel::onRegistrationNumberChanged,
                        placeholder = when (state.role) {
                            UserRole.FARMER -> "e.g. 7/12 Gat No. 128/2 or Krishi ID"
                            UserRole.BUYER -> "e.g. CPCB/MH/NAS/AGRI/2024/09"
                            UserRole.DRIVER -> "e.g. MH15-2020-0012345"
                            else -> "e.g. 27AAAAA0000A1Z5"
                        },
                        leadingIcon = Icons.Outlined.Badge,
                        isRequired = false,
                        roleThemeColor = roleThemeColor,
                        onMicClick = {
                            triggerVoiceInput(
                                prompt = "Speak registration or license number (नोंदणी क्रमांक बोलें)",
                                fieldTitle = "Registration Number"
                            ) { spoken ->
                                viewModel.onRegistrationNumberChanged(spoken.uppercase())
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = viewModel::completeProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = roleThemeColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.RocketLaunch, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Launch AgriStock",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun RoleTabButton(
    role: UserRole,
    label: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = when (role) {
        UserRole.FARMER -> HarvestGreen
        UserRole.BUYER -> Color(0xFF0F766E)
        UserRole.DRIVER -> Color(0xFFD97706)
        else -> HarvestGreen
    }
    val animBg by animateColorAsState(
        targetValue = if (isSelected) activeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "tabBg"
    )

    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        color = animBg,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 12.sp
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) activeColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun AgriVoiceInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    onMicClick: () -> Unit,
    roleThemeColor: Color,
    isRequired: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HarvestGreenContainer.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onMicClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Dictate $label",
                        tint = HarvestGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "Voice",
                        style = MaterialTheme.typography.labelSmall,
                        color = HarvestGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = roleThemeColor
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (value.isNotBlank()) {
                        IconButton(
                            onClick = { onValueChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onMicClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HarvestGreenContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = "Voice Input for $label",
                                    tint = HarvestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = roleThemeColor,
                focusedLeadingIconColor = roleThemeColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
