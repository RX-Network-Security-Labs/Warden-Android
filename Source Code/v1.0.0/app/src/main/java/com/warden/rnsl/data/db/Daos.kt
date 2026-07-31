package com.warden.rnsl.data.db

import androidx.room.*
import com.warden.rnsl.data.model.AppSession
import com.warden.rnsl.data.model.PermissionLog
import com.warden.rnsl.data.model.ScreenEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: PermissionLog)

    @Query("SELECT * FROM permission_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<PermissionLog>>

    @Query("SELECT * FROM permission_logs WHERE packageName = :pkg ORDER BY timestamp DESC")
    fun getLogsForApp(pkg: String): Flow<List<PermissionLog>>

    @Query("SELECT * FROM permission_logs WHERE suspiciousStatus = 'BAD' ORDER BY timestamp DESC")
    fun getBadLogs(): Flow<List<PermissionLog>>

    @Query("SELECT COUNT(*) FROM permission_logs WHERE suspiciousStatus = 'BAD' AND timestamp > :since")
    fun getBadCountSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM permission_logs")
    fun getTotalCount(): Flow<Int>

    @Query("DELETE FROM permission_logs")
    suspend fun clearAll()

    @Query("SELECT * FROM permission_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<PermissionLog>>
}

@Dao
interface AppSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AppSession)

    @Update
    suspend fun update(session: AppSession)

    @Query("SELECT * FROM app_sessions ORDER BY openTime DESC")
    fun getAllSessions(): Flow<List<AppSession>>

    @Query("SELECT * FROM app_sessions WHERE packageName = :pkg ORDER BY openTime DESC")
    fun getSessionsForApp(pkg: String): Flow<List<AppSession>>

    @Query("SELECT * FROM app_sessions WHERE closeTime = 0 LIMIT 1")
    suspend fun getOpenSession(): AppSession?

    @Query("DELETE FROM app_sessions")
    suspend fun clearAll()
}

@Dao
interface ScreenEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ScreenEvent)

    @Query("SELECT * FROM screen_events ORDER BY timestamp DESC LIMIT 100")
    fun getRecentEvents(): Flow<List<ScreenEvent>>

    @Query("SELECT isOn FROM screen_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastScreenState(): Boolean?

    // New: get screen state at exact timestamp
    @Query("SELECT isOn FROM screen_events WHERE timestamp <= :timestamp ORDER BY timestamp DESC LIMIT 1")
    suspend fun getScreenStateAt(timestamp: Long): Boolean?

    @Query("DELETE FROM screen_events")
    suspend fun clearAll()
}
