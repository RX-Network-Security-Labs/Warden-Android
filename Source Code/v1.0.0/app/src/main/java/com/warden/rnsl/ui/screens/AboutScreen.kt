package com.warden.rnsl.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.warden.rnsl.ui.components.WardenTopBar
import com.warden.rnsl.ui.theme.WardenBlue

@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WardenTopBar(
            title = "About",
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(20.dp), color = WardenBlue) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Warden", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = WardenBlue)
                        Text(text = "Privacy Logger", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = WardenBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text(text = "Version 1.0.0", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = WardenBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Developed by\nRX Network Security Labs (RNSL)", textAlign = TextAlign.Center, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("What Warden Does", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        AboutFeatureItem(Icons.Filled.PhoneAndroid, "Shows all installed apps including system apps")
                        AboutFeatureItem(Icons.Filled.Key, "Logs which permissions each app used and when")
                        AboutFeatureItem(Icons.Filled.Timer, "Tracks app open/close duration")
                        AboutFeatureItem(Icons.Filled.Tv, "Monitors screen on/off state")
                        AboutFeatureItem(Icons.Filled.Warning, "Alerts on suspicious background activity")
                        AboutFeatureItem(Icons.Filled.Upload, "Export logs as JSON, CSV or TXT")
                    }
                }
            }

            item {
                Text("Links", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        AboutLinkRow(Icons.Filled.Code, "GitHub Repository", "github.com/RX-Network-Security-Labs") { openUrl("https://github.com/RX-Network-Security-Labs") }
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        AboutLinkRow(Icons.Filled.Forum, "Report Issue / Feedback", "Discord — RX NSL Feedback Channel") { openUrl("https://discord.com/channels/1516826054176669718/1518128528925917364") }
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        AboutLinkRow(Icons.Filled.Language, "Website", "rx-network-security-labs.github.io") { openUrl("https://rx-network-security-labs.github.io/website/") }
                    }
                }
            }

            item {
                Text(
                    text = "© 2026 RX Network Security Labs\nAll rights reserved.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AboutFeatureItem(icon: ImageVector, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, modifier = Modifier.padding(top = 1.dp))
    }
}

@Composable
fun AboutLinkRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = WardenBlue)
        }
    }
}
