package com.warden.rnsl.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.warden.rnsl.shizuku.ShizukuHelper
import com.warden.rnsl.shizuku.ShizukuStatus
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.components.WardenTopBar
import com.warden.rnsl.ui.theme.BadRed
import com.warden.rnsl.ui.theme.GoodGreen
import com.warden.rnsl.ui.theme.WardenBlue
import rikka.shizuku.Shizuku

const val SHIZUKU_REQUEST_CODE = 1001

@Composable
fun ShizukuSetupScreen(viewModel: WardenViewModel, navController: NavController) {
    val context = LocalContext.current
    var shizukuStatus by remember {
        mutableStateOf(ShizukuHelper.getStatus(context.packageManager))
    }
    var permissionGranted by remember {
        mutableStateOf(ShizukuHelper.isPermissionGranted())
    }

    fun refreshStatus() {
        shizukuStatus = ShizukuHelper.getStatus(context.packageManager)
        permissionGranted = ShizukuHelper.isPermissionGranted()
    }

    DisposableEffect(Unit) {
        val binderReceived = Shizuku.OnBinderReceivedListener { refreshStatus() }
        val binderDead = Shizuku.OnBinderDeadListener { refreshStatus() }
        val permResult = Shizuku.OnRequestPermissionResultListener { _, result ->
            permissionGranted = result == PackageManager.PERMISSION_GRANTED
            shizukuStatus = ShizukuHelper.getStatus(context.packageManager)
        }
        ShizukuHelper.addBinderReceivedListener(binderReceived)
        ShizukuHelper.addBinderDeadListener(binderDead)
        ShizukuHelper.addRequestPermissionResultListener(permResult)
        onDispose {
            ShizukuHelper.removeBinderReceivedListener(binderReceived)
            ShizukuHelper.removeBinderDeadListener(binderDead)
            ShizukuHelper.removeRequestPermissionResultListener(permResult)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WardenTopBar(
            title = "Shizuku Setup",
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
            item { StatusCard(shizukuStatus, permissionGranted) }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.List, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Setup Steps", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        StepItem(number = "1", text = "Install Shizuku from Play Store or GitHub")
                        StepItem(number = "2", text = "Open Shizuku app and start the service")
                        StepItem(number = "3", text = "Come back to Warden and tap 'Request Permission' below")
                        StepItem(number = "4", text = "Tap Allow on the Shizuku permission popup")
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { refreshStatus() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check Shizuku Status")
                    }

                    if (shizukuStatus == ShizukuStatus.NOT_INSTALLED) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RikkaApps/Shizuku/releases"))
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WardenBlue)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Shizuku")
                        }
                    }

                    // FIX: show for both READY+no permission AND NOT_GRANTED
                    if ((shizukuStatus == ShizukuStatus.READY && !permissionGranted) ||
                        shizukuStatus == ShizukuStatus.NOT_GRANTED) {
                        Button(
                            onClick = { ShizukuHelper.requestPermission(SHIZUKU_REQUEST_CODE) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WardenBlue)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Permission")
                        }
                    }

                    if (shizukuStatus == ShizukuStatus.READY && permissionGranted) {
                        Button(
                            onClick = { viewModel.setLiveLogging(true); navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoodGreen)
                        ) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enable Live Logging")
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = WardenBlue.copy(alpha = 0.08f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "If Shizuku is not working or you don't want to install it, try ADB instead from Settings → ADB Setup.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun StatusCard(status: ShizukuStatus, permissionGranted: Boolean) {
    val (icon, text, color) = when {
        status == ShizukuStatus.READY && permissionGranted ->
            Triple(Icons.Filled.CheckCircle, "Shizuku is ready and permission granted!", GoodGreen)
        status == ShizukuStatus.READY && !permissionGranted ->
            Triple(Icons.Filled.Info, "Shizuku is running but permission not granted yet.", WardenBlue)
        // FIX: NOT_GRANTED was missing — caused "Unknown status"
        status == ShizukuStatus.NOT_GRANTED ->
            Triple(Icons.Filled.Info, "Shizuku is running but permission not granted yet.", WardenBlue)
        status == ShizukuStatus.NOT_RUNNING ->
            Triple(Icons.Filled.Warning, "Shizuku is installed but not running. Open Shizuku and start the service.", BadRed)
        status == ShizukuStatus.NOT_INSTALLED ->
            Triple(Icons.Filled.Error, "Shizuku is not installed. Download it first.", BadRed)
        else -> Triple(Icons.Filled.Help, "Unknown status", BadRed)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Shizuku Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text, fontSize = 13.sp, color = color)
            }
        }
    }
}

@Composable
fun StepItem(number: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Surface(shape = RoundedCornerShape(50), color = WardenBlue, modifier = Modifier.size(22.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
