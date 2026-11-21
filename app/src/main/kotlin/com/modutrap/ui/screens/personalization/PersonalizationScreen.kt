package com.modutrap.ui.screens.personalization

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modutrap.core.AppContainer
import com.modutrap.personalization.ThemeManager
import com.modutrap.ui.components.OptionRow
import com.modutrap.ui.components.ScreenScaffold
import com.modutrap.ui.components.SectionCard
import com.modutrap.ui.viewmodels.PersonalizationViewModel
import com.modutrap.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

private val themeOptions = listOf("Sistema", "Escuro", "Claro")
private val themeValues = listOf("system", "dark", "light")

/** PERSONALIZAÇÃO: theme, accent, visual presets, custom files and backups. */
@Composable
fun PersonalizationScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val vm: PersonalizationViewModel = viewModel()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val visualPresets by vm.visualPresets.collectAsStateWithLifecycle()
    val files by vm.files.collectAsStateWithLifecycle()
    val backups by vm.backups.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(message) {
        message?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
    }

    // Dialog state.
    var saveVisualPresetOpen by remember { mutableStateOf(false) }
    var saveFileOpen by remember { mutableStateOf(false) }
    var backupNameOpen by remember { mutableStateOf(false) }
    var readFile by remember { mutableStateOf<File?>(null) }

    ScreenScaffold(
        title = "Personalização",
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Theme.
            SectionCard(title = "Tema") {
                OptionRow(
                    options = themeOptions,
                    selected = themeOptions.firstOrNull {
                        themeValues[themeOptions.indexOf(it)] == settings.themeMode
                    } ?: themeOptions[0],
                    onSelect = { label ->
                        vm.setTheme(themeValues[themeOptions.indexOf(label)])
                    }
                )
            }
            Spacer(Modifier.height(16.dp))

            // Accent.
            SectionCard(title = "Cor de acento") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    vm.accentPresets.forEach { preset ->
                        val selected = preset.hex.equals(settings.accentColor, ignoreCase = true)
                        val color = ThemeManager.parseColor(preset.hex) ?: Color.Gray
                        Surface(
                            onClick = { vm.setAccent(preset.hex) },
                            shape = CircleShape,
                            color = color,
                            contentColor = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                )
                        ) {
                            if (selected) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Personalizar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = settings.accentColor,
                    onValueChange = { vm.setAccent(it) },
                    label = { Text("Valor hex (#RRGGBB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))

            // Visual presets.
            SectionCard(
                title = "Presets visuais",
                description = "Salve e reutilize combinações de tema e acento."
            ) {
                Button(
                    onClick = { saveVisualPresetOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Salvar preset atual")
                }
                Spacer(Modifier.height(12.dp))
                if (visualPresets.isEmpty()) {
                    Text(
                        "Nenhum preset visual salvo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    visualPresets.forEach { preset ->
                        PresetRow(
                            name = preset.name,
                            summary = "${preset.themeMode} · ${preset.accentColor}",
                            onApply = { vm.applyVisualPreset(preset) },
                            onDelete = { vm.deleteVisualPreset(preset.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Custom files.
            SectionCard(
                title = "Gerenciamento de arquivos",
                description = "Arquivos próprios do aplicativo (temas, JSON, configurações exportadas)."
            ) {
                OutlinedButton(
                    onClick = { saveFileOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Novo arquivo")
                }
                Spacer(Modifier.height(12.dp))
                if (files.isEmpty()) {
                    Text(
                        "Nenhum arquivo próprio encontrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    files.forEach { file ->
                        FileRow(
                            name = file.name,
                            size = FileUtils.humanSize(file.length()),
                            onClick = { readFile = file },
                            onDelete = { vm.deleteCustomFile(file.name) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Backups.
            SectionCard(
                title = "Backup e restauração",
                description = "Backup completo de configurações e Fast Flags."
            ) {
                OutlinedButton(
                    onClick = { backupNameOpen = true },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Criar backup")
                }
                Spacer(Modifier.height(12.dp))
                if (backups.isEmpty()) {
                    Text(
                        "Nenhum backup disponível.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    backups.forEach { backup ->
                        FileRow(
                            name = backup.name,
                            size = backup.createdAt,
                            onClick = { vm.restoreBackup(backup) },
                            onDelete = { vm.deleteBackup(backup) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (saveVisualPresetOpen) {
        var presetName by remember { mutableStateOf("") }
        TextInputDialog(
            title = "Salvar preset visual",
            onDismiss = { saveVisualPresetOpen = false },
            onConfirm = {
                if (presetName.isNotBlank()) vm.saveVisualPreset(presetName)
                saveVisualPresetOpen = false
            },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Nome do preset") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    if (saveFileOpen) {
        var fileName by remember { mutableStateOf("") }
        var fileContent by remember { mutableStateOf("") }
        TextInputDialog(
            title = "Novo arquivo",
            onDismiss = { saveFileOpen = false },
            onConfirm = {
                if (FileUtils.isValidFileName(fileName)) vm.saveCustomFile(fileName, fileContent)
                saveFileOpen = false
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("Nome do arquivo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = fileContent,
                        onValueChange = { fileContent = it },
                        label = { Text("Conteúdo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (backupNameOpen) {
        var backupName by remember { mutableStateOf("") }
        TextInputDialog(
            title = "Criar backup",
            onDismiss = { backupNameOpen = false },
            onConfirm = {
                if (backupName.isNotBlank()) vm.createBackup(backupName)
                backupNameOpen = false
            },
            text = {
                OutlinedTextField(
                    value = backupName,
                    onValueChange = { backupName = it },
                    label = { Text("Nome do backup") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    val fileToRead = readFile
    if (fileToRead != null) {
        val content = vm.readCustomFile(fileToRead.name)
        TextInputDialog(
            title = fileToRead.name,
            onDismiss = { readFile = null },
            onConfirm = { readFile = null },
            text = {
                Column {
                    Text(
                        "${fileToRead.name} · ${FileUtils.humanSize(fileToRead.length())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content ?: "(vazio)",
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
private fun PresetRow(
    name: String,
    summary: String,
    onApply: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onApply,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Excluir")
            }
        }
    }
}

@Composable
private fun FileRow(
    name: String,
    size: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(size, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Excluir")
            }
        }
    }
}

@Composable
private fun TextInputDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    text: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { text.invoke() },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
