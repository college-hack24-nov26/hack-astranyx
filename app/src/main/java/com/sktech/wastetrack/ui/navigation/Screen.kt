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
    data object FarmDetail : Screen("farm_detail/{farmId}") {
        fun createRoute(farmId: String) = "farm_detail/$farmId"
    }
    data object AddEditFarm : Screen("add_edit_farm?farmId={farmId}") {
        fun createRoute(farmId: String = "new") = "add_edit_farm?farmId=$farmId"
    }
    data object ListingDetail : Screen("listing_detail/{listingId}") {
        fun createRoute(listingId: String) = "listing_detail/$listingId"
    }
    data object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    data object AddCropCycle : Screen("add_crop_cycle?farmId={farmId}") {
        fun createRoute(farmId: String = "") = "add_crop_cycle?farmId=$farmId"
    }
    data object StubbleValorization : Screen("stubble_valorization?farmId={farmId}&crop={crop}") {
        fun createRoute(farmId: String = "plot_1", crop: String = "Cotton") = "stubble_valorization?farmId=$farmId&crop=$crop"
    }
    data object PaymentHistory : Screen("payment_history")
    data object FindDealer : Screen("find_dealer")
    data object CertificateDetail : Screen("certificate_detail/{certId}") {
        fun createRoute(certId: String = "cert_001") = "certificate_detail/$certId"
    }
    data object CropRecommendation : Screen("crop_recommendation?farmId={farmId}") {
        fun createRoute(farmId: String = "default_farm") = "crop_recommendation?farmId=$farmId"
    }
    data object CropPerformance : Screen("crop_performance?farmId={farmId}&crop={crop}") {
        fun createRoute(farmId: String = "default_farm", crop: String = "Wheat") = "crop_performance?farmId=$farmId&crop=$crop"
    }
    data object CropSuitability : Screen("crop_suitability?farmId={farmId}") {
        fun createRoute(farmId: String = "default_farm") = "crop_suitability?farmId=$farmId"
    }
}
