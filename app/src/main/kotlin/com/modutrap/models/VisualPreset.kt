package com.modutrap.models

import kotlinx.serialization.Serializable

/** A saved visual/appearance preset for the Personalization screen. */
@Serializable
data class VisualPreset(
    val id: String,
    val name: String,
    val themeMode: String,
    val accentColor: String
)
