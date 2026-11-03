package com.sktech.wastetrack.ui.screens.compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EsgCertificate(
    val id: String,
    val certificateNumber: String,
    val title: String,
    val categoryName: String,
    val tonnageMt: Double,
    val co2SavedMt: Double,
    val issueDateMillis: Long,
    val issuingAuthority: String,
    val digitalHash: String,
    val isForm10Filed: Boolean
)

data class ComplianceUiState(
    val annualTargetMt: Double = 250.0,
    val fulfilledTonnageMt: Double = 210.0,
    val fulfillmentPercentage: Float = 0.84f,
    val co2TotalAvertedMt: Double = 162.4,
    val certificates: List<EsgCertificate> = listOf(
        EsgCertificate(
            id = "cert_001",
            certificateNumber = "ESG-MH-2026-9912",
            title = "Cotton Residue Stubble Mitigation",
            categoryName = "Dry Cotton Stalks",
            tonnageMt = 45.0,
            co2SavedMt = 34.8,
            issueDateMillis = System.currentTimeMillis() - 2L * 86400 * 1000,
            issuingAuthority = "Maharashtra Pollution Control Board (MPCB)",
            digitalHash = "0x8E2A7B1C4D9F0E31",
            isForm10Filed = true
        ),
        EsgCertificate(
            id = "cert_002",
            certificateNumber = "ESG-MH-2026-9804",
            title = "Bio-Mass Industrial Co-Gen Utilization",
            categoryName = "Crushed Bagasse",
            tonnageMt = 80.0,
            co2SavedMt = 61.9,
            issueDateMillis = System.currentTimeMillis() - 8L * 86400 * 1000,
            issuingAuthority = "Maharashtra Pollution Control Board (MPCB)",
            digitalHash = "0x3F9D1E8A7C2B4065",
            isForm10Filed = true
        ),
        EsgCertificate(
            id = "cert_003",
            certificateNumber = "ESG-MH-2026-9650",
            title = "Clean Energy Bio-Fuel Pelleting Mandate",
            categoryName = "Soybean Biomass",
            tonnageMt = 35.0,
            co2SavedMt = 27.1,
            issueDateMillis = System.currentTimeMillis() - 15L * 86400 * 1000,
            issuingAuthority = "Maharashtra Energy Development Agency (MEDA)",
            digitalHash = "0x7C1B4A9E2D8F3012",
            isForm10Filed = true
        ),
        EsgCertificate(
            id = "cert_004",
            certificateNumber = "ESG-MH-2026-9410",
            title = "Wheat Straw Soil Degradation Offset",
            categoryName = "Wheat Straw",
            tonnageMt = 50.0,
            co2SavedMt = 38.6,
            issueDateMillis = System.currentTimeMillis() - 24L * 86400 * 1000,
            issuingAuthority = "Maharashtra State Clean Air Cell",
            digitalHash = "0x5A8E3C1B7D2F9043",
            isForm10Filed = true
        )
    ),
    val selectedCertificate: EsgCertificate? = null,
    val showExportToast: Boolean = false
)

@HiltViewModel
class ComplianceViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ComplianceUiState())
    val uiState: StateFlow<ComplianceUiState> = _uiState.asStateFlow()

    fun selectCertificate(cert: EsgCertificate) {
        _uiState.update { it.copy(selectedCertificate = cert) }
    }

    fun dismissCertificateDialog() {
        _uiState.update { it.copy(selectedCertificate = null) }
    }

    fun triggerExportReport() {
        _uiState.update { it.copy(showExportToast = true) }
    }

    fun dismissToast() {
        _uiState.update { it.copy(showExportToast = false) }
    }
}
