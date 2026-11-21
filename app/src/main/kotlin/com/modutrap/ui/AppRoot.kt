package com.modutrap.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.modutrap.core.AppContainer
import com.modutrap.navigation.AppNavGraph
import com.modutrap.personalization.ThemeManager
import com.modutrap.ui.theme.BloxstrapMobileTheme

/**
 * Root composition: resolves the active theme (dark/light/system + accent) from
 * settings, registers the Shizuku lifecycle listeners, and hosts the nav graph.
 */
@Composable
fun AppRoot(container: AppContainer) {
    val settings by container.settings.settings.collectAsStateWithLifecycle()

    val darkTheme = when (settings.themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val accent = ThemeManager.parseColor(settings.accentColor) ?: Color(0xFF8B5CF6)

    // Register Shizuku binder/permission listeners for the process lifetime.
    DisposableEffect(Unit) {
        val listeners = container.shizuku.init(
            onGranted = { /* status is read on demand from the integration */ },
            onLost = { }
        )
        onDispose {
            container.shizuku.cleanup(listeners)
        }
    }

    BloxstrapMobileTheme(
        accentColor = accent,
        darkTheme = darkTheme
    ) {
        AppNavGraph(container)
    }
}
