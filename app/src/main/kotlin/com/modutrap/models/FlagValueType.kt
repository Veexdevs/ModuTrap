package com.modutrap.models

/**
 * Logical type of a Roblox Fast Flag value. Used for validation so we never
 * write a nonsense value (e.g. "True" into an integer flag).
 */
enum class FlagValueType {
    BOOLEAN,
    INTEGER,
    STRING
}
