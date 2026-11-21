package com.modutrap.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modutrap.core.AppContainer
import com.modutrap.ui.components.OptionRow
import com.modutrap.ui.components.ScreenScaffold
import com.modutrap.ui.components.SectionCard
import com.modutrap.ui.components.SettingSwitch
import com.modutrap.ui.viewmodels.SettingsViewModel

private val fpsOptions = listOf("Sem limite", "60", "90", "120", "144", "240")
private val fpsValues = listOf(0, 60, 90, 120, 144, 240)

/** PERFORMANCE: FPS cap and the performance-oriented toggles. */
@Composable
fun PerformanceScreen(container: AppContainer, onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel()
    val settings by vm.settings.collectAsStateWithLifecycle()

    ScreenScaffold(title = "Desempenho", onBack = onBack) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionCard(title = "Taxa de quadros") {
                Text("Limite de FPS", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                OptionRow(
                    options = fpsOptions,
                    selected = fpsOptions.firstOrNull { fpsValues[fpsOptions.indexOf(it)] == settings.fpsCap }
                        ?: fpsOptions[0],
                    onSelect = { label ->
                        vm.update { copy(fpsCap = fpsValues[fpsOptions.indexOf(label)]) }
                    }
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionCard(title = "Otimizações") {
                SettingSwitch(
                    label = "Otimização de GC (Luau)",
                    checked = settings.enableGCOptimization,
                    onChange = { vm.update { copy(enableGCOptimization = it) } },
                    description = "Ajusta flags de coleta de lixo do Luau para reduzir pausas."
                )
                Spacer(Modifier.height(8.dp))
                SettingSwitch(
                    label = "Reduzir animações",
                    checked = settings.reduceMotion,
                    onChange = { vm.update { copy(reduceMotion = it) } }
                )
                Spacer(Modifier.height(8.dp))
                SettingSwitch(
                    label = "Limpar cache do Roblox na inicialização",
                    checked = settings.clearCacheOnLaunch,
                    onChange = { vm.update { copy(clearCacheOnLaunch = it) } }
                )
                Spacer(Modifier.height(8.dp))
                SettingSwitch(
                    label = "Limitar processos em segundo plano",
                    checked = settings.limitBackgroundProcesses,
                    onChange = { vm.update { copy(limitBackgroundProcesses = it) } }
                )
            }
            Spacer(Modifier.height(16.dp))

            Text(
                text = "As otimizações de desempenho são aplicadas como Fast Flags na próxima inicialização.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
