package com.sktech.wastetrack.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.sktech.wastetrack.R
import com.sktech.wastetrack.domain.model.UserRole

data class BottomNavItem(
    val labelRes: Int,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val farmerNavItems = listOf(
    BottomNavItem(R.string.nav_home, Screen.Dashboard.route, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    BottomNavItem(R.string.nav_crops, Screen.Crops.route, Icons.Filled.Agriculture, Icons.Outlined.Agriculture),
    BottomNavItem(R.string.nav_market, Screen.Market.route, Icons.Filled.Storefront, Icons.Outlined.Storefront),
    BottomNavItem(R.string.nav_weather, Screen.Weather.route, Icons.Filled.WbSunny, Icons.Outlined.WbSunny),
    BottomNavItem(R.string.nav_profile, Screen.Settings.route, Icons.Filled.Person, Icons.Outlined.Person)
)

val buyerNavItems = listOf(
    BottomNavItem(R.string.nav_home, Screen.Dashboard.route, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    BottomNavItem(R.string.nav_market, Screen.Market.route, Icons.Filled.Storefront, Icons.Outlined.Storefront),
    BottomNavItem(R.string.nav_orders, "orders", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    BottomNavItem(R.string.nav_compliance, "compliance", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(R.string.nav_profile, Screen.Settings.route, Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    role: UserRole = UserRole.FARMER,
    onNavigate: (String) -> Unit
) {
    val items = if (role == UserRole.BUYER) buyerNavItems else farmerNavItems

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = { Text(stringResource(item.labelRes)) }
            )
        }
    }
}
