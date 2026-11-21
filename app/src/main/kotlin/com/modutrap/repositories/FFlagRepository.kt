package com.modutrap.repositories

import com.modutrap.core.AppPaths
import com.modutrap.core.Logger
import com.modutrap.flags.FastFlagValidator
import com.modutrap.models.FFlagEntry
import com.modutrap.utils.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/** Result of importing a JSON document into the flag store. */
data class ImportResult(val imported: Int, val total: Int)

/**
 * In-memory + on-disk store for the Fast Flags we want Roblox to use.
 *
 * The on-disk representation mirrors Bloxstrap / Roblox's expected schema:
 * ```
 * { "FFlags": { "FFlagKey": "value", ... } }
 * ```
 * All values are strings. Boolean values are normalized to Roblox's canonical
 * `True`/`False` on write (see [FastFlagValidator]).
 */
class FFlagRepository {

    private val _flags = MutableStateFlow<List<FFlagEntry>>(emptyList())
    val flags: StateFlow<List<FFlagEntry>> = _flags.asStateFlow()

    suspend fun load(): List<FFlagEntry> = withContext(Dispatchers.IO) {
        val file = AppPaths.clientAppSettingsFile
        val result: List<FFlagEntry> = try {
            val raw = JsonUtils.readFile(file) ?: return@withContext emptyList()
            JsonUtils.parseFlags(raw)?.map { FFlagEntry(it.key, it.value) }
                ?.sortedBy { it.key } ?: emptyList()
        } catch (e: Exception) {
            Logger.writeException("FFlagRepository::load", e)
            emptyList()
        }
        _flags.value = result
        result
    }

    suspend fun save(entries: List<FFlagEntry>) = withContext(Dispatchers.IO) {
        val text = JsonUtils.encodeFlags(entries.associate { it.key to it.value })
        val ok = JsonUtils.writeFile(AppPaths.clientAppSettingsFile, text)
        _flags.value = entries.sortedBy { it.key }
        if (ok) {
            Logger.writeLine("FFlagRepository::save", "Saved ${entries.size} flags.")
        } else {
            Logger.writeLine("FFlagRepository::save", "Failed to write flags file.")
        }
    }

    /**
     * Insert or update a single flag. Values are validated/normalized; if the
     * flag is not in the verified catalog the raw value is kept (the UI warns
     * about unverified flags).
     */
    suspend fun upsert(key: String, rawValue: String): FlagValidationOutcome {
        val keyTrimmed = key.trim()
        if (keyTrimmed.isBlank()) return FlagValidationOutcome(false, "Nome da flag vazio.")
        val validation = FastFlagValidator.validate(keyTrimmed, rawValue)
        if (!validation.valid) return FlagValidationOutcome(false, validation.message)

        val value = validation.normalizedValue ?: rawValue.trim()
        val current = _flags.value.toMutableList()
        val idx = current.indexOfFirst { it.key.equals(keyTrimmed, ignoreCase = true) }
        if (idx >= 0) {
            current[idx] = FFlagEntry(keyTrimmed, value)
        } else {
            current.add(FFlagEntry(keyTrimmed, value))
        }
        save(current)
        return FlagValidationOutcome(
            true,
            null,
            verified = FastFlagValidator.isVerified(keyTrimmed)
        )
    }

    suspend fun delete(key: String) {
        val current = _flags.value.filterNot { it.key.equals(key.trim(), ignoreCase = true) }
        save(current)
    }

    suspend fun clear() = save(emptyList())

    /**
     * Import a JSON document. When [replace] is true the imported flags
     * overwrite existing keys (merge); when false new keys are added and
     * existing keys are left untouched.
     */
    suspend fun importJson(raw: String, replace: Boolean): ImportResult = withContext(Dispatchers.IO) {
        val parsed = try {
            JsonUtils.parseFlags(raw)
        } catch (e: Exception) {
            Logger.writeException("FFlagRepository::import", e)
            null
        } ?: return@withContext ImportResult(0, _flags.value.size)

        val imported = parsed.map { FFlagEntry(it.key, it.value) }
        val merged: List<FFlagEntry> = if (replace) {
            val byKey = _flags.value.associateBy { it.key.lowercase() }.toMutableMap()
            imported.forEach { byKey[it.key.lowercase()] = it }
            byKey.values.toList()
        } else {
            val existing = _flags.value.associateBy { it.key.lowercase() }.toMutableMap()
            imported.forEach { existing[it.key.lowercase()] = it }
            existing.values.toList()
        }
        save(merged)
        ImportResult(imported.size, merged.size)
    }

    fun exportJson(entries: List<FFlagEntry> = _flags.value): String =
        JsonUtils.encodeFlags(entries.associate { it.key to it.value })

    /** Export to a file and return its absolute path (or null on failure). */
    suspend fun exportToFile(file: File): String? = withContext(Dispatchers.IO) {
        val text = exportJson()
        if (JsonUtils.writeFile(file, text)) file.absolutePath else null
    }

    suspend fun backup(name: String): Boolean = withContext(Dispatchers.IO) {
        val src = AppPaths.clientAppSettingsFile
        if (!src.exists()) return@withContext false
        val dst = File(AppPaths.backupsDir, sanitizeName(name) + ".json")
        val ok = runCatching { src.copyTo(dst, overwrite = true); dst.exists() }.getOrDefault(false)
        Logger.writeLine("FFlagRepository::backup", "Backup: ${dst.absolutePath}")
        ok
    }

    suspend fun restore(name: String): Boolean = withContext(Dispatchers.IO) {
        val src = File(AppPaths.backupsDir, sanitizeName(name) + ".json")
        if (!src.exists()) return@withContext false
        val ok = runCatching {
            src.copyTo(AppPaths.clientAppSettingsFile, overwrite = true)
            AppPaths.clientAppSettingsFile.exists()
        }.getOrDefault(false)
        if (ok) load()
        ok
    }

    fun listBackups(): List<String> =
        AppPaths.backupsDir.listFiles { f -> f.isFile && f.name.endsWith(".json") }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()

    fun deleteBackup(name: String): Boolean = runCatching {
        File(AppPaths.backupsDir, sanitizeName(name) + ".json").delete()
    }.getOrDefault(false)

    fun count(): Int = _flags.value.size

    private fun sanitizeName(name: String): String =
        name.trim().replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "backup" }
}

/** Result of a single [FFlagRepository.upsert]. */
data class FlagValidationOutcome(
    val success: Boolean,
    val message: String?,
    val verified: Boolean = true
)
