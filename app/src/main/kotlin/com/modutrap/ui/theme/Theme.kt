package com.modutrap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * App theme. Dark by default; the accent is injected so the Personalization
 * screen can restyle the whole app live.
 */
@Composable
fun BloxstrapMobileTheme(
    accentColor: Color,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.16f),
            onPrimaryContainer = Color.White,
            secondary = accentColor.copy(alpha = 0.85f),
            onSecondary = Color.White,
            tertiary = AppColors.Success,
            background = AppColors.Background,
            onBackground = AppColors.OnBackground,
            surface = AppColors.Surface,
            onSurface = AppColors.OnSurface,
            surfaceVariant = AppColors.SurfaceVariant,
            onSurfaceVariant = AppColors.OnSurfaceVariant,
            outline = AppColors.Outline,
            outlineVariant = AppColors.OutlineVariant,
            surfaceTint = accentColor,
            error = AppColors.Error,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.12f),
            onPrimaryContainer = accentColor,
            secondary = accentColor.copy(alpha = 0.7f),
            onSecondary = Color.White,
            tertiary = AppColors.Success,
            background = AppColors.LightBackground,
            onBackground = AppColors.LightOnBackground,
            surface = AppColors.LightSurface,
            onSurface = AppColors.LightOnSurface,
            surfaceVariant = AppColors.LightSurfaceVariant,
            onSurfaceVariant = AppColors.LightOnSurfaceVariant,
            outline = AppColors.LightOutline,
            outlineVariant = AppColors.LightOutline,
            surfaceTint = accentColor,
            error = AppColors.Error,
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
