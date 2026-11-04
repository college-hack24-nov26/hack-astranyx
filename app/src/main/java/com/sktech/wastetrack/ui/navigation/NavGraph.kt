package com.sktech.wastetrack.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sktech.wastetrack.ui.screens.auth.LoginScreen
import com.sktech.wastetrack.ui.screens.auth.SignUpScreen
import com.sktech.wastetrack.ui.screens.bid.BidMarketScreen
import com.sktech.wastetrack.ui.screens.crop.CropClassifyScreen
import com.sktech.wastetrack.ui.screens.crop.CropCycleDetailScreen
import com.sktech.wastetrack.ui.screens.crop.CropCycleListScreen
import com.sktech.wastetrack.ui.screens.crop.AddCropCycleScreen

import com.sktech.wastetrack.ui.screens.dashboard.DriverDashboardScreen
import com.sktech.wastetrack.ui.screens.transporter.TransporterCommandHubScreen
import com.sktech.wastetrack.ui.screens.dashboard.FarmerDashboardScreen
import com.sktech.wastetrack.ui.screens.marketplace.CreateListingScreen
import com.sktech.wastetrack.ui.screens.marketplace.MarketBrowseScreen
import com.sktech.wastetrack.ui.screens.marketplace.ListingDetailScreen
import com.sktech.wastetrack.ui.screens.order.OrderDetailScreen
import com.sktech.wastetrack.ui.screens.onboarding.OnboardingScreen
import com.sktech.wastetrack.ui.screens.order.OrderListScreen
import com.sktech.wastetrack.ui.screens.dashboard.RecyclerDashboardScreen
import com.sktech.wastetrack.ui.screens.invoice.InvoiceScreen
import com.sktech.wastetrack.ui.screens.invoice.InvoiceGeneratorScreen
import com.sktech.wastetrack.ui.screens.invoice.InvoiceHistoryScreen
import com.sktech.wastetrack.ui.screens.settings.SettingsScreen
import com.sktech.wastetrack.ui.screens.notifications.NotificationsScreen
import com.sktech.wastetrack.ui.screens.payment.PaymentHistoryScreen
import com.sktech.wastetrack.ui.screens.dealer.FindDealerScreen
import com.sktech.wastetrack.ui.screens.transfer.TransferScreen
import com.sktech.wastetrack.ui.screens.transfer.ShipmentTrackingScreen
import com.sktech.wastetrack.ui.screens.transfer.FleetTrackerScreen
import com.sktech.wastetrack.ui.screens.transfer.QRScanScreen
import com.sktech.wastetrack.ui.screens.transfer.QRGenerateScreen
import com.sktech.wastetrack.ui.screens.transfer.WeighbridgeSlipScreen
import com.sktech.wastetrack.ui.screens.weather.WeatherDashboardScreen
import com.sktech.wastetrack.ui.screens.analytics.AnalyticsScreen
import com.sktech.wastetrack.ui.screens.compliance.ComplianceScreen
import com.sktech.wastetrack.ui.screens.compliance.CertificateViewScreen
import com.sktech.wastetrack.ui.screens.agroplan.CropRecommendationScreen
import com.sktech.wastetrack.ui.screens.agroplan.CropPerformanceAssessmentScreen
import com.sktech.wastetrack.ui.screens.compliance.ESGDashboardScreen
import com.sktech.wastetrack.ui.screens.farm.FarmListScreen
import com.sktech.wastetrack.ui.screens.farm.FarmDetailScreen
import com.sktech.wastetrack.ui.screens.farm.AddEditFarmScreen

import com.sktech.wastetrack.ui.screens.agent.AgentScreen
import com.sktech.wastetrack.ui.screens.quality.BiomassQualityScreen
import com.sktech.wastetrack.ui.screens.admin.AdminDashboardScreen
import com.sktech.wastetrack.ui.screens.admin.UserVerificationScreen
import com.sktech.wastetrack.ui.screens.admin.DisputeManagementScreen
import com.sktech.wastetrack.ui.screens.admin.AuditLogScreen
import com.sktech.wastetrack.ui.screens.agroplan.CustomAgroPlanScreen
import com.sktech.wastetrack.ui.screens.agroplan.StubbleValorizationDeskScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onProfileCompleted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            OnboardingScreen(
                onFinish = {
                    val authPrefs = context.getSharedPreferences("wastetrack_prefs", android.content.Context.MODE_PRIVATE)
                    val isLoggedIn = authPrefs.getBoolean(com.sktech.wastetrack.util.Constants.KEY_IS_LOGGED_IN, false)
                    val nextRoute = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            FarmerDashboardScreen(
                onNavigateToCrops = {
                    navController.navigate(Screen.Crops.route)
                },
                onNavigateToMarket = {
                    navController.navigate(Screen.Market.route)
                },
                onNavigateToWeather = {
                    navController.navigate(Screen.Weather.route)
                },
                onNavigateToTransfers = {
                    navController.navigate(Screen.Transfers.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route)
                },
                onNavigateToFarms = {
                    navController.navigate(Screen.Farms.route)
                },
                onNavigateToAgent = {
                    navController.navigate(Screen.Agent.route)
                },
                onNavigateToESGDashboard = {
                    navController.navigate(Screen.ESGDashboard.route)
                },
                onNavigateToCustomAgroPlan = { farmId ->
                    navController.navigate(Screen.CustomAgroPlan.createRoute(farmId))
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }
        composable(Screen.Crops.route) {
            CropCycleListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCropDetail = { cycleId ->
                    navController.navigate(Screen.CropDetail.createRoute(cycleId))
                },
                onNavigateToScanner = {
                    navController.navigate(Screen.CropClassify.route)
                },
                onNavigateToAddCrop = {
                    navController.navigate(Screen.AddCropCycle.createRoute())
                }
            )
        }
        composable(
            route = Screen.CropDetail.route,
            arguments = listOf(navArgument("cycleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cycleId = backStackEntry.arguments?.getString("cycleId") ?: ""
            CropCycleDetailScreen(
                cycleId = cycleId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToScanner = {
                    navController.navigate(Screen.CropClassify.route)
                },
                onNavigateToMarket = {
                    navController.navigate(Screen.Market.route)
                }
            )
        }
        composable(Screen.CropClassify.route) {
            CropClassifyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMarket = {
                    navController.navigate(Screen.CreateListing.route)
                }
            )
        }
        composable(Screen.CreateListing.route) {
            CreateListingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMarket = {
                    navController.navigate(Screen.Market.route)
                }
            )
        }
        composable(Screen.Bids.route) {
            BidMarketScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Orders.route) {
            OrderListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTracking = { transferId ->
                    navController.navigate(Screen.ShipmentTracking.createRoute(transferId))
                },
                onNavigateToDetail = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onNavigateToPayments = {
                    navController.navigate(Screen.PaymentHistory.route)
                }
            )
        }
        composable(Screen.RecyclerDashboard.route) {
            RecyclerDashboardScreen(
                onNavigateToMarket = {
                    navController.navigate(Screen.Market.route)
                },
                onNavigateToBids = {
                    navController.navigate(Screen.Bids.route)
                },
                onNavigateToTransfers = {
                    navController.navigate(Screen.Transfers.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.route)
                },
                onNavigateToInvoices = {
                    navController.navigate(Screen.Invoices.route)
                },
                onNavigateToQuality = {
                    navController.navigate(Screen.Quality.route)
                },
                onNavigateToESGDashboard = {
                    navController.navigate(Screen.ESGDashboard.route)
                }
            )
        }
        composable(Screen.Invoices.route) {
            InvoiceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Quality.route) {
            BiomassQualityScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Market.route) {
            MarketBrowseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                }
            )
        }
        composable(Screen.Transfers.route) {
            TransferScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToQRGenerate = { transferId ->
                    navController.navigate(Screen.QRGenerate.createRoute(transferId))
                }
            )
        }
        composable(Screen.DriverDashboard.route) {
            DriverDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTransfers = {
                    navController.navigate(Screen.Transfers.route)
                },
                onNavigateToTracking = { transferId ->
                    navController.navigate(Screen.ShipmentTracking.createRoute(transferId))
                },
                onNavigateToFleet = {
                    navController.navigate(Screen.FleetTracker.route)
                },
                onNavigateToQRScan = {
                    navController.navigate(Screen.QRScan.route)
                },
                onNavigateToWeighbridge = {
                    navController.navigate(Screen.WeighbridgeSlips.route)
                },
                onNavigateToQRGenerate = { transferId ->
                    navController.navigate(Screen.QRGenerate.createRoute(transferId))
                },
                onNavigateToTransporterHub = {
                    navController.navigate(Screen.TransporterHub.route)
                }
            )
        }
        composable(Screen.QRScan.route) {
            QRScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.QRGenerate.route,
            arguments = listOf(navArgument("transferId") { type = NavType.StringType; defaultValue = "tr_001" })
        ) { backStackEntry ->
            val transferId = backStackEntry.arguments?.getString("transferId") ?: "tr_001"
            QRGenerateScreen(
                transferId = transferId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.WeighbridgeSlips.route) {
            WeighbridgeSlipScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.FleetTracker.route) {
            FleetTrackerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTracking = { transferId ->
                    navController.navigate(Screen.ShipmentTracking.createRoute(transferId))
                }
            )
        }
        composable(
            route = Screen.ShipmentTracking.route,
            arguments = listOf(navArgument("transferId") { type = NavType.StringType })
        ) {
            ShipmentTrackingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Weather.route) {
            WeatherDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Compliance.route) {
            ComplianceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCertificateDetail = { certId ->
                    navController.navigate(Screen.CertificateDetail.createRoute(certId))
                }
            )
        }
        composable(Screen.ESGDashboard.route) {
            ESGDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToInvoices = {
                    navController.navigate(Screen.Invoices.route)
                }
            )
        }
        composable(Screen.Farms.route) {
            FarmListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { farmId ->
                    navController.navigate(Screen.FarmDetail.createRoute(farmId))
                },
                onNavigateToAddFarm = {
                    navController.navigate(Screen.AddEditFarm.createRoute("new"))
                }
            )
        }
        composable(Screen.Agent.route) {
            AgentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminDashboard.route)
                }
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToUserVerification = {
                    navController.navigate(Screen.UserVerification.route)
                },
                onNavigateToDisputes = {
                    navController.navigate(Screen.DisputeManagement.route)
                },
                onNavigateToAuditLogs = {
                    navController.navigate(Screen.AuditLogs.route)
                },
                onNavigateToFleet = {
                    navController.navigate(Screen.FleetTracker.route)
                },
                onNavigateToESG = {
                    navController.navigate(Screen.Compliance.route)
                },
                onNavigateToQuality = {
                    navController.navigate(Screen.Quality.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.UserVerification.route) {
            UserVerificationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.DisputeManagement.route) {
            DisputeManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.AuditLogs.route) {
            AuditLogScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.InvoiceGenerator.route) {
            InvoiceGeneratorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.InvoiceHistory.route)
                }
            )
        }
        composable(Screen.InvoiceHistory.route) {
            InvoiceHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.InvoiceGenerator.route)
                }
            )
        }
        composable(
            route = Screen.CustomAgroPlan.route,
            arguments = listOf(
                navArgument("farmId") { type = NavType.StringType; defaultValue = "default_farm" },
                navArgument("crop") { type = NavType.StringType; defaultValue = "Wheat" }
            )
        ) {
            CustomAgroPlanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToListingCreation = {
                    navController.navigate(Screen.CreateListing.route)
                }
            )
        }
        composable(Screen.TransporterHub.route) {
            TransporterCommandHubScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToFleetMap = {
                    navController.navigate(Screen.FleetTracker.route)
                },
                onNavigateToWeighbridge = {
                    navController.navigate(Screen.WeighbridgeSlips.route)
                }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMarket = {
                    navController.navigate(Screen.Market.route)
                }
            )
        }
        composable(
            route = Screen.ListingDetail.route,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) {
            ListingDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToOrder = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                }
            )
        }
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) {
            OrderDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTracking = { transferId ->
                    navController.navigate(Screen.ShipmentTracking.createRoute(transferId))
                },
                onNavigateToQRGenerate = { transferId ->
                    navController.navigate(Screen.QRGenerate.createRoute(transferId))
                }
            )
        }

        composable(
            route = Screen.AddCropCycle.route,
            arguments = listOf(navArgument("farmId") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) {
            AddCropCycleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.StubbleValorization.route,
            arguments = listOf(
                navArgument("farmId") {
                    type = NavType.StringType
                    defaultValue = "plot_1"
                },
                navArgument("crop") {
                    type = NavType.StringType
                    defaultValue = "Cotton"
                }
            )
        ) {
            StubbleValorizationDeskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAgroPlan = { farmId, crop ->
                    navController.navigate(Screen.CustomAgroPlan.createRoute(farmId, crop))
                },
                onNavigateToCreateListing = {
                    navController.navigate(Screen.CreateListing.route)
                }
            )
        }

        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToInvoice = {
                    navController.navigate(Screen.Invoices.route)
                }
            )
        }

        composable(Screen.FindDealer.route) {
            FindDealerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.CertificateDetail.route,
            arguments = listOf(navArgument("certId") {
                type = NavType.StringType
                defaultValue = "cert_001"
            })
        ) { backStackEntry ->
            val certId = backStackEntry.arguments?.getString("certId") ?: "cert_001"
            CertificateViewScreen(
                certificateId = certId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.CropRecommendation.route,
            arguments = listOf(
                navArgument("farmId") {
                    type = NavType.StringType
                    defaultValue = "default_farm"
                }
            )
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: "default_farm"
            CropRecommendationScreen(
                farmId = farmId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCropAssessment = { cropName ->
                    navController.navigate(Screen.CropPerformance.createRoute(farmId = farmId, crop = cropName))
                },
                onNavigateToDirectSearch = {
                    navController.navigate(Screen.CropPerformance.createRoute(farmId = farmId, crop = "Wheat"))
                }
            )
        }

        composable(
            route = Screen.CropPerformance.route,
            arguments = listOf(
                navArgument("farmId") {
                    type = NavType.StringType
                    defaultValue = "default_farm"
                },
                navArgument("crop") {
                    type = NavType.StringType
                    defaultValue = "Wheat"
                }
            )
        ) { backStackEntry ->
            val farmId = backStackEntry.arguments?.getString("farmId") ?: "default_farm"
            val crop = backStackEntry.arguments?.getString("crop") ?: "Wheat"
            CropPerformanceAssessmentScreen(
                farmId = farmId,
                cropName = crop,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAgroPlan = { selectedCrop ->
                    navController.navigate(Screen.CustomAgroPlan.createRoute(farmId = farmId, crop = selectedCrop))
                },
                onNavigateToMarketTrends = {
                    navController.navigate(Screen.Analytics.route)
                }
            )
        }
    }
}
