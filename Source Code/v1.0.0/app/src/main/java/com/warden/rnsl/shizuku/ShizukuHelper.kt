package com.warden.rnsl.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuHelper {

    fun isShizukuInstalled(packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isShizukuRunning(): Boolean {
        return try {
            // Must check binderAlive first — pingBinder() throws if not attached yet
            Shizuku.getBinder() != null && Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun isPermissionGranted(): Boolean {
        return try {
            // isPreV11 itself can throw if binder not ready
            if (!isShizukuRunning()) return false
            if (Shizuku.isPreV11()) return false
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermission(requestCode: Int) {
        try {
            if (isShizukuRunning() && !Shizuku.isPreV11()) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addBinderReceivedListener(listener: Shizuku.OnBinderReceivedListener) {
        Shizuku.addBinderReceivedListener(listener)
    }

    fun removeBinderReceivedListener(listener: Shizuku.OnBinderReceivedListener) {
        Shizuku.removeBinderReceivedListener(listener)
    }

    fun addBinderDeadListener(listener: Shizuku.OnBinderDeadListener) {
        Shizuku.addBinderDeadListener(listener)
    }

    fun removeBinderDeadListener(listener: Shizuku.OnBinderDeadListener) {
        Shizuku.removeBinderDeadListener(listener)
    }

    fun addRequestPermissionResultListener(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.addRequestPermissionResultListener(listener)
    }

    fun removeRequestPermissionResultListener(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.removeRequestPermissionResultListener(listener)
    }

    fun getStatus(packageManager: PackageManager): ShizukuStatus {
        return when {
            !isShizukuInstalled(packageManager) -> ShizukuStatus.NOT_INSTALLED
            !isShizukuRunning() -> ShizukuStatus.NOT_RUNNING
            !isPermissionGranted() -> ShizukuStatus.NOT_GRANTED
            else -> ShizukuStatus.READY
        }
    }
}

enum class ShizukuStatus {
    NOT_INSTALLED,
    NOT_RUNNING,
    NOT_GRANTED,
    READY
}