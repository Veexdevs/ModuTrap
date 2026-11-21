package com.modutrap.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val packageOptions = listOf("Roblox global", "Roblox VNG", "Pacote personalizado")
private val packageValues = listOf("global", "vng", "custom")

/** LAUNCH: which Roblox package to target and what to apply on launch. */
@Composable
fun LaunchScreen(container: AppContainer, onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel()
    val settings by vm.settings.collectAsStateWithLifecycle()

    var customPackage by remember { mutableStateOf(settings.customPackage) }

    ScreenScaffold(title = "Iniciação", onBack = onBack) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionCard(title = "Pacote do Roblox") {
                Text(
                    text = "Pacote do Roblox",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                OptionRow(
                    options = packageOptions,
                    selected = packageOptions.firstOrNull {
                        packageValues[packageOptions.indexOf(it)] == settings.preferredPackage
                    } ?: packageOptions[0],
                    onSelect = { label ->
                        vm.update { copy(preferredPackage = packageValues[packageOptions.indexOf(label)]) }
                    }
                )

                if (settings.preferredPackage == "custom") {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customPackage,
                        onValueChange = {
                            customPackage = it
                            vm.update { copy(customPackage = it) }
                        },
                        label = { Text("Pacote personalizado") },
                        singleLine = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            SectionCard(title = "Aplicação na inicialização") {
                SettingSwitch(
                    label = "Aplicar Fast Flags ao iniciar",
                    checked = settings.applyFlagsOnLaunch,
                    onChange = { vm.update { copy(applyFlagsOnLaunch = it) } },
                    description = "Escreve as Fast Flags configuradas no ClientAppSettings.json."
                )
                Spacer(Modifier.height(8.dp))
                SettingSwitch(
                    label = "Aplicar otimizações ao iniciar",
                    checked = settings.applyOptimizationsOnLaunch,
                    onChange = { vm.update { copy(applyOptimizationsOnLaunch = it) } }
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionCard(title = "Histórico") {
                Text(
                    text = "Última inicialização",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (settings.lastLaunchTs.isBlank()) "nunca" else settings.lastLaunchTs,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
