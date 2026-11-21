package com.modutrap.roblox

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.modutrap.core.Logger
import com.modutrap.models.RobloxStatus

/**
 * Locates and queries the Roblox installation on the device.
 *
 * On Android 11+ (API 30+) package visibility rules mean `getPackageInfo()`
 * on another app throws [PackageManager.NameNotFoundException] unless the
 * target package is declared in a `<queries>` block (we declare both known
 * Roblox packages in the manifest). As a defensive fallback we also probe for a
 * launch intent, which the system resolves for installed apps regardless of
 * visibility, and `getApplicationInfo` as a third path.
 *
 * This class never bundles or ships the proprietary Roblox APK — it only
 * detects and launches an independently installed official client.
 */
class RobloxManager(private val context: Context) {

    private val pm: PackageManager by lazy { context.packageManager }

    /** Resolve the package name the user prefers into a concrete package string. */
    fun resolvePackage(preferred: String, custom: String): String {
        return when (preferred) {
            "vng" -> PACKAGE_VNG
            "custom" -> if (custom.isNotBlank()) custom.trim() else PACKAGE_GLOBAL
            else -> PACKAGE_GLOBAL
        }
    }

    /** Three-layer detection that survives Android 11+ package-visibility rules. */
    fun isInstalled(pkg: String): Boolean {
        if (pkg.isBlank()) return false

        runCatching {
            pm.getPackageInfo(pkg, 0)
            return true
        }.onFailure { e ->
            if (e !is PackageManager.NameNotFoundException) {
                Logger.writeLine("RobloxManager::isInstalled", "getPackageInfo($pkg): ${e.message}")
            }
        }

        runCatching {
            if (pm.getLaunchIntentForPackage(pkg) != null) {
                Logger.writeLine("RobloxManager::isInstalled", "Detected $pkg via launch intent.")
                return true
            }
        }.onFailure { Logger.writeLine("RobloxManager::isInstalled", "launch intent: ${it.message}") }

        runCatching {
            pm.getApplicationInfo(pkg, 0)
            return true
        }.onFailure { }

        return false
    }

    fun installedPackages(): List<String> =
        listOf(PACKAGE_GLOBAL, PACKAGE_VNG).filter { isInstalled(it) }

    fun anyInstalled(): Boolean = installedPackages().isNotEmpty()

    fun versionName(pkg: String): String? = runCatching {
        pm.getPackageInfo(pkg, 0).versionName
    }.getOrNull()

    fun isDebugBuild(pkg: String): Boolean = runCatching {
        val info: ApplicationInfo = pm.getApplicationInfo(pkg, 0)
        (info.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }.getOrDefault(false)

    fun dataDir(pkg: String): String? = runCatching {
        pm.getApplicationInfo(pkg, 0).dataDir.absolutePath
    }.getOrNull()

    fun hasLaunchIntent(pkg: String): Boolean = runCatching {
        pm.getLaunchIntentForPackage(pkg) != null
    }.getOrDefault(false)

    /** Full status snapshot for the given preference, or an "not installed" status. */
    fun status(preferred: String, custom: String): RobloxStatus {
        val pkg = resolvePackage(preferred, custom)
        val installed = isInstalled(pkg)
        return RobloxStatus(
            installed = installed,
            packageName = pkg,
            versionName = if (installed) versionName(pkg) else null,
            dataDir = if (installed) dataDir(pkg) else null,
            isDebugBuild = if (installed) isDebugBuild(pkg) else false,
            hasLaunchIntent = if (installed) hasLaunchIntent(pkg) else false,
            packageVisible = runCatching { pm.getPackageInfo(pkg, 0); true }.getOrDefault(false)
        )
    }

    /**
     * Open the official Roblox install page. Prefers the Play Store and falls
     * back to the browser when the Play Store is not installed or unavailable.
     * Returns true if an activity was started.
     */
    fun openInstallPage(): Boolean {
        val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PACKAGE_GLOBAL"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val storeResolved = runCatching {
            context.startActivity(storeIntent)
            true
        }.getOrDefault(false)

        if (storeResolved) {
            Logger.writeLine("RobloxManager::openInstallPage", "Opened Play Store.")
            return true
        }

        return runCatching {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(INSTALL_URL))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
            Logger.writeLine("RobloxManager::openInstallPage", "Opened browser fallback.")
            true
        }.onFailure {
            Logger.writeException("RobloxManager::openInstallPage", it as? ActivityNotFoundException ?: it)
            false
        }.isSuccess
    }

    companion object {
        const val PACKAGE_GLOBAL = "com.roblox.client"
        const val PACKAGE_VNG = "com.roblox.client.vnggames"
        const val INSTALL_URL = "https://www.roblox.com/downloads"
    }
}
