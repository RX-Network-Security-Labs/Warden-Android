package com.warden.rnsl.ui

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.warden.rnsl.data.model.AppInfo
import com.warden.rnsl.data.model.PermissionLog
import com.warden.rnsl.data.repository.WardenRepository
import com.warden.rnsl.util.WardenPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WardenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WardenRepository(application)
    val prefs = WardenPrefs(application)

    // Apps
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    private val _appFilter = MutableStateFlow(AppFilter.ALL)
    val appFilter: StateFlow<AppFilter> = _appFilter

    val filteredApps: StateFlow<List<AppInfo>> = combine(_apps, _appFilter) { apps, filter ->
        when (filter) {
            AppFilter.ALL -> apps
            AppFilter.USER -> apps.filter { !it.isSystemApp }
            AppFilter.SYSTEM -> apps.filter { it.isSystemApp }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Logs
    val allLogs: StateFlow<List<PermissionLog>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentLogs: StateFlow<List<PermissionLog>> = repository.getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val badLogs: StateFlow<List<PermissionLog>> = repository.getBadLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalLogCount: StateFlow<Int> = repository.getTotalLogCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val badCountToday: StateFlow<Int> = repository.getBadCountToday()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Settings
    val liveLoggingEnabled: StateFlow<Boolean> = prefs.liveLoggingEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val darkTheme: StateFlow<Boolean> = prefs.darkTheme
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Loading state
    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val list = withContext(Dispatchers.IO) { repository.getInstalledApps() }
            _apps.value = list
            _isLoadingApps.value = false
        }
    }

    fun setFilter(filter: AppFilter) {
        _appFilter.value = filter
    }

    fun setDarkTheme(dark: Boolean) {
        viewModelScope.launch { prefs.setDarkTheme(dark) }
    }

    fun setLiveLogging(enabled: Boolean) {
        viewModelScope.launch { prefs.setLiveLogging(enabled) }
    }

    fun clearAllLogs() {
        viewModelScope.launch { repository.clearAllLogs() }
    }

    fun getAppInfo(packageName: String): AppInfo? {
        return _apps.value.find { it.packageName == packageName }
    }

    fun getLogsForApp(packageName: String): Flow<List<PermissionLog>> {
        return repository.getLogsForApp(packageName)
    }

    fun openDeveloperOptions() {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(intent)
    }

    fun openAppPermissionSettings(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(intent)
    }

    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(intent)
    }
}

enum class AppFilter { ALL, USER, SYSTEM }
