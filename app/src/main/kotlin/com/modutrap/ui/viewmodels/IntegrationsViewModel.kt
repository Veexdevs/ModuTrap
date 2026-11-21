package com.modutrap.ui.viewmodels

import android.app.Application
import com.modutrap.models.DiagnosticReport
import com.modutrap.models.ShizukuStatus
import com.modutrap.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/** Drives the Integrações screen: Shizuku status and diagnostics. */
class IntegrationsViewModel(app: Application) : BaseViewModel(app) {

    private val _status = MutableStateFlow(ShizukuStatus.NOT_INSTALLED)
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val _report = MutableStateFlow<DiagnosticReport?>(null)
    val report: StateFlow<DiagnosticReport?> = _report.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _status.value = container.shizuku.status(app)
    }

    fun requestPermission() {
        val granted = container.integration.requestPermission()
        _message.value = if (granted) "Shizuku já está autorizado." else "Solicitação enviada. Abra o Shizuku e autorize o app."
        refresh()
    }

    fun generateReport() {
        viewModelScope.launch {
            _report.value = container.diagnostics.build()
            _message.value = "Relatório de diagnóstico gerado."
        }
    }

    fun reportText(): String? = _report.value?.toText()

    fun dismissReport() {
        _report.value = null
    }

    fun dismissMessage() {
        _message.value = null
    }
}
