package com.modutrap.ui.viewmodels

import android.app.Application
import com.modutrap.models.FFlagEntry
import com.modutrap.models.FlagPreset
import com.modutrap.repositories.FlagValidationOutcome
import com.modutrap.repositories.ImportResult
import com.modutrap.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the Fast Flags screen: the live flag list, search, presets, backups
 * and import/export. Every mutation goes through [com.modutrap.repositories.FFlagRepository].
 */
class FlagsViewModel(app: Application) : BaseViewModel(app) {

    private val repo get() = container.flags
    private val presets get() = container.presets

    private val _flags = MutableStateFlow<List<FFlagEntry>>(emptyList())
    val flags: StateFlow<List<FFlagEntry>> = _flags.asStateFlow()

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()

    private val _presets = MutableStateFlow<List<FlagPreset>>(emptyList())
    val presets: StateFlow<List<FlagPreset>> = _presets.asStateFlow()

    private val _backups = MutableStateFlow<List<String>>(emptyList())
    val backups: StateFlow<List<String>> = _backups.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    init {
        load()
    }

    fun load() {
        launch {
            repo.load()
            _flags.value = repo.flags.value
            _presets.value = presets.listFlagPresets()
            _backups.value = repo.listBackups()
        }
    }

    fun setSearch(value: String) {
        _search.value = value
    }

    /** The flags shown in the list, filtered by the search term. */
    fun visibleFlags(): List<FFlagEntry> {
        val q = _search.value.trim().lowercase()
        return if (q.isEmpty()) _flags.value
        else _flags.value.filter { it.key.lowercase().contains(q) || it.value.lowercase().contains(q) }
    }

    fun addOrUpdate(key: String, value: String) {
        launch {
            val outcome = repo.upsert(key, value)
            _flags.value = repo.flags.value
            if (outcome.success) {
                _message.value = if (outcome.verified) "Flag adicionada: $key" else "Flag adicionada (não verificada): $key"
            } else {
                _message.value = outcome.message ?: "Erro ao salvar a flag."
            }
            _backups.value = repo.listBackups()
        }
    }

    fun delete(key: String) {
        launch {
            repo.delete(key)
            _flags.value = repo.flags.value
            _backups.value = repo.listBackups()
            _message.value = "Flag excluída: $key"
        }
    }

    fun clearAll() {
        launch {
            repo.clear()
            _flags.value = repo.flags.value
            _message.value = "Todas as flags foram removidas."
        }
    }

    fun importJson(raw: String, replace: Boolean) {
        launch {
            _busy.value = true
            val result: ImportResult = repo.importJson(raw, replace)
            _flags.value = repo.flags.value
            _backups.value = repo.listBackups()
            _message.value = "${result.imported} flag(s) importada(s)."
            _busy.value = false
        }
    }

    fun exportJson(): String {
        return repo.exportJson()
    }

    fun applyPreset(preset: FlagPreset) {
        launch {
            repo.clear()
            val merged = preset.flags.map { FFlagEntry(it.key, it.value) }
            repo.save(merged)
            _flags.value = repo.flags.value
            _message.value = "Preset aplicado: ${preset.name} (${merged.size} flags)."
        }
    }

    fun savePreset(id: String, name: String, description: String) {
        val preset = FlagPreset(id, name, description, _flags.value.associate { it.key to it.value })
        launch {
            presets.saveFlagPreset(preset)
            _presets.value = presets.listFlagPresets()
            _message.value = "Preset salvo: $name"
        }
    }

    fun deletePreset(id: String) {
        launch {
            presets.deleteFlagPreset(id)
            _presets.value = presets.listFlagPresets()
            _message.value = "Preset excluído."
        }
    }

    fun createBackup(name: String) {
        launch {
            val ok = repo.backup(name)
            _backups.value = repo.listBackups()
            _message.value = if (ok) "Backup criado: $name" else "Falha ao criar o backup."
        }
    }

    fun restoreBackup(name: String) {
        launch {
            val ok = repo.restore(name)
            _flags.value = repo.flags.value
            _backups.value = repo.listBackups()
            _message.value = if (ok) "Backup restaurado: $name" else "Falha ao restaurar o backup."
        }
    }

    fun deleteBackup(name: String) {
        launch {
            repo.deleteBackup(name)
            _backups.value = repo.listBackups()
            _message.value = "Backup excluído."
        }
    }

    fun dismissMessage() {
        _message.value = null
    }
}
