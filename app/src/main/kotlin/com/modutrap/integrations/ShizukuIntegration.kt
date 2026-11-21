package com.modutrap.integrations

import android.content.Context
import com.modutrap.models.ShizukuStatus
import com.modutrap.shizuku.ShizukuManager
import com.modutrap.shizuku.ShizukuManager.ShellResult

/**
 * High-level Shizuku integration facade used by ViewModels and the UI.
 *
 * It wraps [ShizukuManager] (the low-level binder bridge) and exposes only the
 * operations the app actually needs: status, permission request, shell
 * execution and file writes. It never attempts to bypass any security or
 * anti-cheat mechanism — only documented, permitted Shizuku operations.
 */
class ShizukuIntegration(
    private val manager: ShizukuManager,
    private val context: Context
) {

    fun status(): ShizukuStatus = manager.status(context)

    fun isAvailable(): Boolean = manager.isReady()

    fun isInstalled(): Boolean = manager.isShizukuInstalled(context)

    fun isBinderAlive(): Boolean = manager.isBinderAlive()

    fun hasPermission(): Boolean = manager.hasPermission()

    fun shouldShowRationale(): Boolean = manager.shouldShowRationale()

    /** Request permission. Returns true when already granted. */
    fun requestPermission(): Boolean = manager.requestPermission()

    fun runShellCommand(script: String, timeoutMs: Long = 15_000L): ShellResult? =
        manager.runShellCommand(script, timeoutMs)

    fun writeFile(path: String, content: String): Boolean = manager.writeFile(path, content)

    fun readFile(path: String): String? = manager.readFile(path)

    fun registerListeners(onGranted: () -> Unit, onLost: () -> Unit) =
        manager.init(onGranted, onLost)

    fun cleanup(listeners: ShizukuManager.ShizukuListeners) =
        manager.cleanup(listeners)
}
