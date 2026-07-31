package com.warden.rnsl

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.navigation.WardenNavHost
import com.warden.rnsl.ui.theme.WardenTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WardenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Check if usage stats permission is granted, if not prompt user
        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        setContent {
            val darkTheme by viewModel.darkTheme.collectAsState()
            WardenTheme(darkTheme = darkTheme) {
                WardenNavHost(viewModel = viewModel)
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
