package com.warden.rnsl.adb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives the inline reply from the ADB pairing notification.
 * User types the 6-digit code directly in the notification → this fires.
 */
class AdbPairingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AdbHelper.ACTION_PAIRING_CODE_REPLY) return

        val bundle = RemoteInput.getResultsFromIntent(intent) ?: return
        val code = bundle.getCharSequence(AdbHelper.KEY_PAIRING_CODE)
            ?.toString()
            ?.trim()
            ?.filter { it.isDigit() }
            ?: return

        if (code.length < 6) {
            // Show error notification
            val nm = context.getSystemService(android.app.NotificationManager::class.java)
            val notification = androidx.core.app.NotificationCompat.Builder(
                context, AdbHelper.PAIRING_CHANNEL_ID
            )
                .setContentTitle("Invalid Code")
                .setContentText("Pairing code must be at least 6 digits. Try again.")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setAutoCancel(true)
                .build()
            nm.notify(AdbHelper.PAIRING_NOTIFICATION_ID, notification)
            return
        }

        // Launch pairing on IO in a fire-and-forget scope
        CoroutineScope(Dispatchers.IO).launch {
            AdbHelper.pairWithCode(context, code)
        }
    }
}
