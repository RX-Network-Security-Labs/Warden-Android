package com.warden.rnsl.ui.screens

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.warden.rnsl.data.model.SuspiciousStatus
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.components.*
import com.warden.rnsl.ui.navigation.Screen
import com.warden.rnsl.ui.theme.BadRed
import com.warden.rnsl.ui.theme.GoodGreen
import com.warden.rnsl.ui.theme.WardenBlue

@Composable
fun HomeScreen(viewModel: WardenViewModel, navController: NavController) {
    val apps by viewModel.apps.collectAsState()
    val badCount by viewModel.badCountToday.collectAsState()
    val totalLogs by viewModel.totalLogCount.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val liveEnabled by viewModel.liveLoggingEnabled.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        WardenTopBar(title = "Warden")

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {

            // Status card
            item {
                WardenCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Security,
                            contentDescription = null,
                            tint = WardenBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Warden is watching", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (badCount > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = BadRed, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "$badCount suspicious events today", color = BadRed, fontSize = 13.sp)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GoodGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "No suspicious events today", color = GoodGreen, fontSize = 13.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${apps.size} apps monitored", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Live logging card
            item {
                WardenCard(onClick = {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FlashOn, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Live Logging", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text(
                                text = if (liveEnabled) "ON — Monitoring active" else "OFF — Tap to enable",
                                fontSize = 13.sp,
                                color = if (liveEnabled) GoodGreen else BadRed
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Quick stats
            item {
                SectionTitle("Quick Stats")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(icon = Icons.Filled.Apps, value = apps.size.toString(), label = "Apps", onClick = { navController.navigate(Screen.Apps.route) })
                    StatCard(icon = Icons.Filled.List, value = totalLogs.toString(), label = "Logs", onClick = { navController.navigate(Screen.Logs.route) })
                    StatCard(icon = Icons.Filled.Warning, value = badCount.toString(), label = "Alerts")
                }
            }

            // Recent alerts
            if (recentLogs.isNotEmpty()) {
                item { SectionTitle("Recent Alerts") }
                items(recentLogs.take(10)) { log ->
                    LogRow(log = log, onClick = {
                        navController.navigate(Screen.AppDetail.createRoute(log.packageName))
                    })
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GoodGreen, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No logs yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Enable live logging to start monitoring", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}