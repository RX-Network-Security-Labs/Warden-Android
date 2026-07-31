package com.warden.rnsl.service

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.warden.rnsl.MainActivity
import com.warden.rnsl.R
import com.warden.rnsl.data.model.*
import com.warden.rnsl.data.repository.WardenRepository
import com.warden.rnsl.shizuku.ShizukuHelper
import com.warden.rnsl.util.WardenPrefs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader

class WardenService : Service() {

  private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private lateinit var repository: WardenRepository
  private lateinit var prefs: WardenPrefs
  private var screenReceiver: ScreenReceiver? = null
  private var usageMonitorJob: Job? = null
  private var appOpsMonitorJob: Job? = null
  private var monitoringStartTime = 0L

  companion object {
    const val NOTIFICATION_ID = 1001
    const val CHANNEL_ID = "warden_service_channel"

    @Volatile var isScreenOn = true
    @Volatile var currentForegroundApp = ""

    val PRIVACY_OPS = setOf(
      "CAMERA",
      "RECORD_AUDIO",
      "ACCESS_FINE_LOCATION",
      "ACCESS_COARSE_LOCATION",
      "COARSE_LOCATION",
      "FINE_LOCATION",
      "READ_CONTACTS",
      "WRITE_CONTACTS",
      "READ_CALL_LOG",
      "WRITE_CALL_LOG",
      "READ_SMS",
      "SEND_SMS",
      "RECEIVE_SMS",
      "READ_CALENDAR",
      "WRITE_CALENDAR",
      "BODY_SENSORS",
      "ACTIVITY_RECOGNITION",
      "READ_MEDIA_IMAGES",
      "READ_MEDIA_VIDEO",
      "READ_MEDIA_AUDIO",
      "READ_EXTERNAL_STORAGE",
      "WRITE_EXTERNAL_STORAGE",
      "READ_PHONE_NUMBERS",
      "PROCESS_OUTGOING_CALLS",
      "ANSWER_PHONE_CALLS",
      "CALL_PHONE"
    )
  }

  override fun onCreate() {
    super.onCreate()
    repository = WardenRepository(applicationContext)
    prefs = WardenPrefs(applicationContext)
    createNotificationChannel()
    startForeground(NOTIFICATION_ID, buildNotification())
    registerScreenReceiver()
    startUsageMonitoring()
    serviceScope.launch {
      prefs.liveLoggingEnabled.first().let {
        enabled ->
        if (enabled) startPermissionMonitoring()
      }
      prefs.liveLoggingEnabled.collect {
        enabled ->
        if (enabled) {
          startPermissionMonitoring()
        } else {
          stopPermissionMonitoring()
        }
      }
    }
  }

  // ─── Screen Receiver ─────────────────────────────────────────────────────

  private fun registerScreenReceiver() {
    screenReceiver = ScreenReceiver()
    val filter = IntentFilter().apply {
      addAction(Intent.ACTION_SCREEN_ON)
      addAction(Intent.ACTION_SCREEN_OFF)
    }
    registerReceiver(screenReceiver, filter)
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
    isScreenOn = pm.isInteractive
  }

  // ─── Usage Stats Monitoring ───────────────────────────────────────────────

  private fun startUsageMonitoring() {
    usageMonitorJob = serviceScope.launch {
      while (isActive) {
        try {
          checkUsageStats()
        } catch (e: Exception) {
          e.printStackTrace()
        }
        delay(5000)
      }
    }
  }

  private suspend fun checkUsageStats() {
    val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 10000
    val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
    val event = UsageEvents.Event()

    while (usageEvents.hasNextEvent()) {
      usageEvents.getNextEvent(event)
      when (event.eventType) {
        UsageEvents.Event.MOVE_TO_FOREGROUND -> {
          currentForegroundApp = event.packageName
          val appName = getAppName(event.packageName)
          val openSession = repository.getOpenSession()
          if (openSession != null && openSession.packageName != event.packageName) {
            val closeTime = event.timeStamp
            repository.updateSession(
              openSession.copy(
                closeTime = closeTime,
                duration = closeTime - openSession.openTime
              )
            )
          }
          repository.insertSession(
            AppSession(
              packageName = event.packageName,
              appName = appName,
              openTime = event.timeStamp
            )
          )
        }
        UsageEvents.Event.MOVE_TO_BACKGROUND -> {
          if (currentForegroundApp == event.packageName) {
            currentForegroundApp = ""
          }
          val openSession = repository.getOpenSession()
          openSession?.let {
            session ->
            if (session.packageName == event.packageName) {
              val closeTime = event.timeStamp
              repository.updateSession(
                session.copy(
                  closeTime = closeTime,
                  duration = closeTime - session.openTime
                )
              )
            }
          }
        }
      }
    }
  }

  // ─── Permission Monitoring ────────────────────────────────────────────────

  private fun startPermissionMonitoring() {
    if (appOpsMonitorJob?.isActive == true) return
    monitoringStartTime = System.currentTimeMillis()

    appOpsMonitorJob = serviceScope.launch {
      val lastSeen = mutableMapOf<String, Long>()
      val packages = packageManager
      .getInstalledPackages(PackageManager.GET_PERMISSIONS)
      .filter {
        it.packageName != packageName
      }

      while (isActive) {
        val now = System.currentTimeMillis()
        for (pkg in packages) {
          try {
            pollPackageAppOps(pkg.packageName, now, lastSeen)
          } catch (e: Exception) {
            /* skip */
          }
        }
        delay(10_000)
      }
    }
  }

  private fun stopPermissionMonitoring() {
    appOpsMonitorJob?.cancel()
    appOpsMonitorJob = null
    monitoringStartTime = 0L
  }

  // ─── Core polling ─────────────────────────────────────────────────────────

  private suspend fun pollPackageAppOps(
    pkg: String,
    now: Long,
    lastSeen: MutableMap<String, Long>
  ) {
    val output = runShizukuCommand(arrayOf("appops", "get", pkg)) ?: return

    for (line in output.lines()) {
      val lineTrimmed = line.trim()
      if (!lineTrimmed.contains("time=+")) continue
      if (!lineTrimmed.contains(": allow")) continue

      val opName = lineTrimmed.substringBefore(":").trim()
      if (opName.isEmpty()) continue
      if (opName !in PRIVACY_OPS) continue

      val lastAccessTime = parseTimeAgo(lineTrimmed, now) ?: continue

      if (lastAccessTime < monitoringStartTime) continue

      // Bulletproof dedup: same app + same op + same minute = same event
      val minuteBucket = lastAccessTime / 60000
      val key = "$pkg:$opName:$minuteBucket"

      if (lastSeen.containsKey(key)) continue
      lastSeen[key] = lastAccessTime

      // ✅ Query exact state at permission timestamp — no delay error!
      val appWasInForeground = wasAppInForegroundAt(pkg, lastAccessTime)
      val screenWasOn = repository.getScreenStateAt(lastAccessTime)
      val status = computeSuspiciousStatus(screenWasOn, appWasInForeground)

      repository.insertPermissionLog(
        PermissionLog(
          packageName = pkg,
          appName = getAppName(pkg),
          permission = opName,
          timestamp = lastAccessTime,
          appState = appWasInForeground,
          screenState = screenWasOn,
          suspiciousStatus = status.name
        )
      )
    }
  }

  // ─── Query UsageStats at exact timestamp ──────────────────────────────────

  private fun wasAppInForegroundAt(pkg: String, timestamp: Long): Boolean {
    return try {
      val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
      // Query 30s window around the timestamp
      val events = usm.queryEvents(timestamp - 30000, timestamp + 1000)
      val event = UsageEvents.Event()
      var lastForeground = ""
      while (events.hasNextEvent()) {
        events.getNextEvent(event)
        if (event.timeStamp > timestamp) break
        when (event.eventType) {
          UsageEvents.Event.MOVE_TO_FOREGROUND -> lastForeground = event.packageName
          UsageEvents.Event.MOVE_TO_BACKGROUND ->
          if (lastForeground == event.packageName) lastForeground = ""
        }
      }
      lastForeground == pkg
    } catch (e: Exception) {
      false
    }
  }

  // ─── Shizuku command runner ───────────────────────────────────────────────

  private fun runShizukuCommand(cmd: Array<String>): String? {
    if (!ShizukuHelper.isShizukuRunning() || !ShizukuHelper.isPermissionGranted()) return null
    return try {
      val clazz = Class.forName("rikka.shizuku.Shizuku")
      val method = clazz.getDeclaredMethod(
        "newProcess",
        Array<String>::class.java,
        Array<String>::class.java,
        String::class.java
      )
      method.isAccessible = true
      val process = method.invoke(
        null, cmd, null, null
      ) as rikka.shizuku.ShizukuRemoteProcess
      val output = BufferedReader(InputStreamReader(process.inputStream))
      .use {
        it.readText()
      }
      process.waitFor()
      process.destroy()
      output
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  // ─── Parse "time=+7m42s914ms ago" → absolute timestamp ───────────────────

  private fun parseTimeAgo(line: String, now: Long): Long? {
    return try {
      val timeMatch = Regex("""time=\+([^;]+?)\s+ago""").find(line) ?: return null
      val timeStr = timeMatch.groupValues[1].trim()
      var totalMs = 0L
      Regex("""(\d+)d""").find(timeStr)?.let {
        totalMs += it.groupValues[1].toLong() * 24 * 60 * 60 * 1000
      }
      Regex("""(\d+)h""").find(timeStr)?.let {
        totalMs += it.groupValues[1].toLong() * 60 * 60 * 1000
      }
      Regex("""(\d+)m(?!s)""").find(timeStr)?.let {
        totalMs += it.groupValues[1].toLong() * 60 * 1000
      }
      Regex("""(\d+)ms""").find(timeStr)?.let {
        totalMs += it.groupValues[1].toLong()
      }
      Regex("""(\d+)(?<!m)s""").find(timeStr)?.let {
        totalMs += it.groupValues[1].toLong() * 1000
      }
      now - totalMs
    } catch (e: Exception) {
      null
    }
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  private fun getAppName(packageName: String): String {
    return try {
      packageManager.getApplicationLabel(
        packageManager.getApplicationInfo(packageName, 0)
      ).toString()
    } catch (e: Exception) {
      packageName
    }
  }

  // ─── Lifecycle ───────────────────────────────────────────────────────────

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    super.onDestroy()
    usageMonitorJob?.cancel()
    stopPermissionMonitoring()
    serviceScope.cancel()
    screenReceiver?.let {
      unregisterReceiver(it)
    }
  }

  private fun createNotificationChannel() {
    val channel = NotificationChannel(
      CHANNEL_ID,
      getString(R.string.notification_channel_name),
      NotificationManager.IMPORTANCE_LOW
    ).apply {
      description = getString(R.string.notification_channel_desc)
      setShowBadge(false)
    }
    getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
  }

  private fun buildNotification(): Notification {
    val pendingIntent = PendingIntent.getActivity(
      this, 0,
      Intent(this, MainActivity::class.java),
      PendingIntent.FLAG_IMMUTABLE
    )
    return NotificationCompat.Builder(this, CHANNEL_ID)
    .setContentTitle(getString(R.string.notification_title))
    .setContentText(getString(R.string.notification_text))
    .setSmallIcon(android.R.drawable.ic_menu_view)
    .setContentIntent(pendingIntent)
    .setOngoing(true)
    .setPriority(NotificationCompat.PRIORITY_LOW)
    .build()
  }
}