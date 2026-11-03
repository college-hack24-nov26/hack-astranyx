package com.sktech.wastetrack.ui.screens.admin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AdminOrganization(
    val orgId: String,
    val name: String,
    val type: String,
    val address: String,
    val district: String,
    val state: String,
    val gstin: String,
    val mpcbConsentNo: String,
    val contactPhone: String,
    val contactEmail: String,
    val acceptedMaterials: List<String>,
    val minOrderQuantityKg: Double,
    val maxPickupRadiusKm: Double,
    val verificationStatus: String,
    val verifiedAt: Long? = null,
    val verifiedBy: String? = null,
    val registrationDate: Long = System.currentTimeMillis() - 86400000L * 3
)

data class AdminDispute(
    val disputeId: String,
    val orderId: String,
    val raisedBy: String,
    val respondent: String,
    val reason: String,
    val claimedWeightLossKg: Double? = null,
    val reportedMoisturePercent: Double? = null,
    val originalAmount: Double,
    val proposedSettlementAmount: Double,
    val status: String,
    val evidenceSummary: String,
    val timestamp: Long = System.currentTimeMillis() - 86400000L * 1,
    val resolutionNotes: String? = null,
    val truckNumber: String = "MH-15-EG-4921",
    val slipNumber: String = "WB-NSK-2026-8812"
)

data class AdminAuditEvent(
    val auditId: String,
    val actorId: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val timestamp: Long,
    val reason: String,
    val outcome: String,
    val blockHash: String
)

data class PlatformKpis(
    val totalActiveListings: Int = 184,
    val totalOrdersCompleted: Int = 412,
    val totalVolumeDivertedTonnes: Double = 12480.0,
    val totalEscrowVolumeInr: Double = 4860000.0,
    val pendingVerifications: Int = 3,
    val activeDisputes: Int = 2,
    val regulatoryComplianceScore: Double = 98.4
)

data class AdminUiState(
    val isLoading: Boolean = false,
    val kpis: PlatformKpis = PlatformKpis(),
    val organizations: List<AdminOrganization> = emptyList(),
    val disputes: List<AdminDispute> = emptyList(),
    val auditEvents: List<AdminAuditEvent> = emptyList(),
    val filteredAudits: List<AdminAuditEvent> = emptyList(),
    val selectedOrganization: AdminOrganization? = null,
    val selectedDispute: AdminDispute? = null,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null,
    val auditSearchQuery: String = "",
    val auditFilterType: String = "ALL",
    val selectedPlantScope: String = "All Hubs",
    val selectedFiscalPeriod: String = "Current Month"
)

@HiltViewModel
class AdminViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState(isLoading = true))
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }

    private fun loadData() {
        val initialOrgs = listOf(
            AdminOrganization(
                orgId = "ORG-001",
                name = "Mahagenco Bio-Energy Thermal Plant",
                type = "Biomass Thermal Power Plant",
                address = "Plot B-14, Bhosari MIDC, Pune",
                district = "Pune",
                state = "Maharashtra",
                gstin = "27AAACM9988D1Z2",
                mpcbConsentNo = "MPCB/RO/PUN/2025/1192",
                contactPhone = "+91 20 2712 8899",
                contactEmail = "procure@mahagenco-bio.in",
                acceptedMaterials = listOf("Wheat Straw", "Paddy Stubble", "Sugarcane Bagasse"),
                minOrderQuantityKg = 10000.0,
                maxPickupRadiusKm = 250.0,
                verificationStatus = "VERIFIED",
                verifiedAt = System.currentTimeMillis() - 86400000L * 30,
                verifiedBy = "ADMIN_KASTURI"
            ),
            AdminOrganization(
                orgId = "ORG-002",
                name = "Khandesh Biomass Briquettes & Pellets",
                type = "Bio-Pellet & Briquetting Cluster",
                address = "Survey 88, MIDC Phase 2, Jalgaon",
                district = "Jalgaon",
                state = "Maharashtra",
                gstin = "27AAAKB3322H1Z7",
                mpcbConsentNo = "MPCB/RO/JAL/2026/0411",
                contactPhone = "+91 257 223 4455",
                contactEmail = "supply@khandeshbriquettes.com",
                acceptedMaterials = listOf("Cotton Stalks", "Soybean Husks", "Maize Stalks"),
                minOrderQuantityKg = 5000.0,
                maxPickupRadiusKm = 120.0,
                verificationStatus = "PENDING_REVIEW",
                registrationDate = System.currentTimeMillis() - 86400000L * 2
            ),
            AdminOrganization(
                orgId = "ORG-003",
                name = "Panchaganga Organic Composting Co.",
                type = "Bio-Fertilizer & Organic Manure Plant",
                address = "Gat 412, Shiroli Industrial Area, Kolhapur",
                district = "Kolhapur",
                state = "Maharashtra",
                gstin = "27AAAGP4455K1Z9",
                mpcbConsentNo = "MPCB/RO/KOL/2026/0782",
                contactPhone = "+91 231 268 9900",
                contactEmail = "info@panchagangabiotech.in",
                acceptedMaterials = listOf("Sugarcane Trash", "Vegetable Scrap", "Horticultural Biomass"),
                minOrderQuantityKg = 3000.0,
                maxPickupRadiusKm = 80.0,
                verificationStatus = "PENDING_REVIEW",
                registrationDate = System.currentTimeMillis() - 86400000L * 1
            ),
            AdminOrganization(
                orgId = "ORG-004",
                name = "Vidarbha Agro Fiber Mills",
                type = "Particle Board & Agro-Paper Pulp",
                address = "Butibori Industrial Area, Nagpur",
                district = "Nagpur",
                state = "Maharashtra",
                gstin = "27AAAFV6677L1Z4",
                mpcbConsentNo = "MPCB/RO/NGP/2026/0129",
                contactPhone = "+91 7104 288 333",
                contactEmail = "commercial@vidarbhafiber.com",
                acceptedMaterials = listOf("Cotton Stalks", "Bagasse", "Rice Husk"),
                minOrderQuantityKg = 8000.0,
                maxPickupRadiusKm = 180.0,
                verificationStatus = "PENDING_REVIEW",
                registrationDate = System.currentTimeMillis() - 86400000L * 4
            ),
            AdminOrganization(
                orgId = "ORG-005",
                name = "Godavari Green Biofuels Ltd",
                type = "2G Bio-Ethanol Commercial Biorefinery",
                address = "Industrial Sector 5, Kopargaon MIDC, Ahmednagar",
                district = "Ahmednagar",
                state = "Maharashtra",
                gstin = "27AABCG1204K1ZX",
                mpcbConsentNo = "MPCB/RO/NSK/2025/9044",
                contactPhone = "+91 2423 224 819",
                contactEmail = "procure@godavaribiofuels.in",
                acceptedMaterials = listOf("Sugarcane Bagasse", "Corn Stover", "Sweet Sorghum Stalks"),
                minOrderQuantityKg = 15000.0,
                maxPickupRadiusKm = 200.0,
                verificationStatus = "VERIFIED",
                verifiedAt = System.currentTimeMillis() - 86400000L * 25,
                verifiedBy = "ADMIN_KASTURI"
            )
        )

        val initialDisputes = listOf(
            AdminDispute(
                disputeId = "DSP-2026-081",
                orderId = "ORD-2026-8812",
                raisedBy = "Mahagenco Bio-Energy Ltd (Buyer)",
                respondent = "Sinnar Agro Producer FPO (Farmer)",
                reason = "Weighbridge Net Weight Variance exceeding 4.2% tolerance",
                claimedWeightLossKg = 420.0,
                reportedMoisturePercent = 14.8,
                originalAmount = 126000.0,
                proposedSettlementAmount = 118400.0,
                status = "OPEN",
                evidenceSummary = "Gross weight at Sinnar weighbridge registered 24,180 kg; Gross weight at Bhosari MIDC registered 23,760 kg (-420 kg tare variance). Escrow hold INR 126,000 active.",
                truckNumber = "MH-15-EG-4921",
                slipNumber = "WB-NSK-2026-8812",
                timestamp = System.currentTimeMillis() - 86400000L * 1
            ),
            AdminDispute(
                disputeId = "DSP-2026-079",
                orderId = "ORD-2026-8832",
                raisedBy = "Khandesh Biomass Pellets (Buyer)",
                respondent = "Niphad Bio-Energy Cooperative",
                reason = "Excessive Moisture in Sugarcane Bagasse Lot (18.2% vs 12% contractual threshold)",
                claimedWeightLossKg = 650.0,
                reportedMoisturePercent = 18.2,
                originalAmount = 152000.0,
                proposedSettlementAmount = 144400.0,
                status = "OPEN",
                evidenceSummary = "Lab test QLAB-NSK-2026-0744 recorded 18.2% moisture content. Penalty deduction INR 7,600 proposed under ASTM D5865-19 standards.",
                truckNumber = "MH-15-BD-8832",
                slipNumber = "WB-NSK-2026-8832",
                timestamp = System.currentTimeMillis() - 86400000L * 2
            ),
            AdminDispute(
                disputeId = "DSP-2026-064",
                orderId = "ORD-2026-7901",
                raisedBy = "Godavari Green Biofuels (Buyer)",
                respondent = "Rahata Farmers Collective",
                reason = "Transit delay beyond 24-hour delivery window causing moisture pickup",
                claimedWeightLossKg = 180.0,
                reportedMoisturePercent = 11.2,
                originalAmount = 94000.0,
                proposedSettlementAmount = 92000.0,
                status = "RESOLVED",
                evidenceSummary = "Arbitrated by Platform Admin on 01 Nov 2026. Escrow released INR 92,000 to farmer, INR 2,000 logistics credit granted to buyer.",
                resolutionNotes = "Transit delay due to highway maintenance verified via GPS telematics. Mutual settlement executed.",
                truckNumber = "MH-17-AG-7711",
                slipNumber = "WB-KPG-2026-7901",
                timestamp = System.currentTimeMillis() - 86400000L * 3
            )
        )

        val initialAudits = listOf(
            AdminAuditEvent(
                auditId = "AUD-2026-9041",
                actorId = "USR-KASTURI-SHINDE",
                action = "ESCROW_ARBITRATION",
                entityType = "DISPUTE",
                entityId = "DSP-2026-064",
                timestamp = System.currentTimeMillis() - 1000L * 60 * 45,
                reason = "Arbitrated escrow settlement for Rahata Farmers Collective (INR 92,000 released)",
                outcome = "RESOLVED",
                blockHash = "SHA256:4a8c9b2e1f0d3a7e5c6b8d9a0e1f2b3c4d5e6f7a"
            ),
            AdminAuditEvent(
                auditId = "AUD-2026-9038",
                actorId = "USR-MAHAGENCO-PROCURE",
                action = "WEIGHBRIDGE_DISCREPANCY_FLAGGED",
                entityType = "ORDER",
                entityId = "ORD-2026-8812",
                timestamp = System.currentTimeMillis() - 1000L * 60 * 180,
                reason = "Weight variance 420 kg flagged between source and destination weighbridges",
                outcome = "FLAGGED",
                blockHash = "SHA256:7f1e2d3c4b5a60789a0b1c2d3e4f5a6b7c8d9e0f"
            ),
            AdminAuditEvent(
                auditId = "AUD-2026-9022",
                actorId = "USR-KASTURI-SHINDE",
                action = "ORG_KYC_VERIFIED",
                entityType = "ORGANIZATION",
                entityId = "ORG-001",
                timestamp = System.currentTimeMillis() - 1000L * 3600 * 8,
                reason = "Mahagenco Bio-Energy statutory GSTIN and MPCB consents approved",
                outcome = "SUCCESS",
                blockHash = "SHA256:9b8a7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b"
            ),
            AdminAuditEvent(
                auditId = "AUD-2026-9014",
                actorId = "USR-LOGISTICS-DISPATCH",
                action = "CONSIGNMENT_GATEPASS_GENERATED",
                entityType = "SHIPMENT",
                entityId = "TR-2026-081",
                timestamp = System.currentTimeMillis() - 1000L * 3600 * 14,
                reason = "Optical QR gatepass token created for Truck MH-15-EG-4921",
                outcome = "SUCCESS",
                blockHash = "SHA256:1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b"
            ),
            AdminAuditEvent(
                auditId = "AUD-2026-8995",
                actorId = "USR-RAJESH-PATIL",
                action = "LISTING_PUBLISHED",
                entityType = "LISTING",
                entityId = "LST-COTTON-092",
                timestamp = System.currentTimeMillis() - 1000L * 3600 * 22,
                reason = "Published 24 MT dry cotton stalk biomass with AI benchmark price INR 2.40/kg",
                outcome = "SUCCESS",
                blockHash = "SHA256:6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b0a9f8e7d"
            ),
            AdminAuditEvent(
                auditId = "AUD-2026-8970",
                actorId = "USR-BUYER-GODAVARI",
                action = "BID_AWARDED",
                entityType = "BID",
                entityId = "BID-BAGASSE-411",
                timestamp = System.currentTimeMillis() - 1000L * 3600 * 36,
                reason = "Contract finalized for 40 MT sugarcane bagasse co-firing supply",
                outcome = "SUCCESS",
                blockHash = "SHA256:3c2b1a0f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b"
            )
        )

        _uiState.update { current ->
            current.copy(
                isLoading = false,
                organizations = initialOrgs,
                disputes = initialDisputes,
                auditEvents = initialAudits,
                filteredAudits = filterAuditsInternal(initialAudits, current.auditSearchQuery, current.auditFilterType),
                kpis = PlatformKpis(
                    totalActiveListings = 184,
                    totalOrdersCompleted = 412,
                    totalVolumeDivertedTonnes = 12480.0,
                    totalEscrowVolumeInr = 4860000.0,
                    pendingVerifications = initialOrgs.count { it.verificationStatus == "PENDING_REVIEW" },
                    activeDisputes = initialDisputes.count { it.status == "OPEN" },
                    regulatoryComplianceScore = 98.4
                )
            )
        }
    }

    fun verifyOrganization(orgId: String, approved: Boolean, notes: String? = null) {
        _uiState.update { current ->
            val updatedOrgs = current.organizations.map { org ->
                if (org.orgId == orgId) {
                    org.copy(
                        verificationStatus = if (approved) "VERIFIED" else "REJECTED",
                        verifiedAt = System.currentTimeMillis(),
                        verifiedBy = "ADMIN_KASTURI"
                    )
                } else org
            }
            val newAuditEvent = AdminAuditEvent(
                auditId = "AUD-2026-${System.currentTimeMillis().toString().takeLast(4)}",
                actorId = "USR-KASTURI-SHINDE",
                action = if (approved) "ORG_KYC_VERIFIED" else "ORG_KYC_REJECTED",
                entityType = "ORGANIZATION",
                entityId = orgId,
                timestamp = System.currentTimeMillis(),
                reason = notes ?: (if (approved) "Statutory credentials certified" else "Application rejected"),
                outcome = if (approved) "SUCCESS" else "REJECTED",
                blockHash = "SHA256:${java.util.UUID.randomUUID().toString().replace("-", "")}"
            )
            val updatedAudits = listOf(newAuditEvent) + current.auditEvents
            current.copy(
                organizations = updatedOrgs,
                auditEvents = updatedAudits,
                filteredAudits = filterAuditsInternal(updatedAudits, current.auditSearchQuery, current.auditFilterType),
                selectedOrganization = null,
                feedbackMessage = if (approved) "Organization verified and certified successfully." else "Organization verification rejected."
            )
        }
    }

    fun resolveDispute(disputeId: String, resolutionNotes: String, settlementAmount: Double) {
        _uiState.update { current ->
            val updatedDisputes = current.disputes.map { dispute ->
                if (dispute.disputeId == disputeId) {
                    dispute.copy(
                        status = "RESOLVED",
                        proposedSettlementAmount = settlementAmount,
                        resolutionNotes = resolutionNotes
                    )
                } else dispute
            }
            val newAuditEvent = AdminAuditEvent(
                auditId = "AUD-2026-${System.currentTimeMillis().toString().takeLast(4)}",
                actorId = "USR-KASTURI-SHINDE",
                action = "ESCROW_ARBITRATION",
                entityType = "DISPUTE",
                entityId = disputeId,
                timestamp = System.currentTimeMillis(),
                reason = resolutionNotes,
                outcome = "RESOLVED",
                blockHash = "SHA256:${java.util.UUID.randomUUID().toString().replace("-", "")}"
            )
            val updatedAudits = listOf(newAuditEvent) + current.auditEvents
            current.copy(
                disputes = updatedDisputes,
                auditEvents = updatedAudits,
                filteredAudits = filterAuditsInternal(updatedAudits, current.auditSearchQuery, current.auditFilterType),
                selectedDispute = null,
                feedbackMessage = "Dispute resolved and escrow settlement executed."
            )
        }
    }

    fun selectOrganization(org: AdminOrganization?) {
        _uiState.update { it.copy(selectedOrganization = org) }
    }

    fun selectDispute(dispute: AdminDispute?) {
        _uiState.update { it.copy(selectedDispute = dispute) }
    }

    fun setAuditSearch(query: String) {
        _uiState.update { current ->
            current.copy(
                auditSearchQuery = query,
                filteredAudits = filterAuditsInternal(current.auditEvents, query, current.auditFilterType)
            )
        }
    }

    fun setAuditFilterType(type: String) {
        _uiState.update { current ->
            current.copy(
                auditFilterType = type,
                filteredAudits = filterAuditsInternal(current.auditEvents, current.auditSearchQuery, type)
            )
        }
    }

    fun setPlantScope(scope: String) {
        _uiState.update { it.copy(selectedPlantScope = scope) }
    }

    fun setFiscalPeriod(period: String) {
        _uiState.update { it.copy(selectedFiscalPeriod = period) }
    }

    private fun filterAuditsInternal(
        audits: List<AdminAuditEvent>,
        query: String,
        type: String
    ): List<AdminAuditEvent> {
        return audits.filter { event ->
            val matchesType = if (type == "ALL") true else event.entityType.equals(type, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                event.action.contains(query, ignoreCase = true) ||
                event.actorId.contains(query, ignoreCase = true) ||
                event.entityId.contains(query, ignoreCase = true) ||
                event.reason.contains(query, ignoreCase = true)
            }
            matchesType && matchesQuery
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }
}

