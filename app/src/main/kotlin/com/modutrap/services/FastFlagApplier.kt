package com.modutrap.services

import com.modutrap.core.Logger
import com.modutrap.repositories.FFlagRepository
import com.modutrap.shizuku.ShizukuManager
import com.modutrap.settings.AppSettings
import com.modutrap.utils.JsonUtils

/** Result of attempting to inject Fast Flags into Roblox. */
sealed class FlagApplyResult {
    data class Applied(val count: Int, val targetPath: String) : FlagApplyResult()

    /**
     * Injection was skipped because the app has no privileges. Roblox will
     * still launch, just with its default flags.
     */
    data class Skipped(val reason: String) : FlagApplyResult()

    data class Failed(val message: String) : FlagApplyResult()
}

/**
 * Translates the app's Graphics/Performance settings into real Roblox Fast
 * Flags, builds the `ClientAppSettings.json` document, and injects it into
 * Roblox's data directory via the Shizuku shell.
 *
 * Only flags present in the verified catalog are emitted — no fabricated flags.
 * Settings that are not injectable as flags (cache clearing, background
 * process limiting) are performed directly through Shizuku instead.
 */
class FastFlagApplier(
    private val flags: FFlagRepository,
    private val shizuku: ShizukuManager
) {

    /** Map the Graphics + Performance settings to Fast Flags. */
    fun buildSettingsFlags(settings: AppSettings): Map<String, String> {
        val map = mutableMapOf<String, String>()

        // ---- Graphics ----
        when (settings.graphicsQuality) {
            1 -> {
                map["FFlagGraphicsQualityAutoLevel"] = "False"
                map["FIntQualityLevel"] = "1"
            }
            2 -> {
                map["FFlagGraphicsQualityAutoLevel"] = "False"
                map["FIntQualityLevel"] = "2"
            }
            3 -> {
                map["FFlagGraphicsQualityAutoLevel"] = "False"
                map["FIntQualityLevel"] = "3"
            }
            0 -> map["FFlagGraphicsQualityAutoLevel"] = "True"
        }

        // MSAA: Off -> 1 (disabled), otherwise the sample count.
        val msaaSamples = when (settings.msaaMode) {
            0 -> 1
            1 -> 2
            2 -> 4
            3 -> 8
            else -> 1
        }
        map["FIntDebugForceMSAASamples"] = msaaSamples.toString()

        // Shadows.
        map["FFlagRenderShadow"] = bool(settings.shadowsEnabled)
        map["FFlagDisableShadowRender"] = bool(!settings.shadowsEnabled)

        // Post-processing.
        map["FFlagDisablePostFx"] = bool(!settings.postFxEnabled)

        // Terrain textures.
        map["FFlagTerrainSkipTextures"] = bool(!settings.terrainTextures)

        // Dynamic resolution.
        map["FFlagRenderDynamicResolutionScale9"] = bool(settings.dynamicResolution)

        // ---- Performance ----
        if (settings.fpsCap > 0) {
            map["DFIntTaskSchedulerTargetFps"] = settings.fpsCap.toString()
            map["FFlagTaskSchedulerLimitTargetFpsTo2402"] = bool(settings.fpsCap <= 240)
        }

        if (settings.enableGCOptimization) {
            map["FFlagLuauCodegen"] = "True"
            map["FFlagDebugDisableScriptingGC"] = "False"
        } else {
            map["FFlagLuauCodegen"] = "False"
        }

        return map
    }

    /** Build the merged ClientAppSettings.json: user flags + settings flags. */
    suspend fun buildClientAppSettings(settings: AppSettings): String {
        val userFlags = flags.flags.value.associate { it.key to it.value }
        val merged = buildSettingsFlags(settings) + userFlags
        return JsonUtils.encodeFlags(merged)
    }

    /**
     * Inject the given JSON into Roblox's data directory. Requires Shizuku.
     * [robloxDataDir] is the Roblox package data directory (resolved by the
     * launcher); the file is written to `<robloxDataDir>/ClientAppSettings.json`.
     */
    suspend fun inject(json: String, robloxDataDir: String?): FlagApplyResult {
        val target = robloxDataDir?.let { "$it/ClientAppSettings.json" }
        if (target.isNullOrBlank()) {
            return FlagApplyResult.Failed("Diretório de dados do Roblox não encontrado.")
        }
        if (!shizuku.isReady()) {
            return FlagApplyResult.Skipped(
                "Shizuku não autorizado. As Fast Flags ficarão pendentes até que o Shizuku seja autorizado."
            )
        }
        val ok = shizuku.writeFile(target, json)
        if (ok) {
            Logger.writeLine("FastFlagApplier::inject", "Injected $target")
            return FlagApplyResult.Applied(flags.count(), target)
        }
        return FlagApplyResult.Failed("Falha ao escrever ClientAppSettings.json no diretório do Roblox.")
    }

    /** Clear Roblox's local cache via Shizuku. Best-effort. */
    suspend fun clearCache(robloxDataDir: String?): Result<Unit> {
        if (!shizuku.isReady()) {
            return Result.failure(IllegalStateException("Shizuku não autorizado."))
        }
        val dir = robloxDataDir ?: return Result.failure(IllegalStateException("Diretório do Roblox ausente."))
        val result = shizuku.runShellCommand("rm -rf '$dir/Cache' 2>/dev/null; true", timeoutMs = 20_000L)
            ?: return Result.failure(IllegalStateException("Comando Shizuku falhou."))
        return if (result.success) Result.success(Unit)
        else Result.failure(IllegalStateException(result.stderr.ifBlank { result.stdout }))
    }

    /**
     * Kill background Roblox processes via Shizuku so the next launch starts
     * clean (the "limit background processes" performance toggle). Best-effort.
     */
    suspend fun killBackgroundProcesses(): Result<Unit> {
        if (!shizuku.isReady()) {
            return Result.failure(IllegalStateException("Shizuku não autorizado."))
        }
        val script = "ps -A -o PID,CMD 2>/dev/null | grep -i roblox | grep -v grep | awk '{print $1}' | xargs -r kill 2>/dev/null; true"
        val result = shizuku.runShellCommand(script, timeoutMs = 15_000L)
            ?: return Result.failure(IllegalStateException("Comando Shizuku falhou."))
        return if (result.success) Result.success(Unit)
        else Result.failure(IllegalStateException(result.stderr.ifBlank { result.stdout }))
    }

    private fun bool(value: Boolean): String = if (value) "True" else "False"
}
