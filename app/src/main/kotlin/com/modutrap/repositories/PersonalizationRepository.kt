package com.modutrap.repositories

import com.modutrap.core.AppPaths
import com.modutrap.core.Logger
import com.modutrap.models.VisualPreset
import com.modutrap.utils.FileUtils
import com.modutrap.utils.JsonUtils
import java.io.File

/**
 * Orchestrates personalization concerns: custom files owned by the app, and
 * visual presets (delegated to [PresetRepository]).
 *
 * Custom files live under [AppPaths.customFilesDir]; themes under
 * [AppPaths.themesDir]. Nothing here touches Roblox's protected files.
 */
class PersonalizationRepository(
    private val presets: PresetRepository = PresetRepository()
) {

    fun listCustomFiles(): List<File> = FileUtils.listFiles(AppPaths.customFilesDir)

    fun listThemeFiles(): List<File> = FileUtils.listFiles(AppPaths.themesDir)

    /** Save (create or overwrite) a custom file. Returns true on success. */
    suspend fun saveCustomFile(name: String, content: String): Boolean {
        if (!FileUtils.isValidFileName(name)) return false
        val file = File(AppPaths.customFilesDir, name)
        return JsonUtils.writeFile(file, content)
    }

    fun readCustomFile(name: String): String? =
        JsonUtils.readFile(File(AppPaths.customFilesDir, name))

    fun deleteCustomFile(name: String): Boolean = runCatching {
        File(AppPaths.customFilesDir, name).delete()
    }.getOrDefault(false)

    fun saveThemeFile(name: String, content: String): Boolean {
        if (!FileUtils.isValidFileName(name)) return false
        return JsonUtils.writeFile(File(AppPaths.themesDir, name), content)
    }

    fun deleteThemeFile(name: String): Boolean = runCatching {
        File(AppPaths.themesDir, name).delete()
    }.getOrDefault(false)

    suspend fun listVisualPresets() = presets.listVisualPresets()

    suspend fun saveVisualPreset(preset: VisualPreset): Boolean {
        val ok = presets.saveVisualPreset(preset)
        if (!ok) Logger.writeLine("PersonalizationRepository", "Failed to save visual preset.")
        return ok
    }

    suspend fun deleteVisualPreset(id: String): Boolean = presets.deleteVisualPreset(id)
}
