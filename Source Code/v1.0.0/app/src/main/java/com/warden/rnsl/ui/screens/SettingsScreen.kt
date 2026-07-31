package com.warden.rnsl.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.components.WardenTopBar
import com.warden.rnsl.ui.navigation.Screen
import com.warden.rnsl.ui.theme.WardenBlue

@Composable
fun SettingsScreen(viewModel: WardenViewModel, navController: NavController) {
    val darkTheme by viewModel.darkTheme.collectAsState()
    val liveEnabled by viewModel.liveLoggingEnabled.collectAsState()
    var showLiveDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        WardenTopBar(title = "Settings")

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {

            item {
                SettingsSection(title = "Appearance") {
                    SettingsToggleRow(
                        icon = Icons.Filled.DarkMode,
                        title = "Dark Theme",
                        subtitle = if (darkTheme) "Currently: Dark" else "Currently: Light",
                        checked = darkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "Monitoring") {
                    Column {
                        SettingsToggleRow(
                            icon = Icons.Filled.FlashOn,
                            title = "Live Logging",
                            subtitle = "Requires Shizuku or ADB",
                            checked = liveEnabled,
                            onCheckedChange = {
                                if (it) showLiveDialog = true
                                else viewModel.setLiveLogging(false)
                            }
                        )
                    }
                }
            }

            item {
                SettingsSection(title = "Setup") {
                    Column {
                        SettingsNavRow(
                            icon = Icons.Filled.ElectricBolt,
                            title = "Shizuku Setup",
                            subtitle = "Configure Shizuku for live logging",
                            onClick = { navController.navigate(Screen.ShizukuSetup.route) }
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsNavRow(
                            icon = Icons.Filled.Cable,
                            title = "ADB Setup",
                            subtitle = "Configure wireless ADB for live logging",
                            onClick = { navController.navigate(Screen.AdbSetup.route) }
                        )
                    }
                }
            }

            item {
                SettingsSection(title = "Data") {
                    Column {
                        SettingsNavRow(
                            icon = Icons.Filled.Upload,
                            title = "Export Logs",
                            subtitle = "Save logs as JSON, CSV or TXT",
                            onClick = { navController.navigate(Screen.ExportLogs.route) }
                        )
                    }
                }
            }

            item {
                SettingsSection(title = "Info") {
                    SettingsNavRow(
                        icon = Icons.Filled.Info,
                        title = "About Warden",
                        subtitle = "Version 1.0.0 · RX Network Security Labs",
                        onClick = { navController.navigate(Screen.About.route) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showLiveDialog) {
        AlertDialog(
            onDismissRequest = { showLiveDialog = false },
            title = { Text("Enable Live Logging", fontWeight = FontWeight.Bold) },
            text = { Text("Choose how Warden should get permission to monitor live events.") },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showLiveDialog = false; navController.navigate(Screen.ShizukuSetup.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = WardenBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Shizuku")
                    }
                    OutlinedButton(
                        onClick = { showLiveDialog = false; navController.navigate(Screen.AdbSetup.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Cable, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use ADB")
                    }
                    TextButton(onClick = { showLiveDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = {}
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = WardenBlue,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) { content() }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SettingsToggleRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = WardenBlue, checkedTrackColor = WardenBlue.copy(alpha = 0.3f)))
    }
}

@Composable
fun SettingsNavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
