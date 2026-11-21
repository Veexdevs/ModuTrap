package com.modutrap.services

import android.content.Context
import android.content.Intent
import com.modutrap.core.Logger
import com.modutrap.models.LaunchResult
import com.modutrap.repositories.FFlagRepository
import com.modutrap.roblox.RobloxManager
import com.modutrap.settings.AppSettings
import com.modutrap.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The Roblox launch pipeline:
 *
 *   1. Resolve the preferred Roblox package.
 *   2. Verify it is installed (and has a launch intent).
 *   3. Load the local Fast Flag store.
 *   4. If enabled, apply optimizations (clear cache, kill background processes).
 *   5. If enabled, build + inject ClientAppSettings.json (via Shizuku).
 *   6. Send the launch intent.
 *   7. Record the launch timestamp.
 *
 * A failure to apply flags/optimizations never blocks the launch — Roblox still
 * starts, and the non-fatal notice is surfaced to the user.
 */
class RobloxLauncher(
    private val context: Context,
    private val settings: SettingsRepository,
    private val flags: FFlagRepository,
    private val roblox: RobloxManager,
    private val applier: FastFlagApplier
) {

    suspend fun launch(): LaunchResult = withContext(Dispatchers.IO) {
        val current = settings.settings.value
        Logger.writeLine("RobloxLauncher::launch", "Applying changes for ${current.preferredPackage}")

        val pkg = roblox.resolvePackage(current.preferredPackage, current.customPackage)
        if (!roblox.isInstalled(pkg)) {
            Logger.writeLine("RobloxLauncher::launch", "Roblox not installed: $pkg")
            return@withContext LaunchResult.NotInstalled
        }

        flags.load()

        val notices = mutableListOf<String>()

        // ---- Optimizations (privileged, best-effort) ----
        if (current.applyOptimizationsOnLaunch) {
            if (current.clearCacheOnLaunch) {
                applier.clearCache(roblox.dataDir(pkg)).onFailure {
                    notices.add("Não foi possível limpar o cache: ${it.message}")
                }
            }
            if (current.limitBackgroundProcesses) {
                applier.killBackgroundProcesses().onFailure {
                    notices.add("Não foi possível limitar processos: ${it.message}")
                }
            }
        }

        // ---- Fast Flag injection ----
        if (current.applyFlagsOnLaunch) {
            val json = applier.buildClientAppSettings(current)
            when (val result = applier.inject(json, roblox.dataDir(pkg))) {
                is FlagApplyResult.Applied ->
                    Logger.writeLine("RobloxLauncher::launch", "Flags applied (${result.count}).")

                is FlagApplyResult.Skipped -> notices.add(result.reason)
                is FlagApplyResult.Failed -> notices.add(result.message)
            }
        } else {
            Logger.writeLine("RobloxLauncher::launch", "Skipping flag injection (disabled).")
        }

        // ---- Launch ----
        val launchIntent: Intent? = context.packageManager.getLaunchIntentForPackage(pkg)
        if (launchIntent == null) {
            return@withContext LaunchResult.Failure("Não existe intent de inicialização para $pkg.")
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(launchIntent)
        } catch (e: Exception) {
            Logger.writeException("RobloxLauncher::launch", e)
            return@withContext LaunchResult.Failure("Falha ao abrir o Roblox: ${e.message}", e)
        }

        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        settings.setLastLaunchTs(ts)
        Logger.writeLine("RobloxLauncher::launch", "Launched $pkg at $ts")
        LaunchResult.Success(notices)
    }
}
