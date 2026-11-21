package com.modutrap.flags

import com.modutrap.models.FastFlagInfo
import com.modutrap.models.FlagValueType

/** Outcome of validating a single Fast Flag value. */
data class FlagValidation(
    val valid: Boolean,
    val normalizedValue: String?,
    val message: String?
) {
    companion object {
        val ok: FlagValidation = FlagValidation(true, null, null)
    }
}

/**
 * Validates Fast Flag keys and values against the verified catalog.
 *
 * Rules:
 *  - Boolean flags accept `true/false` (any case) and are normalized to
 *    Roblox's canonical `True`/`False`.
 *  - Integer flags must parse as an integer.
 *  - String flags accept any value.
 *  - A key not in the catalog is *not* rejected outright, but it is reported as
 *    unverified so the UI can warn the user that it may not take effect.
 */
object FastFlagValidator {

    /** Validate a value against a known [info]. */
    fun validate(info: FastFlagInfo, rawValue: String): FlagValidation {
        val value = rawValue.trim()
        return when (info.type) {
            FlagValueType.BOOLEAN -> {
                val lower = value.lowercase()
                when (lower) {
                    "true" -> FlagValidation(true, "True", null)
                    "false" -> FlagValidation(true, "False", null)
                    else -> FlagValidation(false, null, "Valor booleano inválido. Use True ou False.")
                }
            }

            FlagValueType.INTEGER -> {
                if (value.toIntOrNull() != null) {
                    FlagValidation(true, value, null)
                } else {
                    FlagValidation(false, null, "Valor numérico inválido. Use um número inteiro.")
                }
            }

            FlagValueType.STRING -> FlagValidation(true, value, null)
        }
    }

    /** Validate an arbitrary key/value pair, resolving metadata from the catalog. */
    fun validate(key: String, rawValue: String): FlagValidation {
        val info = FastFlagCatalog.info(key)
        if (info == null) {
            // Unknown flag: accept but flag it as unverified. The UI warns.
            return FlagValidation(true, rawValue.trim(), null)
        }
        return validate(info, rawValue)
    }

    /** Whether [key] is part of the verified catalog. */
    fun isVerified(key: String): Boolean = FastFlagCatalog.isKnown(key)
}
