package com.modutrap.models

import kotlinx.serialization.Serializable

/** A saved bundle of Fast Flags that can be applied in one step. */
@Serializable
data class FlagPreset(
    val id: String,
    val name: String,
    val description: String,
    val flags: Map<String, String>
)
