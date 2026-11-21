package com.modutrap.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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

private val qualityOptions = listOf("Baixo", "Médio", "Alto", "Máximo")
private val msaaOptions = listOf("Desligado", "2x", "4x", "8x")

/** GRAPHICS: quality, render distance, MSAA and per-feature graphics toggles. */
@Composable
fun GraphicsScreen(container: AppContainer, onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel()
    val settings by vm.settings.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = "Gráficos",
        onBack = onBack
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionCard(title = "Qualidade") {
                Text(
                    text = "Qualidade gráfica",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                OptionRow(
                    options = qualityOptions,
                    selected = qualityOptions.getOrNull(settings.graphicsQuality) ?: qualityOptions[0],
                    onSelect = { label -> vm.update { copy(graphicsQuality = qualityOptions.indexOf(label)) } }
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionCard(title = "Distância e amostragem") {
                Text(
                    text = "Distância de renderização",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Valor atual: ${settings.renderDistance}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = settings.renderDistance.toFloat(),
                    onValueChange = { vm.update { copy(renderDistance = it.toInt()) } },
                    valueRange = 0f..10f,
                    steps = 9
                )

                Spacer(Modifier.height(16.dp))
                Text("MSAA", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                OptionRow(
                    options = msaaOptions,
                    selected = msaaOptions.getOrNull(settings.msaaMode) ?: msaaOptions[0],
                    onSelect = { label -> vm.update { copy(msaaMode = msaaOptions.indexOf(label)) } }
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionCard(title = "Funcionalidades") {
                SettingSwitch(
                    label = "Sombras",
                    checked = settings.shadowsEnabled,
                    onChange = { vm.update { copy(shadowsEnabled = it) } }
                )
                Spacer(Modifier.height(8.dp))
                SettingSwitch(
                    label = "Efeitos de pós-processamento",
                    checked = settings.postFxEnabled,
                    onChange = { vm.update { copy(postFxEnabled = it) } }
                )
                Spacer(Modifier.height(8.dp))
                SettingSwitch(
                    label = "Textures de terreno",
                    checked = settings.terrainTextures,
                    onChange = { vm.update { copy(terrainTextures = it) } },
                    description = "Desativar carrega texturas de terreno simplificadas."
                )
                Spacer(Modifier.height(8.dp))
                SettingSwitch(
                    label = "Resolução dinâmica",
                    checked = settings.dynamicResolution,
                    onChange = { vm.update { copy(dynamicResolution = it) } }
                )
            }
            Spacer(Modifier.height(16.dp))

            Text(
                text = "As configurações gráficas são aplicadas como Fast Flags na próxima inicialização.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
