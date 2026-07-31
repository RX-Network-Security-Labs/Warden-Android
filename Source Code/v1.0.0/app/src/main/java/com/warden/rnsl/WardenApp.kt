package com.warden.rnsl

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import com.warden.rnsl.service.WardenService

class WardenApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Start the background service on app launch
        val serviceIntent = Intent(this, WardenService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
