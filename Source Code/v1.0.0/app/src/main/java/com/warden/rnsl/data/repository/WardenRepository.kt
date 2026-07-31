package com.warden.rnsl.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.warden.rnsl.data.db.WardenDatabase
import com.warden.rnsl.data.model.*
import kotlinx.coroutines.flow.Flow

class WardenRepository(private val context: Context) {

    private val db = WardenDatabase.getInstance(context)
    private val permLogDao = db.permissionLogDao()
    private val sessionDao = db.appSessionDao()
    private val screenDao = db.screenEventDao()

    // ---- Apps ----
    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        return packages.map { pkg ->
            val permissions = pkg.requestedPermissions?.toList() ?: emptyList()
            AppInfo(
                packageName = pkg.packageName,
                appName = pkg.applicationInfo.loadLabel(pm).toString(),
                isSystemApp = (pkg.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                installedDate = pkg.firstInstallTime,
                versionName = pkg.versionName ?: "N/A",
                permissions = permissions,
                icon = pkg.applicationInfo.loadIcon(pm)
            )
        }.sortedBy { it.appName }
    }

    fun getAppInfo(packageName: String): AppInfo? {
        return getInstalledApps().find { it.packageName == packageName }
    }

    // ---- Permission Logs ----
    fun getAllLogs(): Flow<List<PermissionLog>> = permLogDao.getAllLogs()
    fun getLogsForApp(pkg: String): Flow<List<PermissionLog>> = permLogDao.getLogsForApp(pkg)
    fun getBadLogs(): Flow<List<PermissionLog>> = permLogDao.getBadLogs()
    fun getRecentLogs(): Flow<List<PermissionLog>> = permLogDao.getRecentLogs()
    fun getTotalLogCount(): Flow<Int> = permLogDao.getTotalCount()
    fun getBadCountToday(): Flow<Int> {
        val todayStart = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return permLogDao.getBadCountSince(todayStart)
    }

    suspend fun insertPermissionLog(log: PermissionLog) = permLogDao.insert(log)
    suspend fun clearAllLogs() {
        permLogDao.clearAll()
        sessionDao.clearAll()
        screenDao.clearAll()
    }

    // ---- Sessions ----
    fun getAllSessions(): Flow<List<AppSession>> = sessionDao.getAllSessions()
    suspend fun insertSession(session: AppSession) = sessionDao.insert(session)
    suspend fun updateSession(session: AppSession) = sessionDao.update(session)
    suspend fun getOpenSession(): AppSession? = sessionDao.getOpenSession()

    // ---- Screen Events ----
    fun getRecentScreenEvents(): Flow<List<ScreenEvent>> = screenDao.getRecentEvents()
    suspend fun insertScreenEvent(event: ScreenEvent) = screenDao.insert(event)
    suspend fun getLastScreenState(): Boolean = screenDao.getLastScreenState() ?: true

    // New: get screen state at exact timestamp
    suspend fun getScreenStateAt(timestamp: Long): Boolean =
        screenDao.getScreenStateAt(timestamp) ?: true
}
