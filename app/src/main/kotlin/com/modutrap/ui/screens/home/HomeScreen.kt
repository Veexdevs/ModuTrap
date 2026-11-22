package com.modutrap.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modutrap.R
import com.modutrap.core.AppContainer
import com.modutrap.models.LaunchResult
import com.modutrap.services.OverlayPermission
import com.modutrap.ui.components.StatusPill
import com.modutrap.ui.theme.AppColors
import kotlinx.coroutines.launch

/** HOME: Roblox + Shizuku status, the primary launch action and settings entry. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(container: AppContainer, onNavigate: (String) -> Unit) {
    val vm: HomeViewModel = viewModel()
    val roblox by vm.roblox.collectAsStateWithLifecycle()
    val shizuku by vm.shizuku.collectAsStateWithLifecycle()
    val launching by vm.launching.collectAsStateWithLifecycle()
    val result by vm.result.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(message) {
        message?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Bloxstrap Mobile") },
                actions = {
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Atualizar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Hero: logo + name on a gradient panel.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 20.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(painterResource(R.drawable.bg_hero_gradient)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Bloxstrap Mobile",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Gerenciador e inicializador independente do Roblox",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // Shizuku status.
            AssistChip(
                onClick = { onNavigate(com.modutrap.navigation.Routes.INTEGRATIONS) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(
                            text = when {
                                shizuku.ready -> "Shizuku pronto"
                                shizuku.installed && shizuku.binderAlive -> "Shizuku em execução"
                                shizuku.installed -> "Aguardando autorização"
                                else -> "Shizuku não instalado"
                            },
                            color = when {
                                shizuku.ready -> AppColors.Success
                                shizuku.installed -> AppColors.Warning
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Integrações",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            Spacer(Modifier.height(20.dp))

            // Roblox status card.
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
                            text = if (roblox.installed) "Roblox instalado" else "Roblox não encontrado",
                            color = if (roblox.installed) AppColors.Success else AppColors.Error
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = roblox.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    if (roblox.installed) {
                        InfoLine("Versão", roblox.versionName ?: "desconhecida")
                        val dir = roblox.dataDir
                        if (dir != null) InfoLine("Data dir", dir)
                        InfoLine(
                            "Build",
                            if (roblox.isDebugBuild) "Debug" else "Release"
                        )
                    } else {
                        Text(
                            text = "O Roblox não foi encontrado neste dispositivo. Instale-o oficialmente para começar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { vm.launch() },
                        enabled = !launching,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        if (launching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("INICIAR ROBLOX")
                    }
                    if (!roblox.installed) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { vm.openInstallPage() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("Instalar Roblox")
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // In-game overlay card.
            OverlayCard(container, vm)

            Spacer(Modifier.height(20.dp))

            // Settings entry.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = null)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Configurações",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Gráficos, desempenho, Fast Flags e mais.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    result?.let { launchResult ->
        LaunchResultDialog(result = launchResult, onDismiss = { vm.dismissResult() })
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(88.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun LaunchResultDialog(result: LaunchResult, onDismiss: () -> Unit) {
    val (title, body) = when (result) {
        is LaunchResult.Success -> {
            val notices = result.notices
            "Roblox iniciado" to if (notices.isNotEmpty()) notices.joinToString("\n") else "O Roblox foi aberto com sucesso."
        }

        is LaunchResult.Failure -> "Falha ao iniciar" to result.message
        is LaunchResult.NotInstalled -> "Roblox não instalado" to
            "O Roblox não está instalado neste dispositivo. Instale-o oficialmente e tente novamente."

        is LaunchResult.Blocked -> "Iniciação bloqueada" to result.message
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}

/**
 * "Menu no jogo": toggles the floating in-game overlay, shows the
 * SYSTEM_ALERT_WINDOW permission status (with a request button), and lets the
 * user start/stop the overlay manually.
 */
@Composable
private fun OverlayCard(container: AppContainer, vm: HomeViewModel) {
    val context = LocalContext.current
    val settings by container.settings.settings.collectAsStateWithLifecycle()
    val permissionGranted = remember { OverlayPermission.isGranted(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Permission result arrives here; the state recomposes on next read.
    }

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
                Icon(Icons.Outlined.Layers, contentDescription = null)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Menu no jogo",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Menu flutuante dentro do Roblox, com minimizar e arrastar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            // Enable toggle.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ativar menu no jogo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = settings.inGameOverlayEnabled,
                    onCheckedChange = { vm.update { copy(inGameOverlayEnabled = it) } }
                )
            }

            // Permission status.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(
                    text = if (permissionGranted) "Permissão concedida" else "Permissão ausente",
                    color = if (permissionGranted) AppColors.Success else AppColors.Warning
                )
                Spacer(Modifier.width(10.dp))
                if (!permissionGranted) {
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                permissionLauncher.launch(OverlayPermission.manageIntent(context))
                            }
                        },
                        shape = MaterialTheme.shapes.small
                    ) { Text("Solicitar permissão") }
                }
            }

            // Start / stop buttons.
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { vm.startOverlay() },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Outlined.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Iniciar menu no jogo")
                }
                OutlinedButton(
                    onClick = { vm.stopOverlay() },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Fechar menu no jogo")
                }
            }
        }
    }
}
