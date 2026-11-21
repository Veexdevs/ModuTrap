package com.modutrap.personalization

import com.modutrap.core.AppPaths
import com.modutrap.core.Logger
import com.modutrap.repositories.FFlagRepository
import com.modutrap.settings.AppSettings
import com.modutrap.settings.SettingsRepository
import com.modutrap.utils.FileUtils
import com.modutrap.utils.JsonUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import java.io.File

/** A backup file on disk. */
data class BackupFile(val name: String, val file: File, val createdAt: String)

@Serializable
private data class BackupDocument(
    val version: Int = 1,
    val createdAt: String,
    val settings: AppSettings,
    val flags: Map<String, String>
)

/**
 * Full-config backup/restore.
 *
 * A backup is a single JSON document capturing the complete [AppSettings]
 * snapshot and the entire Fast Flag map. Backups live in [AppPaths.backupsDir]
 * alongside the Fast Flag backups. Restoring writes settings back through
 * [SettingsRepository] and flags back through [FFlagRepository].
 */
class BackupManager(
    private val settings: SettingsRepository,
    private val flags: FFlagRepository
) {

    suspend fun createBackup(name: String): BackupFile? {
        val current = settings.settings.value
        val flagMap = flags.flags.value.associate { it.key to it.value }
        val doc = BackupDocument(
            version = 1,
            createdAt = FileUtils.formatDate(System.currentTimeMillis()),
            settings = current,
            flags = flagMap
        )
        val text = JsonUtils.json.encodeToString(BackupDocument.serializer(), doc)
        val safeName = sanitize(name)
        val file = File(AppPaths.backupsDir, "modutrap-$safeName.json")
        if (!JsonUtils.writeFile(file, text)) {
            Logger.writeLine("BackupManager::create", "Failed to write backup.")
            return null
        }
        Logger.writeLine("BackupManager::create", "Backup written: ${file.absolutePath}")
        return BackupFile(safeName, file, doc.createdAt)
    }

    fun listBackups(): List<BackupFile> =
        AppPaths.backupsDir.listFiles { f -> f.isFile && f.name.startsWith("modutrap-") && f.name.endsWith(".json") }
            ?.sortedBy { it.name }
            ?.map { file ->
                BackupFile(
                    name = file.nameWithoutExtension.removePrefix("modutrap-"),
                    file = file,
                    createdAt = file.name
                )
            }
            ?: emptyList()

    /**
     * Restore a backup. Writes settings and flags back. Returns true on success.
     * Does not clear unrelated flags — restore merges flags from the backup.
     */
    suspend fun restore(backup: BackupFile): Boolean {
        return runCatching {
            val text = backup.file.readText()
            val doc = JsonUtils.json.decodeFromString<BackupDocument>(
                BackupDocument.serializer(),
                text
            )

            val s = doc.settings
            // Re-apply every settings key so the live StateFlow updates.
            settings.put(SettingsRepository.Keys.graphicsQuality, s.graphicsQuality)
            settings.put(SettingsRepository.Keys.renderDistance, s.renderDistance)
            settings.put(SettingsRepository.Keys.msaaMode, s.msaaMode)
            settings.put(SettingsRepository.Keys.shadowsEnabled, s.shadowsEnabled)
            settings.put(SettingsRepository.Keys.postFxEnabled, s.postFxEnabled)
            settings.put(SettingsRepository.Keys.terrainTextures, s.terrainTextures)
            settings.put(SettingsRepository.Keys.dynamicResolution, s.dynamicResolution)
            settings.put(SettingsRepository.Keys.fpsCap, s.fpsCap)
            settings.put(SettingsRepository.Keys.enableGCOptimization, s.enableGCOptimization)
            settings.put(SettingsRepository.Keys.reduceMotion, s.reduceMotion)
            settings.put(SettingsRepository.Keys.clearCacheOnLaunch, s.clearCacheOnLaunch)
            settings.put(SettingsRepository.Keys.limitBackgroundProcesses, s.limitBackgroundProcesses)
            settings.put(SettingsRepository.Keys.preferredPackage, s.preferredPackage)
            settings.put(SettingsRepository.Keys.customPackage, s.customPackage)
            settings.put(SettingsRepository.Keys.applyFlagsOnLaunch, s.applyFlagsOnLaunch)
            settings.put(SettingsRepository.Keys.applyOptimizationsOnLaunch, s.applyOptimizationsOnLaunch)
            settings.put(SettingsRepository.Keys.themeMode, s.themeMode)
            settings.put(SettingsRepository.Keys.accentColor, s.accentColor)
            settings.put(SettingsRepository.Keys.shizukuAutoRequest, s.shizukuAutoRequest)

            flags.importJson(JsonUtils.encodeFlags(doc.flags), replace = true)
            Logger.writeLine("BackupManager::restore", "Restored backup '${backup.name}'.")
            true
        }.onFailure {
            Logger.writeException("BackupManager::restore", it)
        }.getOrDefault(false)
    }

    fun delete(backup: BackupFile): Boolean = runCatching {
        backup.file.delete()
    }.getOrDefault(false)

    private fun sanitize(name: String): String =
        name.trim().replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "backup" }
}
