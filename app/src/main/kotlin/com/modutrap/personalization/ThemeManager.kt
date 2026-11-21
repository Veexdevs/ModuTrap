package com.modutrap.personalization

import androidx.compose.ui.graphics.Color

/** A named accent-color preset offered in the Personalization screen. */
data class AccentPreset(val name: String, val hex: String)

/**
 * Accent-color presets and hex parsing for the app's visual theme.
 *
 * The theme itself (dark/light/system) is stored as a string in settings; the
 * accent is applied to the Material 3 color scheme at composition time.
 */
object ThemeManager {

    val accentPresets: List<AccentPreset> = listOf(
        AccentPreset("Violeta", "#8B5CF6"),
        AccentPreset("Azul", "#3B82F6"),
        AccentPreset("Roxo profundo", "#6D28D9"),
        AccentPreset("Rosa", "#EC4899"),
        AccentPreset("Vermelho", "#EF4444"),
        AccentPreset("Laranja", "#F59E0B"),
        AccentPreset("Verde", "#10B981"),
        AccentPreset("Ciano", "#06B6D4"),
        AccentPreset("Grafite", "#94A3B8")
    )

    /** Parse a `#RGB`, `#RRGGBB` or `#AARRGGBB` hex string. Returns null if invalid. */
    fun parseColor(hex: String): Color? {
        val h = hex.trim()
        return when (h.length) {
            4 -> runCatching {
                Color(
                    red = h[1].digitToInt(16) * 17,
                    green = h[2].digitToInt(16) * 17,
                    blue = h[3].digitToInt(16) * 17
                )
            }.getOrNull()

            7 -> runCatching {
                Color(
                    red = h.substring(1, 3).toInt(16),
                    green = h.substring(3, 5).toInt(16),
                    blue = h.substring(5, 7).toInt(16)
                )
            }.getOrNull()

            9 -> runCatching {
                Color(
                    alpha = h.substring(1, 3).toInt(16) / 255f,
                    red = h.substring(3, 5).toInt(16),
                    green = h.substring(5, 7).toInt(16),
                    blue = h.substring(7, 9).toInt(16)
                )
            }.getOrNull()

            else -> null
        }
    }

    fun isValidHex(hex: String): Boolean = parseColor(hex) != null
}
