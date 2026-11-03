package com.sktech.wastetrack.ui.screens.quality

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.model.quality.BiomassQualityReport
import com.sktech.wastetrack.domain.model.quality.QualityGrade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

data class BiomassQualityUiState(
    val selectedFilter: String = "All",
    val isEntryDialogOpen: Boolean = false,
    val selectedReportForCertificate: BiomassQualityReport? = null,
    val reports: List<BiomassQualityReport> = listOf(
        BiomassQualityReport(
            id = "qlab_001",
            certificateNumber = "QLAB-NSK-2026-0819",
            orderId = "ORD-2026-8812",
            lotNumber = "LOT-COTTON-092",
            commodity = "Baled Dry Cotton Stalk Residue",
            supplierFpo = "Sinnar Agro Producer FPO",
            vehicleNumber = "MH-15-EG-4921",
            grossCalorificValueKcal = 3980.0,
            moisturePercentage = 10.8,
            ashPercentage = 4.1,
            volatileMatterPercentage = 69.2,
            fixedCarbonPercentage = 15.9,
            grade = QualityGrade.GRADE_A_PREMIUM,
            moisturePenaltyDeductionInr = 0.0,
            netEscrowReleaseAmount = 126000.0,
            testedByTechnician = "Kasturi Shinde (QA & Boiler Chemist)",
            testingLab = "Nashik Thermal Bio-Energy Central Testing Lab",
            dateMillis = System.currentTimeMillis() - 2L * 86400 * 1000,
            labStandardReference = "IS 18134 / ASTM D5865-19",
            isEscrowApproved = true,
            digitalSignatureHash = "SHA256:8f9a4c2e1b0d7e6f9a8b1c2d3e4f5a6b7c8d9e0f"
        ),
        BiomassQualityReport(
            id = "qlab_002",
            certificateNumber = "QLAB-NSK-2026-0744",
            orderId = "ORD-2026-8832",
            lotNumber = "LOT-BAGASSE-411",
            commodity = "Crushed Sugarcane Bagasse",
            supplierFpo = "Niphad Bio-Energy Cooperative",
            vehicleNumber = "MH-15-BD-8832",
            grossCalorificValueKcal = 3620.0,
            moisturePercentage = 14.6,
            ashPercentage = 3.8,
            volatileMatterPercentage = 66.8,
            fixedCarbonPercentage = 14.8,
            grade = QualityGrade.GRADE_C_PENALTY,
            moisturePenaltyDeductionInr = 7600.0,
            netEscrowReleaseAmount = 144400.0,
            testedByTechnician = "Kasturi Shinde (QA & Boiler Chemist)",
            testingLab = "Nashik Thermal Bio-Energy Central Testing Lab",
            dateMillis = System.currentTimeMillis() - 4L * 86400 * 1000,
            labStandardReference = "IS 18134 / ASTM D5865-19",
            isEscrowApproved = true,
            digitalSignatureHash = "SHA256:7b6c5d4e3f2a1b0c9d8e7f6a5b4c3d2e1f0a9b8c"
        ),
        BiomassQualityReport(
            id = "qlab_003",
            certificateNumber = "QLAB-NSK-2026-0901",
            orderId = "ORD-2026-9102",
            lotNumber = "LOT-SOYBEAN-108",
            commodity = "Soybean Chaff & Husk Briquettes",
            supplierFpo = "Dindori Farmer Producer Co.",
            vehicleNumber = "MH-15-AB-3341",
            grossCalorificValueKcal = 4120.0,
            moisturePercentage = 9.4,
            ashPercentage = 3.4,
            volatileMatterPercentage = 71.1,
            fixedCarbonPercentage = 16.1,
            grade = QualityGrade.GRADE_A_PREMIUM,
            moisturePenaltyDeductionInr = 0.0,
            netEscrowReleaseAmount = 88000.0,
            testedByTechnician = "Kasturi Shinde (QA & Boiler Chemist)",
            testingLab = "Nashik Thermal Bio-Energy Central Testing Lab",
            dateMillis = System.currentTimeMillis() - 1L * 86400 * 1000,
            labStandardReference = "IS 18134 / ASTM D5865-19",
            isEscrowApproved = false,
            digitalSignatureHash = "SHA256:3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f"
        ),
        BiomassQualityReport(
            id = "qlab_004",
            certificateNumber = "QLAB-NSK-2026-0922",
            orderId = "ORD-2026-9450",
            lotNumber = "LOT-WHEAT-203",
            commodity = "Unshredded Damp Wheat Straw",
            supplierFpo = "Igatpuri Agri Collective",
            vehicleNumber = "MH-15-GH-6019",
            grossCalorificValueKcal = 3150.0,
            moisturePercentage = 18.2,
            ashPercentage = 9.4,
            volatileMatterPercentage = 58.4,
            fixedCarbonPercentage = 14.0,
            grade = QualityGrade.REJECTED,
            moisturePenaltyDeductionInr = 0.0,
            netEscrowReleaseAmount = 0.0,
            testedByTechnician = "Kasturi Shinde (QA & Boiler Chemist)",
            testingLab = "Nashik Thermal Bio-Energy Central Testing Lab",
            dateMillis = System.currentTimeMillis() - 6L * 3600 * 1000,
            labStandardReference = "IS 18134 / ASTM D5865-19",
            isEscrowApproved = false,
            digitalSignatureHash = "SHA256:1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b"
        )
    )
)

@HiltViewModel
class BiomassQualityViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BiomassQualityUiState())
    val uiState: StateFlow<BiomassQualityUiState> = _uiState.asStateFlow()

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun openEntryDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isEntryDialogOpen = isOpen) }
    }

    fun selectReportForCertificate(report: BiomassQualityReport?) {
        _uiState.update { it.copy(selectedReportForCertificate = report) }
    }

    fun approveEscrow(reportId: String) {
        _uiState.update { state ->
            val updated = state.reports.map {
                if (it.id == reportId) it.copy(isEscrowApproved = true) else it
            }
            state.copy(
                reports = updated,
                selectedReportForCertificate = state.selectedReportForCertificate?.let {
                    if (it.id == reportId) it.copy(isEscrowApproved = true) else it
                }
            )
        }
    }

    fun submitGateLabTest(
        orderId: String,
        lotNumber: String,
        commodity: String,
        supplierFpo: String,
        vehicleNumber: String,
        gcv: Double,
        moisture: Double,
        ash: Double,
        grossEscrowAmount: Double
    ) {
        val grade = when {
            moisture > 16.0 || ash > 8.0 -> QualityGrade.REJECTED
            moisture > 14.0 -> QualityGrade.GRADE_C_PENALTY
            moisture > 12.0 -> QualityGrade.GRADE_B_STANDARD
            else -> QualityGrade.GRADE_A_PREMIUM
        }

        val penaltyDeduction = when (grade) {
            QualityGrade.GRADE_C_PENALTY -> grossEscrowAmount * 0.05
            QualityGrade.GRADE_B_STANDARD -> grossEscrowAmount * 0.02
            QualityGrade.REJECTED -> grossEscrowAmount
            QualityGrade.GRADE_A_PREMIUM -> 0.0
        }

        val netRelease = if (grade == QualityGrade.REJECTED) 0.0 else grossEscrowAmount - penaltyDeduction
        val newCertNo = "QLAB-NSK-2026-0" + (930..999).random()

        val newReport = BiomassQualityReport(
            id = UUID.randomUUID().toString(),
            certificateNumber = newCertNo,
            orderId = orderId,
            lotNumber = lotNumber,
            commodity = commodity,
            supplierFpo = supplierFpo,
            vehicleNumber = vehicleNumber,
            grossCalorificValueKcal = gcv,
            moisturePercentage = moisture,
            ashPercentage = ash,
            volatileMatterPercentage = kotlin.random.Random.nextDouble(65.0, 72.0),
            fixedCarbonPercentage = kotlin.random.Random.nextDouble(14.0, 17.0),
            grade = grade,
            moisturePenaltyDeductionInr = penaltyDeduction,
            netEscrowReleaseAmount = netRelease,
            testedByTechnician = "Kasturi Shinde (QA & Boiler Chemist)",
            testingLab = "Nashik Thermal Bio-Energy Central Testing Lab",
            dateMillis = System.currentTimeMillis(),
            labStandardReference = "IS 18134 / ASTM D5865-19",
            isEscrowApproved = grade == QualityGrade.GRADE_A_PREMIUM,
            digitalSignatureHash = "SHA256:" + UUID.randomUUID().toString().replace("-", "")
        )

        _uiState.update {
            it.copy(
                reports = listOf(newReport) + it.reports,
                isEntryDialogOpen = false,
                selectedReportForCertificate = newReport
            )
        }
    }
}

