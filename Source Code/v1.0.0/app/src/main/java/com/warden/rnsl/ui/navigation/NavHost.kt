package com.warden.rnsl.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.screens.*
import com.warden.rnsl.ui.theme.WardenBlue

@Composable
fun WardenNavHost(viewModel: WardenViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomRoutes = listOf(
        Screen.Home.route,
        Screen.Apps.route,
        Screen.Logs.route,
        Screen.Settings.route
    )
    val showBottomBar = bottomRoutes.any { currentRoute == it }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) WardenBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) WardenBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = WardenBlue.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.Apps.route) {
                AppsScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.Logs.route) {
                LogsScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.AppDetail.route) { backStack ->
                val pkg = backStack.arguments?.getString("packageName") ?: ""
                AppDetailScreen(viewModel = viewModel, navController = navController, packageName = pkg)
            }
            composable(Screen.ShizukuSetup.route) {
                ShizukuSetupScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.AdbSetup.route) {
                AdbSetupScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.ExportLogs.route) {
                ExportLogsScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.About.route) {
                AboutScreen(navController = navController)
            }
        }
    }
}
