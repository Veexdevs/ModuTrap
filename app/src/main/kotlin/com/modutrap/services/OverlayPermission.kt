package com.modutrap.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Helpers for the floating in-game overlay's `SYSTEM_ALERT_WINDOW` permission.
 *
 * On API 26+ the app must be granted "Display over other apps" to draw the
 * ModuTrap menu on top of Roblox. This checks the grant and builds the intent
 * that opens the system's overlay-permission settings screen.
 */
object OverlayPermission {

    fun isGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            // Pre-M: always allowed, no permission gate.
            true
        }
    }

    /** Intent that opens the "Display over other apps" settings screen. */
    fun manageIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /** Start the system overlay-permission settings screen. */
    fun request(activity: Activity) {
        runCatching {
            activity.startActivity(manageIntent(activity))
        }
    }
}
