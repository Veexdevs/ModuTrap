package com.modutrap.ui.viewmodels

import android.app.Application
import com.modutrap.settings.AppSettings
import com.modutrap.settings.SettingsRepository
import com.modutrap.ui.BaseViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Exposes the live [AppSettings] snapshot and a single [update] entry point.
 *
 * [update] reads the current snapshot, applies the given transform, and writes
 * every key back through [SettingsRepository]. This keeps the UI declarative:
 * screens call `update { it.copy(shadowsEnabled = false) }` and the flow
 * reflects the change automatically.
 */
class SettingsViewModel(app: Application) : BaseViewModel(app) {

    private val repo: SettingsRepository get() = container.settings

    val settings: StateFlow<AppSettings> = repo.settings

    /** Apply [block] to the current snapshot and persist the result. */
    fun update(block: AppSettings.() -> AppSettings) {
        viewModelScope.launch {
            val current = repo.settings.value
            val updated = current.block()
            repo.set(updated)
        }
    }

    /** Persist a full [AppSettings] snapshot (used by backup restore). */
    fun save(settings: AppSettings) {
        viewModelScope.launch { repo.set(settings) }
    }
}
