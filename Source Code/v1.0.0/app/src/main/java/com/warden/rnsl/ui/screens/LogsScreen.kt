package com.warden.rnsl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.components.*
import com.warden.rnsl.ui.navigation.Screen
import com.warden.rnsl.ui.theme.BadRed
import com.warden.rnsl.ui.theme.GoodGreen
import com.warden.rnsl.ui.theme.WardenBlue

@Composable
fun LogsScreen(viewModel: WardenViewModel, navController: NavController) {
    val logs by viewModel.allLogs.collectAsState()
    val liveEnabled by viewModel.liveLoggingEnabled.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        WardenTopBar(title = "Logs")

        Surface(
            color = if (liveEnabled) GoodGreen.copy(alpha = 0.15f) else BadRed.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (liveEnabled) Icons.Filled.FiberManualRecord else Icons.Filled.FiberManualRecord,
                        contentDescription = null,
                        tint = if (liveEnabled) GoodGreen else BadRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (liveEnabled) "Live Logging: ON" else "Live Logging: OFF",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (liveEnabled) GoodGreen else BadRed
                    )
                }
                if (!liveEnabled) {
                    TextButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Text("ENABLE", color = WardenBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.List, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No logs yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Enable live logging to start tracking", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                item {
                    Text(
                        text = "${logs.size} total events",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                items(logs) { log ->
                    LogRow(log = log, onClick = {
                        navController.navigate(Screen.AppDetail.createRoute(log.packageName))
                    })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
