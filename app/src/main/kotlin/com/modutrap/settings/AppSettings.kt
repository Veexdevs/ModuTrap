package com.modutrap.settings

import androidx.datastore.preferences.core.Preferences
import kotlinx.serialization.Serializable

/**
 * All persisted user settings, flattened into one immutable snapshot.
 *
 * The Graphics and Performance groups are not applied directly by the engine —
 * they are translated into Fast Flags at launch time (see
 * [com.modutrap.services.FastFlagApplier]). This keeps the UI honest: every
 * toggle maps to a real, applied flag or a documented capability.
 */
@Serializable
data class AppSettings(
    // Graphics
    val graphicsQuality: Int = 0,
    val renderDistance: Int = 6,
    val msaaMode: Int = 0,
    val shadowsEnabled: Boolean = true,
    val postFxEnabled: Boolean = true,
    val terrainTextures: Boolean = true,
    val dynamicResolution: Boolean = false,

    // Performance
    val fpsCap: Int = 0,
    val enableGCOptimization: Boolean = false,
    val reduceMotion: Boolean = false,
    val clearCacheOnLaunch: Boolean = false,
    val limitBackgroundProcesses: Boolean = false,

    // Launch
    val preferredPackage: String = "global",
    val customPackage: String = "",
    val applyFlagsOnLaunch: Boolean = true,
    val applyOptimizationsOnLaunch: Boolean = true,
    val lastLaunchTs: String = "",

    // Personalization
    val themeMode: String = "system",
    val accentColor: String = "#8B5CF6",

    // Integrations
    val shizukuAutoRequest: Boolean = false,

    // In-game overlay
    val inGameOverlayEnabled: Boolean = false
) {
    companion object {
        val defaults = AppSettings()

        fun fromPrefs(p: Preferences): AppSettings = AppSettings(
            graphicsQuality = p[SettingsRepository.Keys.graphicsQuality] ?: 0,
            renderDistance = p[SettingsRepository.Keys.renderDistance] ?: 6,
            msaaMode = p[SettingsRepository.Keys.msaaMode] ?: 0,
            shadowsEnabled = p[SettingsRepository.Keys.shadowsEnabled] ?: true,
            postFxEnabled = p[SettingsRepository.Keys.postFxEnabled] ?: true,
            terrainTextures = p[SettingsRepository.Keys.terrainTextures] ?: true,
            dynamicResolution = p[SettingsRepository.Keys.dynamicResolution] ?: false,
            fpsCap = p[SettingsRepository.Keys.fpsCap] ?: 0,
            enableGCOptimization = p[SettingsRepository.Keys.enableGCOptimization] ?: false,
            reduceMotion = p[SettingsRepository.Keys.reduceMotion] ?: false,
            clearCacheOnLaunch = p[SettingsRepository.Keys.clearCacheOnLaunch] ?: false,
            limitBackgroundProcesses = p[SettingsRepository.Keys.limitBackgroundProcesses] ?: false,
            preferredPackage = p[SettingsRepository.Keys.preferredPackage] ?: "global",
            customPackage = p[SettingsRepository.Keys.customPackage] ?: "",
            applyFlagsOnLaunch = p[SettingsRepository.Keys.applyFlagsOnLaunch] ?: true,
            applyOptimizationsOnLaunch = p[SettingsRepository.Keys.applyOptimizationsOnLaunch] ?: true,
            lastLaunchTs = p[SettingsRepository.Keys.lastLaunchTs] ?: "",
            themeMode = p[SettingsRepository.Keys.themeMode] ?: "system",
            accentColor = p[SettingsRepository.Keys.accentColor] ?: "#8B5CF6",
            shizukuAutoRequest = p[SettingsRepository.Keys.shizukuAutoRequest] ?: false,
            inGameOverlayEnabled = p[SettingsRepository.Keys.inGameOverlayEnabled] ?: false
        )
    }
}
