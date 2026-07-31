package com.warden.rnsl.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.warden.rnsl.data.model.PermissionLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ExportUtil {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val fileFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun exportAsJson(context: Context, logs: List<PermissionLog>): Uri? {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(logs)
        return writeToFile(context, json, "warden_log_${fileFormat.format(Date())}.json", "application/json")
    }

    fun exportAsCsv(context: Context, logs: List<PermissionLog>): Uri? {
        val sb = StringBuilder()
        sb.appendLine("App Name,Date-Time,Permission,App State,Screen State,Status")
        logs.forEach { log ->
            sb.appendLine(
                "${log.appName}," +
                "${dateFormat.format(Date(log.timestamp))}," +
                "${log.permission}," +
                "${if (log.appState) "On" else "Off"}," +
                "${if (log.screenState) "On" else "Off"}," +
                log.suspiciousStatus
            )
        }
        return writeToFile(context, sb.toString(), "warden_log_${fileFormat.format(Date())}.csv", "text/csv")
    }

    fun exportAsTxt(context: Context, logs: List<PermissionLog>): Uri? {
        val sb = StringBuilder()
        sb.appendLine("=== Warden Privacy Logger Export ===")
        sb.appendLine("Generated: ${dateFormat.format(Date())}")
        sb.appendLine("Total logs: ${logs.size}")
        sb.appendLine("=" .repeat(50))
        sb.appendLine()
        logs.forEach { log ->
            sb.appendLine("App     : ${log.appName} (${log.packageName})")
            sb.appendLine("Time    : ${dateFormat.format(Date(log.timestamp))}")
            sb.appendLine("Permission: ${log.permission}")
            sb.appendLine("App State : ${if (log.appState) "Foreground" else "Background"}")
            sb.appendLine("Screen  : ${if (log.screenState) "On" else "Off"}")
            sb.appendLine("Status  : ${log.suspiciousStatus}")
            sb.appendLine("-".repeat(40))
        }
        return writeToFile(context, sb.toString(), "warden_log_${fileFormat.format(Date())}.txt", "text/plain")
    }

    private fun writeToFile(context: Context, content: String, fileName: String, mimeType: String): Uri? {
        return try {
            val dir = File(context.getExternalFilesDir(null), "WardenExports")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            file.writeText(content)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share log via"))
    }

    fun getExportedFiles(context: Context): List<File> {
        val dir = File(context.getExternalFilesDir(null), "WardenExports")
        return if (dir.exists()) dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
        else emptyList()
    }
}
