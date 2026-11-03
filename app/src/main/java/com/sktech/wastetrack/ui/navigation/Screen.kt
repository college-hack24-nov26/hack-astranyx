package com.sktech.wastetrack.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object RecyclerDashboard : Screen("recycler_dashboard")
    data object CropClassify : Screen("crop_classify")
    data object Crops : Screen("crops")
    data object Farms : Screen("farms")
    data object CropDetail : Screen("crop_detail/{cycleId}") {
        fun createRoute(cycleId: String) = "crop_detail/$cycleId"
    }
    data object CreateListing : Screen("create_listing")
    data object Market : Screen("market")
    data object Bids : Screen("bids")
    data object Orders : Screen("orders")
    data object Invoices : Screen("invoices")
    data object Quality : Screen("quality")
    data object Transfers : Screen("transfers")
    data object QRScan : Screen("qr_scan")
    data object QRGenerate : Screen("qr_generate/{transferId}") {
        fun createRoute(transferId: String = "tr_001") = "qr_generate/$transferId"
    }
    data object WeighbridgeSlips : Screen("weighbridge_slips")
    data object DriverDashboard : Screen("driver_dashboard")
    data object FleetTracker : Screen("fleet_tracker")
    data object ShipmentTracking : Screen("shipment_tracking/{transferId}") {
        fun createRoute(transferId: String) = "shipment_tracking/$transferId"
    }
    data object Weather : Screen("weather")
    data object Analytics : Screen("analytics")
    data object Compliance : Screen("compliance")
    data object ESGDashboard : Screen("esg_dashboard")
    data object Agent : Screen("agent")
    data object Settings : Screen("settings")
    data object AdminDashboard : Screen("admin_dashboard")
    data object UserVerification : Screen("user_verification")
    data object DisputeManagement : Screen("dispute_management")
    data object AuditLogs : Screen("audit_logs")
    data object CustomAgroPlan : Screen("custom_agro_plan?farmId={farmId}&crop={crop}") {
        fun createRoute(farmId: String = "default_farm", crop: String = "Wheat") = "custom_agro_plan?farmId=$farmId&crop=$crop"
    }
    data object InvoiceGenerator : Screen("invoice_generator")
    data object InvoiceHistory : Screen("invoice_history")
    data object TransporterHub : Screen("transporter_hub")
    data object Notifications : Screen("notifications")
    data object AddCropCycle : Screen("add_crop_cycle?farmId={farmId}") {
        fun createRoute(farmId: String = "") = "add_crop_cycle?farmId=$farmId"
    }
}
