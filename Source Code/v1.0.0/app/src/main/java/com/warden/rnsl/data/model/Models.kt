package com.warden.rnsl.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents an installed app
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val installedDate: Long,
    val versionName: String,
    val permissions: List<String>,
    val icon: android.graphics.drawable.Drawable? = null
)

// Enum for suspicious status
enum class SuspiciousStatus {
    GOOD,    // Screen On + App On
    BAD,     // Screen Off + App Off using permission / Screen On + App Off
    UNKNOWN  // Screen Off + App On
}

// Permission log entry stored in Room
@Entity(tableName = "permission_logs")
data class PermissionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val permission: String,
    val timestamp: Long,
    val appState: Boolean,      // true = app in foreground
    val screenState: Boolean,   // true = screen on
    val suspiciousStatus: String // GOOD, BAD, UNKNOWN
)

// App session log - open/close tracking
@Entity(tableName = "app_sessions")
data class AppSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val openTime: Long,
    val closeTime: Long = 0,
    val duration: Long = 0  // milliseconds
)

// Screen state log
@Entity(tableName = "screen_events")
data class ScreenEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val isOn: Boolean
)

// Helper to compute suspicious status
fun computeSuspiciousStatus(screenOn: Boolean, appInForeground: Boolean): SuspiciousStatus {
    return when {
        screenOn && appInForeground -> SuspiciousStatus.GOOD
        !screenOn && !appInForeground -> SuspiciousStatus.BAD
        screenOn && !appInForeground -> SuspiciousStatus.BAD
        else -> SuspiciousStatus.UNKNOWN // screenOff + appOn
    }
}

fun SuspiciousStatus.toEmoji(): String = when (this) {
    SuspiciousStatus.GOOD -> "🟢 Good"
    SuspiciousStatus.BAD -> "🔴 Bad"
    SuspiciousStatus.UNKNOWN -> "⚪ Unknown"
}
