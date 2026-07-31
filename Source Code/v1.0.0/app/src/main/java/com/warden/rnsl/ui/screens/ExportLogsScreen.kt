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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.components.WardenTopBar
import com.warden.rnsl.ui.theme.BadRed
import com.warden.rnsl.ui.theme.GoodGreen
import com.warden.rnsl.ui.theme.WardenBlue
import com.warden.rnsl.util.ExportUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExportLogsScreen(viewModel: WardenViewModel, navController: NavController) {
    val context = LocalContext.current
    val logs by viewModel.allLogs.collectAsState()
    var exportedFiles by remember { mutableStateOf(ExportUtil.getExportedFiles(context)) }
    var showClearDialog by remember { mutableStateOf(false) }
    var exportStatus by remember { mutableStateOf("") }
    var exportSuccess by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        WardenTopBar(
            title = "Export Logs",
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        )

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            if (exportStatus.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (exportSuccess) GoodGreen.copy(alpha = 0.1f) else BadRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (exportSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                tint = if (exportSuccess) GoodGreen else BadRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = exportStatus, color = if (exportSuccess) GoodGreen else BadRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.BarChart, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("${logs.size} logs available", fontWeight = FontWeight.Bold)
                            Text("Ready to export", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Text("Export Options", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                ExportOptionCard(
                    icon = Icons.Filled.DataObject,
                    title = "Export as JSON",
                    subtitle = "Full detailed report (machine-readable)",
                    onExport = {
                        val uri = ExportUtil.exportAsJson(context, logs)
                        if (uri != null) {
                            exportStatus = "Exported as JSON!"
                            exportSuccess = true
                            exportedFiles = ExportUtil.getExportedFiles(context)
                            ExportUtil.shareFile(context, uri, "application/json")
                        } else {
                            exportStatus = "Export failed"
                            exportSuccess = false
                        }
                    }
                )
            }

            item {
                ExportOptionCard(
                    icon = Icons.Filled.TableChart,
                    title = "Export as CSV",
                    subtitle = "Spreadsheet compatible (Excel / Google Sheets)",
                    onExport = {
                        val uri = ExportUtil.exportAsCsv(context, logs)
                        if (uri != null) {
                            exportStatus = "Exported as CSV!"
                            exportSuccess = true
                            exportedFiles = ExportUtil.getExportedFiles(context)
                            ExportUtil.shareFile(context, uri, "text/csv")
                        } else {
                            exportStatus = "Export failed"
                            exportSuccess = false
                        }
                    }
                )
            }

            item {
                ExportOptionCard(
                    icon = Icons.Filled.TextSnippet,
                    title = "Export as TXT",
                    subtitle = "Human-readable format",
                    onExport = {
                        val uri = ExportUtil.exportAsTxt(context, logs)
                        if (uri != null) {
                            exportStatus = "Exported as TXT!"
                            exportSuccess = true
                            exportedFiles = ExportUtil.getExportedFiles(context)
                            ExportUtil.shareFile(context, uri, "text/plain")
                        } else {
                            exportStatus = "Export failed"
                            exportSuccess = false
                        }
                    }
                )
            }

            if (exportedFiles.isNotEmpty()) {
                item { Text("Recent Exports", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                items(exportedFiles) { file -> RecentFileCard(file = file) }
            }

            item {
                Button(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BadRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Logs")
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Logs?") },
            text = { Text("This will permanently delete all permission logs, app sessions, and screen events. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllLogs()
                        exportStatus = "All logs cleared"
                        exportSuccess = true
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BadRed)
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ExportOptionCard(icon: ImageVector, title: String, subtitle: String, onExport: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(containerColor = WardenBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) { Text("EXPORT", fontSize = 11.sp) }
        }
    }
}

@Composable
fun RecentFileCard(file: File) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.InsertDriveFile, contentDescription = null, tint = WardenBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(dateFormat.format(Date(file.lastModified())), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = WardenBlue)
        }
    }
}
