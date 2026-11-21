package com.modutrap.repositories

import com.modutrap.core.AppPaths
import com.modutrap.core.Logger
import com.modutrap.models.FlagPreset
import com.modutrap.models.VisualPreset
import com.modutrap.utils.JsonUtils
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import java.io.File

/**
 * Persists user-created [FlagPreset] and [VisualPreset] bundles as JSON files
 * under [AppPaths.presetsDir], plus a set of built-in presets that ship with
 * the app.
 */
class PresetRepository {

    // ---- Built-in flag presets ----
    val builtInFlagPresets: List<FlagPreset> = listOf(
        FlagPreset(
            id = "builtin-balanced",
            name = "Desempenho equilibrado",
            description = "Balanço entre fluidez e qualidade visual.",
            flags = mapOf(
                "FFlagDisablePostFx" to "True",
                "FIntRenderShadowIntensity" to "0",
                "FIntDebugForceMSAASamples" to "1",
                "FFlagSkipFrameForSlowLoadingAssets" to "True",
                "FFlagDebugDisableTelemetryV2Event" to "True"
            )
        ),
        FlagPreset(
            id = "builtin-maxfps",
            name = "Máximo de FPS",
            description = "Prioriza a taxa de quadros.",
            flags = mapOf(
                "DFIntTaskSchedulerTargetFps" to "240",
                "FFlagTaskSchedulerLimitTargetFpsTo2402" to "True",
                "FFlagDisableShadowRender" to "True",
                "FIntRenderShadowIntensity" to "0",
                "FFlagSkipFrameForSlowLoadingAssets" to "True"
            )
        ),
        FlagPreset(
            id = "builtin-lowgraphics",
            name = "Gráficos baixos",
            description = "Reduz a carga gráfica para hardware básico.",
            flags = mapOf(
                "FFlagDisablePostFx" to "True",
                "FFlagDisableShadowRender" to "True",
                "FIntDebugForceMSAASamples" to "1",
                "FFlagTerrainSkipTextures" to "True",
                "DFFlagTextureQualityOverrideEnabled" to "True",
                "DFIntTextureQualityOverride" to "0"
            )
        ),
        FlagPreset(
            id = "builtin-privacy",
            name = "Privacidade (sem telemetria)",
            description = "Desativa envios de telemetria.",
            flags = mapOf(
                "FFlagDebugDisableTelemetryPoint" to "True",
                "FFlagDebugDisableTelemetryEphemeralCounter" to "True",
                "FFlagDebugDisableTelemetryEventIngest" to "True",
                "FFlagDebugDisableTelemetryV2Counter" to "True",
                "FFlagDebugDisableTelemetryV2Event" to "True",
                "FFlagDebugDisableTelemetryV2Stat" to "True",
                "FFlagDebugDisableTelemetryEphemeralStat" to "True",
                "DFStringHttpPointsReporterUrl" to ""
            )
        ),
        FlagPreset(
            id = "builtin-network",
            name = "Rede otimizada",
            description = "Otimiza conexão e entrada em jogos.",
            flags = mapOf(
                "FFlagNetworkCdnBypass" to "True",
                "FFlagNetworkBypassUdpThrottling" to "True",
                "FFlagReducePacketLoss" to "True",
                "FFlagEnableFastGameJoin" to "True",
                "FFlagEnableQuickGameLaunch" to "True"
            )
        )
    )

    // ---- Built-in visual presets ----
    val builtInVisualPresets: List<VisualPreset> = listOf(
        VisualPreset("builtin-dark", "Escuro padrão", "system", "#8B5CF6"),
        VisualPreset("builtin-midnight", "Meia-noite", "dark", "#3B82F6"),
        VisualPreset("builtin-graphite", "Grafite", "dark", "#94A3B8"),
        VisualPreset("builtin-amber", "Âmbar", "dark", "#F59E0B"),
        VisualPreset("builtin-emerald", "Esmeralda", "dark", "#10B981")
    )

    // ---- Flag presets ----
    suspend fun listFlagPresets(): List<FlagPreset> {
        val saved = loadFlagPresets()
        return builtInFlagPresets.filterNot { it.id in saved.map { p -> p.id } } + saved
    }

    suspend fun saveFlagPreset(preset: FlagPreset): Boolean {
        val file = presetFile(preset.id)
        val text = JsonUtils.json.encodeToString(FlagPreset.serializer(), preset)
        return JsonUtils.writeFile(file, text)
    }

    suspend fun deleteFlagPreset(id: String): Boolean = runCatching {
        presetFile(id).delete()
    }.getOrDefault(false)

    private suspend fun loadFlagPresets(): List<FlagPreset> {
        return AppPaths.presetsDir.listFiles { f -> f.isFile && f.name.endsWith(".json") }
            ?.mapNotNull { file ->
                runCatching {
                    JsonUtils.json.decodeFromString<FlagPreset>(
                        FlagPreset.serializer(),
                        file.readText()
                    )
                }.onFailure { Logger.writeException("PresetRepository::load", it) }
                    .getOrNull()
            }
            ?: emptyList()
    }

    // ---- Visual presets ----
    suspend fun listVisualPresets(): List<VisualPreset> {
        val saved = loadVisualPresets()
        return builtInVisualPresets.filterNot { it.id in saved.map { p -> p.id } } + saved
    }

    suspend fun saveVisualPreset(preset: VisualPreset): Boolean {
        val file = visualPresetFile(preset.id)
        val text = JsonUtils.json.encodeToString(VisualPreset.serializer(), preset)
        return JsonUtils.writeFile(file, text)
    }

    suspend fun deleteVisualPreset(id: String): Boolean = runCatching {
        visualPresetFile(id).delete()
    }.getOrDefault(false)

    private suspend fun loadVisualPresets(): List<VisualPreset> {
        return AppPaths.presetsDir.listFiles { f -> f.isFile && f.name.startsWith("visual-") && f.name.endsWith(".json") }
            ?.mapNotNull { file ->
                runCatching {
                    JsonUtils.json.decodeFromString<VisualPreset>(
                        VisualPreset.serializer(),
                        file.readText()
                    )
                }.onFailure { Logger.writeException("PresetRepository::loadVisual", it) }
                    .getOrNull()
            }
            ?: emptyList()
    }

    private fun presetFile(id: String): File =
        File(AppPaths.presetsDir, "flag-${sanitize(id)}.json")

    private fun visualPresetFile(id: String): File =
        File(AppPaths.presetsDir, "visual-${sanitize(id)}.json")

    private fun sanitize(id: String): String =
        id.trim().replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "preset" }
}
