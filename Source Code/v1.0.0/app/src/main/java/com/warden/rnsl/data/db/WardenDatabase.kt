package com.warden.rnsl.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.warden.rnsl.data.model.AppSession
import com.warden.rnsl.data.model.PermissionLog
import com.warden.rnsl.data.model.ScreenEvent

@Database(
    entities = [PermissionLog::class, AppSession::class, ScreenEvent::class],
    version = 1,
    exportSchema = false
)
abstract class WardenDatabase : RoomDatabase() {
    abstract fun permissionLogDao(): PermissionLogDao
    abstract fun appSessionDao(): AppSessionDao
    abstract fun screenEventDao(): ScreenEventDao

    companion object {
        @Volatile
        private var INSTANCE: WardenDatabase? = null

        fun getInstance(context: Context): WardenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WardenDatabase::class.java,
                    "warden_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
