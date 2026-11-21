package com.modutrap.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * JSON helpers shared by the Fast Flag, Preset and Backup layers.
 *
 * The canonical on-disk format mirrors Bloxstrap / Roblox's expected schema:
 * ```
 * { "FFlags": { "FFlagKey": "value", ... } }
 * ```
 * but a bare top-level map of flags is also tolerated on import.
 */
object JsonUtils {

    val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    /** Encode a flat map of flags into the canonical `{ "FFlags": { ... } }` document. */
    fun encodeFlags(flags: Map<String, String>): String {
        val obj = buildJsonObject {
            put(
                "FFlags",
                buildJsonObject {
                    flags.toSortedMap().forEach { (k, v) -> put(k, JsonPrimitive(v)) }
                }
            )
        }
        return json.encodeToString(JsonObject.serializer(), obj)
    }

    /**
     * Parse a JSON document and return the flag map it contains.
     *
     * Accepts either `{ "FFlags": { ... } }` or a bare `{ "FFlag": "value" }` map.
     * Values are coerced to strings (Roblox stores all Fast Flag values as
     * strings). Returns null when the document is not a valid object.
     */
    fun parseFlags(raw: String): Map<String, String>? {
        return runCatching {
            val element: JsonElement = json.parseToJsonElement(raw)
            val obj: JsonObject = element.jsonObject
            val flagsObj: JsonObject = obj["FFlags"]?.jsonObject ?: obj
            flagsObj.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
        }.getOrNull()
    }

    /** Parse any JSON object into a string->string map (used for presets/backups). */
    fun parseStringMap(raw: String): Map<String, String>? =
        runCatching {
            val obj: JsonObject = json.parseToJsonElement(raw).jsonObject
            obj.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
        }.getOrNull()

    /** Read a file's text, or null if it is missing/unreadable. */
    fun readFile(file: File): String? = runCatching {
        if (file.exists()) file.readText() else null
    }.getOrNull()

    /** Write text to a file, creating parent directories. */
    fun writeFile(file: File, text: String): Boolean = runCatching {
        file.parentFile?.mkdirs()
        file.writeText(text)
        true
    }.getOrDefault(false)
}
