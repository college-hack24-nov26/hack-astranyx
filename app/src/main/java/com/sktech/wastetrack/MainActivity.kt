package com.sktech.wastetrack

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sktech.wastetrack.data.sync.CloudSyncEngine
import com.sktech.wastetrack.ui.components.NetworkStatusBanner
import com.sktech.wastetrack.ui.navigation.BottomNavBar
import com.sktech.wastetrack.ui.navigation.NavGraph
import com.sktech.wastetrack.ui.navigation.Screen
import com.sktech.wastetrack.ui.theme.WastetrackTheme
import com.sktech.wastetrack.util.Constants
import com.sktech.wastetrack.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private val BOTTOM_BAR_ROUTES = setOf(
    Screen.Dashboard.route,
    Screen.RecyclerDashboard.route,
    Screen.DriverDashboard.route,
    Screen.Crops.route,
    Screen.Market.route,
    Screen.Orders.route,
    Screen.Weather.route,
    Screen.Analytics.route,
    Screen.Compliance.route,
    Screen.Settings.route
)

private val BUYER_ROUTES = setOf(
    Screen.Orders.route,
    Screen.Compliance.route,
    Screen.RecyclerDashboard.route
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var cloudSyncEngine: CloudSyncEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cloudSyncEngine.schedulePeriodicSync()
        enableEdgeToEdge()
        setContent {
            WastetrackTheme {
                val context = LocalContext.current
                val startDestination = remember {
                    val prefs = context.getSharedPreferences("agristock_prefs", Context.MODE_PRIVATE)
                    val authPrefs = context.getSharedPreferences("wastetrack_prefs", Context.MODE_PRIVATE)
                    val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)
                    val isLoggedIn = authPrefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false)
                    when {
                        !onboardingCompleted -> Screen.Onboarding.route
                        isLoggedIn -> Screen.Dashboard.route
                        else -> Screen.Login.route
                    }
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: startDestination
                val showBottomBar = currentRoute in BOTTOM_BAR_ROUTES

                val activeRole = if (currentRoute in BUYER_ROUTES) {
                    com.sktech.wastetrack.domain.model.UserRole.BUYER
                } else {
                    com.sktech.wastetrack.domain.model.UserRole.FARMER
                }

                Scaffold(
                    topBar = {
                        NetworkStatusBanner(networkMonitor = networkMonitor)
                    },
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                role = activeRole,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        innerPadding = innerPadding,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
