package com.sktech.wastetrack.ui.screens.notifications

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.model.notification.AgroNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class NotificationsUiState(
    val selectedFilter: String = "ALL",
    val notifications: List<AgroNotification> = listOf(
        AgroNotification(
            id = "notif_001",
            title = "SMAM 50% Subsidy Scheme: Super Seeder Machines",
            body = "Maharashtra Dept of Agriculture Direct Benefit Transfer (DBT) portal is now accepting applications for Happy Seeder and Super Seeder stubble management equipment. Up to 50% subsidy allocated for Nashik & Sinnar farmers.",
            category = "GOVT_SUBSIDY",
            urgencyBadge = "DBT SCHEME OPEN",
            timestampText = "2 hours ago",
            actionText = "Apply DBT Scheme",
            subsidyAmount = 125000.0,
            isRead = false
        ),
        AgroNotification(
            id = "notif_002",
            title = "MPCB Air Act 1981: Zero Stubble Burning Advisory",
            body = "District Collectorate Nashik and MPCB regional office have activated satellite fire-radiance telemetry. Open-field stubble burning carries statutory penalties under Section 19(5). Ensure all crop residue is transported to authorized bio-power depots.",
            category = "MPCB_COMPLIANCE",
            urgencyBadge = "STATUTORY NOTICE",
            timestampText = "5 hours ago",
            actionText = "View MPCB Advisory",
            isRead = false
        ),
        AgroNotification(
            id = "notif_003",
            title = "Mandi Rate Spike: Cotton Stalk Pelletization Demand",
            body = "Mahindra Bio-Energy and Igatpuri Power Unit have increased purchase rates for dry cotton stalks to INR 2,800/MT (up by INR 350/MT). High demand for winter co-firing contracts.",
            category = "PRICE_SURGE",
            urgencyBadge = "RATE SURGE +14%",
            timestampText = "1 day ago",
            actionText = "Open Marketplace",
            isRead = false
        ),
        AgroNotification(
            id = "notif_004",
            title = "Central CRM Scheme: 80% FPO Custom Hiring Subsidy",
            body = "Crop Residue Management (CRM) 2026 Central Sector Scheme grants 80% financial assistance for Farmer Producer Organizations (FPOs) establishing stubble baling and mulching machinery hubs.",
            category = "GOVT_SUBSIDY",
            urgencyBadge = "80% FPO GRANT",
            timestampText = "1 day ago",
            actionText = "View Guidelines",
            subsidyAmount = 240000.0,
            isRead = true
        ),
        AgroNotification(
            id = "notif_005",
            title = "Escrow Milestone Payout Settled: INR 1,33,560",
            body = "Smart Escrow Vault has cleared direct bank payout for 45 MT cotton stalk consignment delivered to Igatpuri MIDC Plant. Form GST EWB-01 gatepass verified.",
            category = "ESCROW_PAYOUT",
            urgencyBadge = "FUNDS CREDITED",
            timestampText = "2 days ago",
            actionText = "View Tax Slip",
            isRead = true
        ),
        AgroNotification(
            id = "notif_006",
            title = "Weather Radar: High Morning Dew Moisture Advisory",
            body = "Relative humidity peaked at 88% in Niphad & Sinnar. Postpone open-air stubble baling until 11:00 AM sunshine window to ensure moisture content stays under 14% gate limit.",
            category = "WEATHER_RADAR",
            urgencyBadge = "MOISTURE ADVISORY",
            timestampText = "3 days ago",
            actionText = "Drying Radar",
            isRead = true
        )
    ),
    val message: String? = null
) {
    val unreadCount: Int
        get() = notifications.count { !it.isRead }

    val activeSubsidySum: Double
        get() = notifications.filter { it.category == "GOVT_SUBSIDY" }.sumOf { it.subsidyAmount ?: 0.0 }

    val filteredList: List<AgroNotification>
        get() = when (selectedFilter) {
            "UNREAD" -> notifications.filter { !it.isRead }
            "GOVT_SUBSIDY" -> notifications.filter { it.category == "GOVT_SUBSIDY" }
            "MPCB_COMPLIANCE" -> notifications.filter { it.category == "MPCB_COMPLIANCE" }
            "PRICE_SURGE" -> notifications.filter { it.category == "PRICE_SURGE" }
            else -> notifications
        }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun markAsRead(id: String) {
        _uiState.update { state ->
            val updated = state.notifications.map { notif ->
                if (notif.id == id) notif.copy(isRead = true) else notif
            }
            state.copy(notifications = updated)
        }
    }

    fun markAllAsRead() {
        _uiState.update { state ->
            val updated = state.notifications.map { it.copy(isRead = true) }
            state.copy(notifications = updated, message = "All notifications marked as read")
        }
    }

    fun dismissNotification(id: String) {
        _uiState.update { state ->
            val updated = state.notifications.filterNot { it.id == id }
            state.copy(notifications = updated, message = "Notification cleared")
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

