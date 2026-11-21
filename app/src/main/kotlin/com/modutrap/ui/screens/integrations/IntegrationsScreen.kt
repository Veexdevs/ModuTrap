package com.modutrap.ui.screens.integrations

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modutrap.core.AppContainer
import com.modutrap.ui.components.ScreenScaffold
import com.modutrap.ui.components.SectionCard
import com.modutrap.ui.components.StatusPill
import com.modutrap.ui.theme.AppColors
import com.modutrap.ui.viewmodels.IntegrationsViewModel
import kotlinx.coroutines.launch

/** INTEGRAÇÕES: Shizuku status, permission request and diagnostics. */
@Composable
fun IntegrationsScreen(container: AppContainer) {
    val vm: IntegrationsViewModel = viewModel()
    val status by vm.status.collectAsStateWithLifecycle()
    val report by vm.report.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(message) {
        message?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
    }

    ScreenScaffold(
        title = "Integrações",
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Shizuku status card.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(
                            text = when {
                                status.ready -> "Serviço ativo"
                                status.installed && status.binderAlive -> "Serviço inativo"
                                status.installed -> "Não autorizado"
                                else -> "Não instalado"
                            },
                            color = when {
                                status.ready -> AppColors.Success
                                status.installed -> AppColors.Warning
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Shizuku",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Integração opcional para aplicar Fast Flags e executar diagnósticos com privilégios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    InfoLine("Instalado", if (status.installed) "Sim" else "Não")
                    InfoLine("Serviço", if (status.binderAlive) "Ativo" else "Inativo")
                    InfoLine("Autorização", if (status.permissionGranted) "Concedida" else "Não concedida")
                    status.uid?.let { InfoLine("UID", it.toString()) }
                    status.version?.let { InfoLine("Versão", it) }

                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { vm.requestPermission() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Solicitar autorização")
                    }
                    if (!status.installed) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "O Shizuku não está instalado. Instale-o para usar recursos com privilégios. O app continua funcionando como inicializador sem ele.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // Diagnostics.
            SectionCard(
                title = "Diagnóstico",
                description = "Relatório do dispositivo, Roblox e estado do Shizuku."
            ) {
                Button(
                    onClick = { vm.generateReport() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Gerar relatório")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    report?.let { reportData ->
        val text = vm.reportText() ?: ""
        AlertDialog(
            onDismissRequest = { vm.dismissReport() },
            title = { Text("Diagnóstico") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    text.lineSequence().forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(text))
                    scope.launch { snackbarHostState.showSnackbar("Relatório copiado.") }
                }) { Text("Copiar") }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissReport() }) { Text("Fechar") }
            }
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
