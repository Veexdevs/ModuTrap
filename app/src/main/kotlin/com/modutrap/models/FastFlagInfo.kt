package com.modutrap.models

/**
 * Metadata for a Fast Flag known to be part of Roblox's current Fast Flag set.
 *
 * The catalog in [com.modutrap.flags.FastFlagCatalog] is curated from public
 * Roblox Fast Flag documentation and community-verified lists. A flag that is
 * *not* in the catalog can still be stored by the user, but it is marked
 * [verified] = false and the UI warns that it may not take effect.
 */
data class FastFlagInfo(
    val key: String,
    val category: FlagCategory,
    val type: FlagValueType,
    val defaultValue: String,
    val description: String,
    val verified: Boolean = true
)
