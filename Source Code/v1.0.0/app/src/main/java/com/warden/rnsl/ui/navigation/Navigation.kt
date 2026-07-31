package com.warden.rnsl.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Apps : Screen("apps")
    object Logs : Screen("logs")
    object Settings : Screen("settings")
    object AppDetail : Screen("app_detail/{packageName}") {
        fun createRoute(packageName: String) = "app_detail/$packageName"
    }
    object ShizukuSetup : Screen("shizuku_setup")
    object AdbSetup : Screen("adb_setup")
    object ExportLogs : Screen("export_logs")
    object About : Screen("about")
    object LiveSetup : Screen("live_setup")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.Apps, "Apps", Icons.Filled.Apps),
    BottomNavItem(Screen.Logs, "Logs", Icons.Filled.Assessment),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings)
)
