package com.warden.rnsl.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "warden_prefs")

object PrefsKeys {
    val LIVE_LOGGING_ENABLED = booleanPreferencesKey("live_logging_enabled")
    val DARK_THEME = booleanPreferencesKey("dark_theme")
    val LIVE_METHOD = stringPreferencesKey("live_method") // "shizuku" or "adb"
    val ADB_CONNECTED = booleanPreferencesKey("adb_connected")
    val SHIZUKU_GRANTED = booleanPreferencesKey("shizuku_granted")
}

class WardenPrefs(private val context: Context) {

    val liveLoggingEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[PrefsKeys.LIVE_LOGGING_ENABLED] ?: false }

    val darkTheme: Flow<Boolean> = context.dataStore.data
        .map { it[PrefsKeys.DARK_THEME] ?: false }

    val liveMethod: Flow<String> = context.dataStore.data
        .map { it[PrefsKeys.LIVE_METHOD] ?: "none" }

    val adbConnected: Flow<Boolean> = context.dataStore.data
        .map { it[PrefsKeys.ADB_CONNECTED] ?: false }

    val shizukuGranted: Flow<Boolean> = context.dataStore.data
        .map { it[PrefsKeys.SHIZUKU_GRANTED] ?: false }

    suspend fun setLiveLogging(enabled: Boolean) {
        context.dataStore.edit { it[PrefsKeys.LIVE_LOGGING_ENABLED] = enabled }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[PrefsKeys.DARK_THEME] = dark }
    }

    suspend fun setLiveMethod(method: String) {
        context.dataStore.edit { it[PrefsKeys.LIVE_METHOD] = method }
    }

    suspend fun setAdbConnected(connected: Boolean) {
        context.dataStore.edit { it[PrefsKeys.ADB_CONNECTED] = connected }
    }

    suspend fun setShizukuGranted(granted: Boolean) {
        context.dataStore.edit { it[PrefsKeys.SHIZUKU_GRANTED] = granted }
    }
}
