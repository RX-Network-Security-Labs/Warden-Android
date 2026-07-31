package com.warden.rnsl.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.components.LogRow
import com.warden.rnsl.ui.components.SectionTitle
import com.warden.rnsl.ui.components.WardenTopBar
import com.warden.rnsl.ui.theme.WardenBlue
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppDetailScreen(viewModel: WardenViewModel, navController: NavController, packageName: String) {
    val app = viewModel.getAppInfo(packageName)
    val logs by viewModel.getLogsForApp(packageName).collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        WardenTopBar(
            title = app?.appName ?: packageName,
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        )

        if (app == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("App not found")
            }
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(14.dp), color = WardenBlue.copy(alpha = 0.1f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Android, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(36.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = app.appName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Version ${app.versionName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = app.packageName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Installed: ${dateFormat.format(Date(app.installedDate))}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (app.isSystemApp) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                                Text(text = "SYSTEM APP", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item { SectionTitle("Permissions (${app.permissions.size})") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    if (app.permissions.isEmpty()) {
                        Text(text = "No permissions", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column {
                            app.permissions.take(20).forEachIndexed { index, perm ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(permissionIcon(perm), contentDescription = null, tint = WardenBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = perm.substringAfterLast("."), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(text = "Granted", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (index < app.permissions.size - 1) {
                                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                            if (app.permissions.size > 20) {
                                Text(text = "+${app.permissions.size - 20} more permissions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }

            if (logs.isNotEmpty()) {
                item { SectionTitle("Recent Logs (${logs.size})") }
                items(logs.take(20)) { log -> LogRow(log = log) }
            }

            item {
                Button(
                    onClick = { viewModel.openAppPermissionSettings(packageName) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WardenBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Permissions in Settings")
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

fun permissionIcon(permission: String): ImageVector {
    return when {
        permission.contains("CAMERA", ignoreCase = true) -> Icons.Filled.CameraAlt
        permission.contains("MICROPHONE", ignoreCase = true) || permission.contains("RECORD_AUDIO", ignoreCase = true) -> Icons.Filled.Mic
        permission.contains("LOCATION", ignoreCase = true) -> Icons.Filled.LocationOn
        permission.contains("CONTACT", ignoreCase = true) -> Icons.Filled.Contacts
        permission.contains("STORAGE", ignoreCase = true) || permission.contains("MEDIA", ignoreCase = true) -> Icons.Filled.Folder
        permission.contains("PHONE", ignoreCase = true) -> Icons.Filled.Phone
        permission.contains("SMS", ignoreCase = true) -> Icons.Filled.Sms
        permission.contains("CALENDAR", ignoreCase = true) -> Icons.Filled.CalendarMonth
        permission.contains("BLUETOOTH", ignoreCase = true) -> Icons.Filled.Bluetooth
        permission.contains("INTERNET", ignoreCase = true) || permission.contains("NETWORK", ignoreCase = true) -> Icons.Filled.Wifi
        permission.contains("NOTIFICATION", ignoreCase = true) -> Icons.Filled.Notifications
        permission.contains("SENSOR", ignoreCase = true) -> Icons.Filled.Sensors
        else -> Icons.Filled.Key
    }
}
