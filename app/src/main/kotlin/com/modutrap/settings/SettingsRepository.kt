package com.modutrap.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.modutrap.core.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Typed access to the app's persisted settings.
 *
 * Exposes a single [settings] StateFlow snapshot plus a generic [put] for
 * ViewModels to write individual keys. All writes are atomic (DataStore `edit`).
 */
class SettingsRepository(private val store: DataStore<Preferences>) {

    /** DataStore preference keys, shared with ViewModels for typed writes. */
    object Keys {
        val graphicsQuality = intPreferencesKey("graphics_quality")
        val renderDistance = intPreferencesKey("render_distance")
        val msaaMode = intPreferencesKey("msaa_mode")
        val shadowsEnabled = booleanPreferencesKey("shadows_enabled")
        val postFxEnabled = booleanPreferencesKey("postfx_enabled")
        val terrainTextures = booleanPreferencesKey("terrain_textures")
        val dynamicResolution = booleanPreferencesKey("dynamic_resolution")
        val fpsCap = intPreferencesKey("fps_cap")
        val enableGCOptimization = booleanPreferencesKey("enable_gc_optimization")
        val reduceMotion = booleanPreferencesKey("reduce_motion")
        val clearCacheOnLaunch = booleanPreferencesKey("clear_cache_on_launch")
        val limitBackgroundProcesses = booleanPreferencesKey("limit_background_processes")
        val preferredPackage = stringPreferencesKey("preferred_package")
        val customPackage = stringPreferencesKey("custom_package")
        val applyFlagsOnLaunch = booleanPreferencesKey("apply_flags_on_launch")
        val applyOptimizationsOnLaunch = booleanPreferencesKey("apply_optimizations_on_launch")
        val lastLaunchTs = stringPreferencesKey("last_launch_ts")
        val themeMode = stringPreferencesKey("theme_mode")
        val accentColor = stringPreferencesKey("accent_color")
        val shizukuAutoRequest = booleanPreferencesKey("shizuku_auto_request")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settings: StateFlow<AppSettings> = store.data
        .map { AppSettings.fromPrefs(it) }
        .stateIn(scope, SharingStarted.Eagerly, AppSettings.defaults)

    /** Write a single key. Returns true on success. */
    suspend fun <T> put(key: Preferences.Key<T>, value: T): Boolean = runCatching {
        store.edit { it[key] = value }
    }.onFailure { Logger.writeException("SettingsRepository::put", it) }.isSuccess

    /** Write the launch timestamp. */
    suspend fun setLastLaunchTs(timestamp: String) {
        put(Keys.lastLaunchTs, timestamp)
    }

    /** Persist a full [AppSettings] snapshot, writing every key atomically. */
    suspend fun set(settings: AppSettings) {
        store.edit { p ->
            p[Keys.graphicsQuality] = settings.graphicsQuality
            p[Keys.renderDistance] = settings.renderDistance
            p[Keys.msaaMode] = settings.msaaMode
            p[Keys.shadowsEnabled] = settings.shadowsEnabled
            p[Keys.postFxEnabled] = settings.postFxEnabled
            p[Keys.terrainTextures] = settings.terrainTextures
            p[Keys.dynamicResolution] = settings.dynamicResolution
            p[Keys.fpsCap] = settings.fpsCap
            p[Keys.enableGCOptimization] = settings.enableGCOptimization
            p[Keys.reduceMotion] = settings.reduceMotion
            p[Keys.clearCacheOnLaunch] = settings.clearCacheOnLaunch
            p[Keys.limitBackgroundProcesses] = settings.limitBackgroundProcesses
            p[Keys.preferredPackage] = settings.preferredPackage
            p[Keys.customPackage] = settings.customPackage
            p[Keys.applyFlagsOnLaunch] = settings.applyFlagsOnLaunch
            p[Keys.applyOptimizationsOnLaunch] = settings.applyOptimizationsOnLaunch
            p[Keys.lastLaunchTs] = settings.lastLaunchTs
            p[Keys.themeMode] = settings.themeMode
            p[Keys.accentColor] = settings.accentColor
            p[Keys.shizukuAutoRequest] = settings.shizukuAutoRequest
        }
    }
}
