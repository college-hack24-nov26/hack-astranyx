package com.sktech.wastetrack.ui.screens.compliance

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.model.SustainabilityDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ESGDashboardUiState(
    val isLoading: Boolean = false,
    val documents: List<SustainabilityDocument> = emptyList(),
    val filteredDocuments: List<SustainabilityDocument> = emptyList(),
    val selectedFilter: String = "ALL",
    val totalBiomassKg: Double = 0.0,
    val totalBiomassTonnes: Double = 0.0,
    val totalCo2AvoidedKg: Double = 0.0,
    val totalCo2AvoidedTonnes: Double = 0.0,
    val totalPm25AvoidedKg: Double = 0.0,
    val totalDieselReplacedLiters: Double = 0.0,
    val totalCleanEnergyMwh: Double = 0.0,
    val selectedDocument: SustainabilityDocument? = null,
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null
)

@HiltViewModel
class ESGDashboardViewModel @Inject constructor() : ViewModel() {

    private val defaultDocs = listOf(
        SustainabilityDocument(
            documentId = "doc_esg_001",
            orderId = "ord_mh_901",
            certificateNumber = "AGR-ESG-2026-9912",
            cropType = "Baled Dry Cotton Stalks",
            cropCategory = "COTTON",
            quantityKg = 45000.0,
            co2AvoidedKg = 45000.0 * 1.46,
            pm25AvoidedKg = 45000.0 * 0.003,
            dieselReplacedLiters = 45000.0 * 0.55,
            cleanEnergyMwh = 45000.0 * 0.0034,
            farmerName = "Sachin Raghunath Shinde",
            buyerName = "Mahindra Bio-Energy Plant, Igatpuri",
            farmLocation = "Sinnar Agro Hub, Nashik",
            destinationFacility = "Igatpuri Bio-Power Generation Unit 4",
            diversionPurpose = "Biofuel Briquetting & Thermal Co-Firing",
            methodology = "ISO 14064-2 & MPCB Clean Air Stubble Mitigation Standard",
            generatedAt = System.currentTimeMillis() - 2L * 86400 * 1000,
            status = "VERIFIED",
            digitalHash = "0x8E2A7B1C4D9F0E31",
            issuingAuthority = "Maharashtra Pollution Control Board (MPCB)",
            form10ManifestNumber = "FORM10-MH-2026-9142"
        ),
        SustainabilityDocument(
            documentId = "doc_esg_002",
            orderId = "ord_mh_902",
            certificateNumber = "AGR-ESG-2026-9804",
            cropType = "Crushed Bagasse (Moisture 11.8%)",
            cropCategory = "SUGARCANE",
            quantityKg = 80000.0,
            co2AvoidedKg = 80000.0 * 1.46,
            pm25AvoidedKg = 80000.0 * 0.003,
            dieselReplacedLiters = 80000.0 * 0.55,
            cleanEnergyMwh = 80000.0 * 0.0034,
            farmerName = "Eknath Khairnar",
            buyerName = "Niphad Co-Gen Biomass Power Ltd",
            farmLocation = "Niphad Bio-Energy Cluster",
            destinationFacility = "Niphad High-Pressure Boiler Turbines",
            diversionPurpose = "Biomass Power Cogeneration",
            methodology = "IPCC Tier-1 Open Field Burning Abatement Baseline",
            generatedAt = System.currentTimeMillis() - 8L * 86400 * 1000,
            status = "VERIFIED",
            digitalHash = "0x3F9D1E8A7C2B4065",
            issuingAuthority = "Maharashtra Energy Development Agency (MEDA)",
            form10ManifestNumber = "FORM10-MH-2026-8832"
        ),
        SustainabilityDocument(
            documentId = "doc_esg_003",
            orderId = "ord_mh_903",
            certificateNumber = "AGR-ESG-2026-9650",
            cropType = "Shredded Golden Wheat Straw",
            cropCategory = "WHEAT",
            quantityKg = 65000.0,
            co2AvoidedKg = 65000.0 * 1.46,
            pm25AvoidedKg = 65000.0 * 0.003,
            dieselReplacedLiters = 65000.0 * 0.55,
            cleanEnergyMwh = 65000.0 * 0.0034,
            farmerName = "Ganesh Jadhav",
            buyerName = "Sinnar Recycled Paper & Board Mill",
            farmLocation = "Sinnar Agro Hub, Nashik",
            destinationFacility = "Sinnar Pulping & Bio-Board Facility",
            diversionPurpose = "Industrial Biochar & Unbleached Agro-Pulp",
            methodology = "GHG Protocol Scope 3 Category 1 Avoided Deforestation",
            generatedAt = System.currentTimeMillis() - 14L * 86400 * 1000,
            status = "VERIFIED",
            digitalHash = "0x7D2A1C90B5E3F864",
            issuingAuthority = "Maharashtra State Clean Air Cell",
            form10ManifestNumber = "FORM10-MH-2026-7719"
        ),
        SustainabilityDocument(
            documentId = "doc_esg_004",
            orderId = "ord_mh_904",
            certificateNumber = "AGR-ESG-2026-9410",
            cropType = "Threshed Soybean Straw & Husk",
            cropCategory = "SOYBEAN",
            quantityKg = 30000.0,
            co2AvoidedKg = 30000.0 * 1.46,
            pm25AvoidedKg = 30000.0 * 0.003,
            dieselReplacedLiters = 30000.0 * 0.55,
            cleanEnergyMwh = 30000.0 * 0.0034,
            farmerName = "Dnyaneshwar Borse",
            buyerName = "Malegaon Green Fuel Pelletization Plant",
            farmLocation = "Dindori Agri Cluster",
            destinationFacility = "Malegaon High-Density Densification Plant",
            diversionPurpose = "Biofuel Pellets for Industrial Furnaces",
            methodology = "ISO 14064-2 Greenhouse Gas Abatement Standard",
            generatedAt = System.currentTimeMillis() - 21L * 86400 * 1000,
            status = "VERIFIED",
            digitalHash = "0x1E8F6A902D4C3B77",
            issuingAuthority = "Maharashtra Pollution Control Board (MPCB)",
            form10ManifestNumber = "FORM10-MH-2026-6105"
        )
    )

    private val _uiState = MutableStateFlow(ESGDashboardUiState(isLoading = true))
    val uiState: StateFlow<ESGDashboardUiState> = _uiState.asStateFlow()

    init {
        loadMetrics()
    }

    private fun loadMetrics() {
        val docs = defaultDocs
        val biomassKgTotal = docs.sumOf { it.quantityKg }
        val co2KgTotal = docs.sumOf { it.co2AvoidedKg }
        val biomassTonnesTotal = biomassKgTotal / 1000.0
        val co2TonnesTotal = co2KgTotal / 1000.0
        val pm25Total = docs.sumOf { it.pm25AvoidedKg }
        val dieselTotal = docs.sumOf { it.dieselReplacedLiters }
        val energyTotal = docs.sumOf { it.cleanEnergyMwh }

        _uiState.update { current ->
            current.copy(
                isLoading = false,
                documents = docs,
                filteredDocuments = applyFilter(docs, current.selectedFilter),
                totalBiomassKg = biomassKgTotal,
                totalBiomassTonnes = biomassTonnesTotal,
                totalCo2AvoidedKg = co2KgTotal,
                totalCo2AvoidedTonnes = co2TonnesTotal,
                totalPm25AvoidedKg = pm25Total,
                totalDieselReplacedLiters = dieselTotal,
                totalCleanEnergyMwh = energyTotal
            )
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { current ->
            current.copy(
                selectedFilter = filter,
                filteredDocuments = applyFilter(current.documents, filter)
            )
        }
    }

    private fun applyFilter(docs: List<SustainabilityDocument>, filter: String): List<SustainabilityDocument> {
        return when (filter) {
            "BIOFUEL" -> docs.filter { it.diversionPurpose.contains("Biofuel", ignoreCase = true) || it.diversionPurpose.contains("Pellet", ignoreCase = true) }
            "COGENERATION" -> docs.filter { it.diversionPurpose.contains("Cogeneration", ignoreCase = true) || it.diversionPurpose.contains("Power", ignoreCase = true) }
            "BIOCHAR" -> docs.filter { it.diversionPurpose.contains("Biochar", ignoreCase = true) || it.diversionPurpose.contains("Pulp", ignoreCase = true) }
            else -> docs
        }
    }

    fun selectDocument(doc: SustainabilityDocument?) {
        _uiState.update { it.copy(selectedDocument = doc) }
    }

    fun refresh() {
        loadMetrics()
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportSuccessMessage = null) }
    }
}
