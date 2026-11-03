package com.sktech.wastetrack.ui.screens.crop

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.CropCycle
import com.sktech.wastetrack.domain.model.CropCycleStatus
import com.sktech.wastetrack.domain.model.GrowthStage
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropCycleListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCropDetail: (String) -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToAddCrop: () -> Unit = {},
    viewModel: CropCycleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filterTabs = listOf("All", "Cultivating", "Harvest Ready", "Planned")

    val filteredCrops = remember(state.crops, state.selectedFilter) {
        when (state.selectedFilter) {
            "Cultivating" -> state.crops.filter { it.status == CropCycleStatus.ACTIVE && it.growthStage != GrowthStage.HARVEST_READY }
            "Harvest Ready" -> state.crops.filter { it.growthStage == GrowthStage.HARVEST_READY }
            "Planned" -> state.crops.filter { it.growthStage == GrowthStage.PLANNING }
            else -> state.crops
        }
    }

    val totalAcreage = state.crops.sumOf { it.plotAreaAcres }
    val totalResidue = state.crops.sumOf { it.projectedResidueMt }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Crop Cycles & Cultivation", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${state.crops.size} active fields tracked", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddCrop) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Plant Crop", tint = HarvestGreen)
                    }
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(imageVector = Icons.Filled.DocumentScanner, contentDescription = "Scanner", tint = HarvestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToScanner,
                containerColor = HarvestGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                text = { Text("Scan Crop AI", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HarvestGreenContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SummaryMetricCol("Total Plots", "${state.crops.size} Fields")
                    VerticalDivider(modifier = Modifier.height(36.dp), color = HarvestGreen.copy(alpha = 0.2f))
                    SummaryMetricCol("Total Acreage", String.format("%.1f Ac", totalAcreage))
                    VerticalDivider(modifier = Modifier.height(36.dp), color = HarvestGreen.copy(alpha = 0.2f))
                    SummaryMetricCol("Est. Biomass", String.format("%.1f MT", totalResidue))
                }
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterTabs) { tab ->
                    val isSelected = state.selectedFilter == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(tab) },
                        label = { Text(tab, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HarvestGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCrops, key = { it.id }) { crop ->
                    CropCycleCard(
                        crop = crop,
                        onClick = { onNavigateToCropDetail(crop.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HarvestGreenDark)
    }
}

@Composable
private fun CropCycleCard(
    crop: CropCycle,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = HarvestGreenContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Filled.Agriculture, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = crop.cropName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "${crop.variety} • ${crop.village}", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (crop.growthStage == GrowthStage.HARVEST_READY) GoldenWheatContainer else HarvestGreenContainer
                ) {
                    Text(
                        text = crop.growthStage.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (crop.growthStage == GrowthStage.HARVEST_READY) GoldenWheatDark else HarvestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Growth Progress", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(text = "${(crop.growthStage.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HarvestGreen)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { crop.growthStage.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = HarvestGreen,
                    trackColor = HarvestGreenContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Plot Area", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(text = "${crop.plotAreaAcres} Acres", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Column {
                    Text(text = "Expected Harvest", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(text = dateFormat.format(Date(crop.expectedHarvestMillis)), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Column {
                    Text(text = "Residue Yield", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(text = "~${crop.projectedResidueMt} MT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HarvestGreen)
                }
            }
        }
    }
}
