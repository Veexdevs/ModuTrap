package com.modutrap.ui.screens.settings

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
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.modutrap.core.AppContainer
import com.modutrap.navigation.Routes
import com.modutrap.ui.components.ScreenScaffold

/** SETTINGS: entry point to the Graphics, Performance, Launch and Personalization groups. */
@Composable
fun SettingsScreen(container: AppContainer, onNavigate: (String) -> Unit) {
    ScreenScaffold(title = "Configurações") { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SettingEntry(
                icon = Icons.Outlined.GraphicEq,
                title = "Gráficos",
                description = "Qualidade de renderização, sombras, MSAA e distância.",
                onClick = { onNavigate(Routes.GRAPHICS) }
            )
            Spacer(Modifier.height(12.dp))
            SettingEntry(
                icon = Icons.Outlined.Memory,
                title = "Desempenho",
                description = "Limite de FPS, GC, telemetria e otimizações.",
                onClick = { onNavigate(Routes.PERFORMANCE) }
            )
            Spacer(Modifier.height(12.dp))
            SettingEntry(
                icon = Icons.Outlined.RocketLaunch,
                title = "Iniciação",
                description = "Pacote do Roblox e aplicar configurações ao iniciar.",
                onClick = { onNavigate(Routes.LAUNCH) }
            )
            Spacer(Modifier.height(12.dp))
            SettingEntry(
                icon = Icons.Outlined.Palette,
                title = "Personalização",
                description = "Temas, presets visuais e gerenciamento de arquivos.",
                onClick = { onNavigate(Routes.PERSONALIZATION) }
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingEntry(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
