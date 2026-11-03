package com.sktech.wastetrack.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.R
import com.sktech.wastetrack.domain.model.UserRole
import com.sktech.wastetrack.domain.model.VehicleType
import com.sktech.wastetrack.util.localizedDisplayName
import com.sktech.wastetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onProfileCompleted: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        com.sktech.wastetrack.util.CityHelper.loadCities(context)
    }

    val scope = rememberCoroutineScope()
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
                val detectedCity = city ?: com.sktech.wastetrack.util.LocationHelper.getFormattedLocationName(context, loc.latitude, loc.longitude) ?: "Maharashtra"
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

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
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
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onProfileCompleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.org_registration_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(R.string.profile_setup_subtitle, stringResource(state.role.nameRes)),
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = adaptiveEmeraldContainer(),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldPrimary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (state.role == UserRole.RECYCLER) Icons.Filled.Recycling else Icons.Filled.Factory,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(state.role.nameRes),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = if (state.role == UserRole.RECYCLER)
                                stringResource(R.string.role_recycler_setup_desc)
                            else
                                stringResource(R.string.role_supervisor_setup_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.error != null) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
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

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.account_role_type),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                UserRole.FARMER to Icons.Filled.Agriculture,
                                UserRole.BUYER to Icons.Filled.Storefront,
                                UserRole.DRIVER to Icons.Filled.LocalShipping
                            ).forEach { (role, icon) ->
                                val isSelected = state.role == role
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable { viewModel.onRoleSelected(role) },
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(role.nameRes),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.authorized_rep_name), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = viewModel::onNameChanged,
                            placeholder = { Text(stringResource(R.string.rep_name_placeholder)) },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            when (state.role) {
                                UserRole.FARMER, UserRole.SUPERVISOR -> "Farm / Holding Name"
                                UserRole.BUYER, UserRole.RECYCLER -> "Business / Company Name"
                                UserRole.DRIVER -> "Transport Agency / Fleet Name (or Carrier)"
                                else -> "Organization Name"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = state.organizationName,
                            onValueChange = viewModel::onOrganizationNameChanged,
                            placeholder = {
                                Text(
                                    when (state.role) {
                                        UserRole.FARMER, UserRole.SUPERVISOR -> "e.g. Green Valley Farm (Nashik)"
                                        UserRole.BUYER, UserRole.RECYCLER -> "e.g. AgroBio Processing Ltd."
                                        UserRole.DRIVER -> "e.g. Godavari Express Logistics or Ramesh Transport"
                                        else -> "e.g. AgriStock Enterprise"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    when (state.role) {
                                        UserRole.FARMER, UserRole.SUPERVISOR -> Icons.Outlined.Agriculture
                                        UserRole.BUYER, UserRole.RECYCLER -> Icons.Outlined.Business
                                        UserRole.DRIVER -> Icons.Outlined.LocalShipping
                                        else -> Icons.Outlined.Business
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.industrial_zone_city),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = onFetchLocation,
                                enabled = !state.isFetchingLocation,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                if (state.isFetchingLocation) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = EmeraldPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.detecting_location_status), style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                                } else {
                                    Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.btn_detect_location), style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        var expanded by remember { mutableStateOf(false) }
                        val suggestions = com.sktech.wastetrack.util.CityHelper.getSuggestions(state.city)

                        ExposedDropdownMenuBox(
                            expanded = expanded && suggestions.isNotEmpty(),
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = state.city,
                                onValueChange = {
                                    viewModel.onCityChanged(it)
                                    expanded = true
                                },
                                placeholder = { Text(stringResource(R.string.zone_city_placeholder)) },
                                leadingIcon = { Icon(Icons.Outlined.LocationCity, contentDescription = null, tint = EmeraldPrimary) },
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded && suggestions.isNotEmpty(),
                                onDismissRequest = { expanded = false }
                            ) {
                                suggestions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            viewModel.onCityChanged(selectionOption)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (!state.locationMessage.isNullOrBlank()) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = adaptiveEmeraldContainer(),
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                                    Text(state.locationMessage.orEmpty(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
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

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Industrial Area / Locality / Plant Landmark",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = state.industrialArea,
                            onValueChange = viewModel::onIndustrialAreaChanged,
                            placeholder = { Text(stringResource(R.string.placeholder_address_ambad)) },
                            leadingIcon = { Icon(Icons.Outlined.Place, contentDescription = null, tint = EmeraldPrimary) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (state.role == UserRole.DRIVER) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Vehicle / Truck Registration Number *",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = state.vehicleNumber,
                                onValueChange = viewModel::onVehicleNumberChanged,
                                placeholder = { Text(stringResource(R.string.placeholder_vehicle_reg_driver)) },
                                leadingIcon = { Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = EmeraldPrimary) },
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(R.string.dash_vehicle_capacity_label),
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
                                    leadingIcon = { Icon(Icons.Outlined.Commute, contentDescription = null, tint = EmeraldPrimary) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleTypeExpanded) },
                                    shape = MaterialTheme.shapes.medium,
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

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            when (state.role) {
                                UserRole.RECYCLER, UserRole.BUYER -> stringResource(R.string.mpcb_auth_number)
                                UserRole.DRIVER -> "Commercial Driving License / Permit (Optional)"
                                else -> stringResource(R.string.gstin_license_number)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = state.registrationNumber,
                            onValueChange = viewModel::onRegistrationNumberChanged,
                            placeholder = {
                                Text(
                                    when (state.role) {
                                        UserRole.RECYCLER, UserRole.BUYER -> stringResource(R.string.mpcb_number_placeholder)
                                        UserRole.DRIVER -> "e.g. MH15-2020-0012345"
                                        else -> stringResource(R.string.gstin_placeholder)
                                    }
                                )
                            },
                            leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = EmeraldPrimary) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = viewModel::completeProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !state.isSaving,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.launch_workspace), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
