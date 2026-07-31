package com.warden.rnsl.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.warden.rnsl.data.model.ScreenEvent
import com.warden.rnsl.data.repository.WardenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isOn = intent.action == Intent.ACTION_SCREEN_ON
        // Update in-memory state directly via the var
        WardenService.isScreenOn = isOn
        // Save to DB
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = WardenRepository(context)
                repository.insertScreenEvent(
                    ScreenEvent(
                        timestamp = System.currentTimeMillis(),
                        isOn = isOn
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
