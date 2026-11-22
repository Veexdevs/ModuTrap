package com.modutrap.ui.screens.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.modutrap.models.LaunchResult
import com.modutrap.models.RobloxStatus
import com.modutrap.models.ShizukuStatus
import com.modutrap.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Drives the Home screen: Roblox + Shizuku status and the launch action. */
class HomeViewModel(app: Application) : BaseViewModel(app) {

    private val _roblox = MutableStateFlow(RobloxStatus(
        installed = false,
        packageName = com.modutrap.roblox.RobloxManager.PACKAGE_GLOBAL,
        versionName = null,
        dataDir = null,
        isDebugBuild = false,
        hasLaunchIntent = false,
        packageVisible = false
    ))
    val roblox: StateFlow<RobloxStatus> = _roblox.asStateFlow()

    private val _shizuku = MutableStateFlow(ShizukuStatus.NOT_INSTALLED)
    val shizuku: StateFlow<ShizukuStatus> = _shizuku.asStateFlow()

    private val _launching = MutableStateFlow(false)
    val launching: StateFlow<Boolean> = _launching.asStateFlow()

    private val _result = MutableStateFlow<LaunchResult?>(null)
    val result: StateFlow<LaunchResult?> = _result.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        // Recompute Roblox status whenever the chosen package changes.
        viewModelScope.launch {
            container.settings.settings.collect { refresh() }
        }
        refresh()
    }

    fun refresh() {
        val s = container.settings.settings.value
        _roblox.value = container.roblox.status(s.preferredPackage, s.customPackage)
        _shizuku.value = container.shizuku.status(app)
    }

    /** Apply [block] to the current settings snapshot and persist the result. */
    fun update(block: com.modutrap.settings.AppSettings.() -> com.modutrap.settings.AppSettings) {
        viewModelScope.launch {
            val current = container.settings.settings.value
            container.settings.set(current.block())
        }
    }

    fun launch() {
        if (_launching.value) return
        viewModelScope.launch {
            _launching.value = true
            try {
                val result = container.launcher.launch()
                _result.value = result
                // If the in-game overlay is enabled, start it right after Roblox
                // launches so the floating menu appears on top of the game.
                if (result is LaunchResult.Success && container.settings.settings.value.inGameOverlayEnabled) {
                    com.modutrap.services.InGameOverlayService.start(app)
                }
            } catch (e: Exception) {
                _result.value = LaunchResult.Failure("Erro ao iniciar: ${e.message}", e)
            } finally {
                _launching.value = false
            }
        }
    }

    fun startOverlay() {
        com.modutrap.services.InGameOverlayService.start(app)
    }

    fun stopOverlay() {
        com.modutrap.services.InGameOverlayService.stop(app)
    }

    fun isOverlayRunning(): Boolean =
        com.modutrap.services.InGameOverlayService.isRunning(app)

    fun openInstallPage() {
        val ok = container.roblox.openInstallPage()
        _message.value = if (ok) "Abrindo página de instalação do Roblox…" else "Não foi possível abrir a página."
    }

    fun dismissMessage() {
        _message.value = null
    }

    fun dismissResult() {
        _result.value = null
    }
}
