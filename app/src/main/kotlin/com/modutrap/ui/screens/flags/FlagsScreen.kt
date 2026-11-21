package com.modutrap.ui.screens.flags

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modutrap.core.AppContainer
import com.modutrap.flags.FastFlagCatalog
import com.modutrap.flags.FastFlagValidator
import com.modutrap.models.FFlagEntry
import com.modutrap.models.FlagCategory
import com.modutrap.models.FlagPreset
import com.modutrap.ui.components.EmptyState
import com.modutrap.ui.components.ScreenScaffold
import com.modutrap.ui.components.SectionCard
import com.modutrap.ui.components.StatusPill
import com.modutrap.ui.viewmodels.FlagsViewModel
import com.modutrap.ui.theme.AppColors
import kotlinx.coroutines.launch

/** FAST FLAGS: searchable list, add/edit/delete, JSON import/export, presets, backups. */
@Composable
fun FlagsScreen(container: AppContainer) {
    val vm: FlagsViewModel = viewModel()
    val flags by vm.flags.collectAsStateWithLifecycle()
    val search by vm.search.collectAsStateWithLifecycle()
    val presets by vm.presets.collectAsStateWithLifecycle()
    val backups by vm.backups.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(message) {
        message?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
    }

    // Dialog state.
    var editing by remember { mutableStateOf<FFlagEntry?>(null) }
    var addingOpen by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<FFlagEntry?>(null) }
    var savePresetOpen by remember { mutableStateOf(false) }
    var backupNameOpen by remember { mutableStateOf(false) }

    // Import / export launchers.
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importUri(context, it) { raw -> vm.importJson(raw, replace = false) } }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportUri(context, it) { vm.exportJson() } }
    }

    val visible = vm.visibleFlags()

    ScreenScaffold(
        title = "Fast Flags",
        actions = {
            IconButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                Icon(Icons.Outlined.Upload, contentDescription = "Importar JSON")
            }
            IconButton(onClick = { exportLauncher.launch("modutrap-flags.json") }) {
                Icon(Icons.Outlined.Download, contentDescription = "Exportar JSON")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Search.
            OutlinedTextField(
                value = search,
                onValueChange = vm::setSearch,
                label = { Text("Pesquisar flag…") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // Flag list.
            Text(
                text = "Flags (${visible.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            if (visible.isEmpty()) {
                EmptyState("Nenhuma flag configurada. Adicione uma ou importe um JSON.")
            } else {
                visible.forEach { entry ->
                    FlagRow(
                        entry = entry,
                        category = categoryLabel(FastFlagCatalog.info(entry.key)?.category),
                        verified = FastFlagValidator.isVerified(entry.key),
                        onEdit = { editing = entry },
                        onDelete = { deleteTarget = entry }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Add button.
            OutlinedButton(
                onClick = { addingOpen = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Adicionar flag")
            }
            Spacer(Modifier.height(20.dp))

            // Presets.
            SectionCard(title = "Presets") {
                Button(
                    onClick = { savePresetOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Salvar preset atual")
                }
                Spacer(Modifier.height(12.dp))
                if (presets.isEmpty()) {
                    Text(
                        "Nenhum preset salvo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    presets.forEach { preset ->
                        PresetRow(
                            preset = preset,
                            onApply = { vm.applyPreset(preset) },
                            onDelete = { vm.deletePreset(preset.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Backups.
            SectionCard(title = "Backups") {
                OutlinedButton(
                    onClick = { backupNameOpen = true },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
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
                    backups.forEach { name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            TextButton(onClick = { vm.restoreBackup(name) }) { Text("Restaurar") }
                            TextButton(onClick = { vm.deleteBackup(name) }) { Text("Excluir") }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    // Add / edit flag dialog.
    var dialogKey by remember { mutableStateOf("") }
    var dialogValue by remember { mutableStateOf("") }
    val isEditing = editing != null
    LaunchedEffect(editing) {
        editing?.let {
            dialogKey = it.key
            dialogValue = it.value
        }
    }
    LaunchedEffect(addingOpen) {
        if (addingOpen) {
            dialogKey = ""
            dialogValue = ""
        }
    }
    val dialogOpen = addingOpen || editing != null
    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = {
                addingOpen = false
                editing = null
            },
            title = { Text(if (isEditing) "Editar Fast Flag" else "Adicionar Fast Flag") },
            text = {
                Column {
                    OutlinedTextField(
                        value = dialogKey,
                        onValueChange = { dialogKey = it },
                        label = { Text("Nome da flag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dialogValue,
                        onValueChange = { dialogValue = it },
                        label = { Text("Valor") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (dialogKey.isNotBlank()) {
                            if (isEditing) vm.addOrUpdate(dialogKey, dialogValue) else vm.addOrUpdate(dialogKey, dialogValue)
                        }
                        addingOpen = false
                        editing = null
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    addingOpen = false
                    editing = null
                }) { Text("Cancelar") }
            }
        )
    }

    // Delete confirm dialog.
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Excluir flag") },
            text = { Text("Excluir ${target.key}? Essa ação pode ser revertida com um backup.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(target.key)
                    deleteTarget = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") }
            }
        )
    }

    // Save preset dialog.
    if (savePresetOpen) {
        var presetName by remember { mutableStateOf("") }
        var presetDesc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { savePresetOpen = false },
            title = { Text("Salvar preset") },
            text = {
                Column {
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = presetDesc,
                        onValueChange = { presetDesc = it },
                        label = { Text("Descrição") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (presetName.isNotBlank()) {
                        vm.savePreset("preset-${System.currentTimeMillis()}", presetName, presetDesc)
                    }
                    savePresetOpen = false
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { savePresetOpen = false }) { Text("Cancelar") }
            }
        )
    }

    // Create backup dialog.
    if (backupNameOpen) {
        var backupName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { backupNameOpen = false },
            title = { Text("Criar backup") },
            text = {
                OutlinedTextField(
                    value = backupName,
                    onValueChange = { backupName = it },
                    label = { Text("Nome do backup") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (backupName.isNotBlank()) vm.createBackup(backupName)
                    backupNameOpen = false
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { backupNameOpen = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FlagRow(
    entry: FFlagEntry,
    category: String?,
    verified: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onEdit,
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
                Text(
                    entry.key,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    entry.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (category != null) {
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text(category, style = MaterialTheme.typography.labelSmall) })
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(
                text = if (verified) "Verificada" else "Não verificada",
                color = if (verified) AppColors.Success else AppColors.Warning
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onEdit) {
                Text("Editar", style = MaterialTheme.typography.labelMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Excluir")
            }
        }
    }
}

@Composable
private fun PresetRow(
    preset: FlagPreset,
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
                Text(preset.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${preset.flags.size} flags · ${preset.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onApply) { Text("Aplicar") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Excluir preset")
            }
        }
    }
}

private fun categoryLabel(category: FlagCategory?): String? = when (category) {
    FlagCategory.GRAPHICS -> "Gráficos"
    FlagCategory.PERFORMANCE -> "Desempenho"
    FlagCategory.NETWORK -> "Rede"
    FlagCategory.TELEMETRY -> "Telemetria"
    FlagCategory.AUDIO -> "Áudio"
    FlagCategory.UI -> "Interface"
    FlagCategory.OTHER -> "Outras"
    null -> null
}

private fun importUri(
    context: android.content.Context,
    uri: Uri,
    onText: (String) -> Unit
) {
    val text = runCatching {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
    }.getOrNull()
    if (text != null) onText(text)
}

private fun exportUri(
    context: android.content.Context,
    uri: Uri,
    produce: () -> String
) {
    val text = produce()
    val ok = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
    }.getOrDefault(false)
    if (!ok) {
        // Fallback: share the exported JSON via an intent if the document write failed.
        runCatching {
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_PERMISSION)
            }
            context.startActivity(Intent.createChooser(share, "Exportar Fast Flags"))
        }
    }
}
