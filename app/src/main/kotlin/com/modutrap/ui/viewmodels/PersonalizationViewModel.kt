package com.modutrap.ui.viewmodels

import android.app.Application
import com.modutrap.models.VisualPreset
import com.modutrap.personalization.AccentPreset
import com.modutrap.personalization.BackupFile
import com.modutrap.personalization.ThemeManager
import com.modutrap.settings.AppSettings
import com.modutrap.ui.BaseViewModel
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/** Drives the Personalization screen: theme, accent, visual presets, files, backups. */
class PersonalizationViewModel(app: Application) : BaseViewModel(app) {

    val settings: StateFlow<AppSettings> = container.settings.settings

    val accentPresets: List<AccentPreset> = ThemeManager.accentPresets

    private val _visualPresets = MutableStateFlow<List<VisualPreset>>(emptyList())
    val visualPresets: StateFlow<List<VisualPreset>> = _visualPresets.asStateFlow()

    private val _files = MutableStateFlow<List<File>>(emptyList())
    val files: StateFlow<List<File>> = _files.asStateFlow()

    private val _backups = MutableStateFlow<List<BackupFile>>(emptyList())
    val backups: StateFlow<List<BackupFile>> = _backups.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _visualPresets.value = container.personalization.listVisualPresets()
            _files.value = container.personalization.listCustomFiles()
            _backups.value = container.backups.listBackups()
        }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch {
            container.settings.put(com.modutrap.settings.SettingsRepository.Keys.themeMode, mode)
        }
    }

    fun setAccent(hex: String) {
        viewModelScope.launch {
            if (ThemeManager.isValidHex(hex)) {
                container.settings.put(com.modutrap.settings.SettingsRepository.Keys.accentColor, hex)
            } else {
                _message.value = "Cor de acento inválida."
            }
        }
    }

    fun saveVisualPreset(name: String) {
        val current = container.settings.settings.value
        val preset = VisualPreset(
            id = "visual-${System.currentTimeMillis()}",
            name = name,
            themeMode = current.themeMode,
            accentColor = current.accentColor
        )
        viewModelScope.launch {
            val ok = container.personalization.saveVisualPreset(preset)
            _message.value = if (ok) "Preset visual salvo: $name" else "Falha ao salvar o preset."
            refresh()
        }
    }

    fun applyVisualPreset(preset: VisualPreset) {
        viewModelScope.launch {
            container.settings.put(com.modutrap.settings.SettingsRepository.Keys.themeMode, preset.themeMode)
            container.settings.put(com.modutrap.settings.SettingsRepository.Keys.accentColor, preset.accentColor)
            _message.value = "Preset aplicado: ${preset.name}"
        }
    }

    fun deleteVisualPreset(id: String) {
        viewModelScope.launch {
            container.personalization.deleteVisualPreset(id)
            _message.value = "Preset visual excluído."
            refresh()
        }
    }

    fun saveCustomFile(name: String, content: String) {
        viewModelScope.launch {
            val ok = container.personalization.saveCustomFile(name, content)
            _message.value = if (ok) "Arquivo salvo: $name" else "Falha ao salvar o arquivo."
            refresh()
        }
    }

    fun deleteCustomFile(name: String) {
        viewModelScope.launch {
            container.personalization.deleteCustomFile(name)
            _message.value = "Arquivo excluído: $name"
            refresh()
        }
    }

    fun readCustomFile(name: String): String? = container.personalization.readCustomFile(name)

    fun createBackup(name: String) {
        viewModelScope.launch {
            _busy.value = true
            val file = container.backups.createBackup(name)
            _message.value = if (file != null) "Backup criado: ${file.name}" else "Falha ao criar o backup."
            refresh()
            _busy.value = false
        }
    }

    fun restoreBackup(backup: BackupFile) {
        viewModelScope.launch {
            _busy.value = true
            val ok = container.backups.restore(backup)
            _message.value = if (ok) "Backup restaurado: ${backup.name}" else "Falha ao restaurar o backup."
            refresh()
            _busy.value = false
        }
    }

    fun deleteBackup(backup: BackupFile) {
        viewModelScope.launch {
            container.backups.delete(backup)
            _message.value = "Backup excluído."
            refresh()
        }
    }

    fun dismissMessage() {
        _message.value = null
    }
}
