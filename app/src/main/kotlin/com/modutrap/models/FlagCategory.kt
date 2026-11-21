package com.modutrap.models

/** Coarse grouping for Fast Flags, used for filtering in the UI. */
enum class FlagCategory(val key: String) {
    GRAPHICS("graphics"),
    PERFORMANCE("performance"),
    NETWORK("network"),
    TELEMETRY("telemetry"),
    AUDIO("audio"),
    UI("ui"),
    OTHER("other");

    companion object {
        fun fromKey(key: String): FlagCategory =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: OTHER
    }
}
