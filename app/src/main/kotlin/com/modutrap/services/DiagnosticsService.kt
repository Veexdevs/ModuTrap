package com.modutrap.services

import android.content.Context
import android.os.Build
import com.modutrap.core.Logger
import com.modutrap.models.DiagnosticReport
import com.modutrap.repositories.FFlagRepository
import com.modutrap.roblox.RobloxManager
import com.modutrap.settings.SettingsRepository
import com.modutrap.shizuku.ShizukuManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Builds a human-readable diagnostics report covering the device, the Roblox
 * installation, the Shizuku integration, the Fast Flag store and a settings
 * summary. Purely read-only — it never modifies anything.
 */
class DiagnosticsService(
    private val context: Context,
    private val roblox: RobloxManager,
    private val shizuku: ShizukuManager,
    private val settings: SettingsRepository,
    private val flags: FFlagRepository
) {

    fun build(): DiagnosticReport {
        val notes = mutableListOf<String>()

        val s = settings.settings.value
        val robloxStatus = roblox.status(s.preferredPackage, s.customPackage)
        val shizukuStatus = shizuku.status(context)

        val device = buildString {
            appendLine("Manufacturer : ${Build.MANUFACTURER}")
            appendLine("Model        : ${Build.MODEL}")
            appendLine("Brand        : ${Build.BRAND}")
            appendLine("Product      : ${Build.PRODUCT}")
            appendLine("Android      : ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Security patch: ${Build.VERSION.SECURITY_PATCH ?: "unknown"}")
            appendLine("ABI          : ${Build.SUPPORTED_ABIS.joinToString(", ")}")
        }

        val robloxText = buildString {
            appendLine("Preferred    : ${robloxStatus.packageName}")
            appendLine("Installed    : ${robloxStatus.installed}")
            appendLine("Version      : ${robloxStatus.versionName ?: "n/a"}")
            appendLine("Debug build  : ${robloxStatus.isDebugBuild}")
            appendLine("Launch intent: ${robloxStatus.hasLaunchIntent}")
            appendLine("Package seen : ${robloxStatus.packageVisible}")
            appendLine("Data dir     : ${robloxStatus.dataDir ?: "n/a"}")
            val installed = roblox.installedPackages()
            appendLine("Known pkgs   : ${if (installed.isEmpty()) "none" else installed.joinToString(", ")}"
            )
        }

        val shizukuText = buildString {
            appendLine("Installed    : ${shizukuStatus.installed}")
            appendLine("Binder alive : ${shizukuStatus.binderAlive}")
            appendLine("Permission   : ${shizukuStatus.permissionGranted}")
            appendLine("Rationale    : ${shizukuStatus.shouldShowRationale}")
            appendLine("UID          : ${shizukuStatus.uid ?: "n/a"}")
            appendLine("Version      : ${shizukuStatus.version ?: "n/a"}")
            appendLine("Ready        : ${shizukuStatus.ready}")
        }

        val flagList = flags.flags.value
        val verified = flagList.count { com.modutrap.flags.FastFlagValidator.isVerified(it.key) }
        val flagsText = buildString {
            appendLine("Total flags  : ${flagList.size}")
            appendLine("Verified     : $verified")
            appendLine("Unverified   : ${flagList.size - verified}")
            appendLine("Local file   : ${com.modutrap.core.AppPaths.clientAppSettingsFile.absolutePath}")
        }

        val settingsText = buildString {
            appendLine("Graphics     : quality=${s.graphicsQuality}, distance=${s.renderDistance}, msaa=$s.msaaMode")
            appendLine("  shadows=${s.shadowsEnabled}, postfx=${s.postFxEnabled}, terrain=${s.terrainTextures}, dynRes=${s.dynamicResolution}")
            appendLine("Performance  : fpsCap=${s.fpsCap}, gc=${s.enableGCOptimization}, reduceMotion=${s.reduceMotion}")
            appendLine("  clearCache=${s.clearCacheOnLaunch}, limitBg=${s.limitBackgroundProcesses}")
            appendLine("Launch       : pkg=${s.preferredPackage}, custom=${s.customPackage.ifBlank { "-" }}")
            appendLine("  applyFlags=${s.applyFlagsOnLaunch}, applyOptim=${s.applyOptimizationsOnLaunch}")
            appendLine("  lastLaunch=${s.lastLaunchTs.ifBlank { "never" }}")
            appendLine("Personaliz.  : theme=${s.themeMode}, accent=${s.accentColor}")
        }

        if (!shizukuStatus.installed) {
            notes.add("Shizuku não está instalado — as Fast Flags não serão injetadas.")
        } else if (!shizukuStatus.ready) {
            notes.add("Shizuku instalado mas não autorizado — inicialize o serviço e autorize o app.")
        }
        if (!robloxStatus.installed) {
            notes.add("Roblox não está instalado neste dispositivo.")
        }
        if (flagList.isNotEmpty() && !shizukuStatus.ready) {
            notes.add("Fast Flags armazenadas localmente, mas sem Shizuku elas não terão efeito no Roblox.")
        }

        try {
            Logger.writeLine("DiagnosticsService::build", "Report generated (${flagList.size} flags).")
        } catch (_: Exception) {
        }

        return DiagnosticReport(
            generatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
            device = device,
            roblox = robloxText,
            shizuku = shizukuText,
            flags = flagsText,
            settings = settingsText,
            notes = notes
        )
    }
}
