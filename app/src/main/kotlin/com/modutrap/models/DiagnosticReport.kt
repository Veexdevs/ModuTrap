package com.modutrap.models

/** A human-readable diagnostics report produced by [com.modutrap.services.DiagnosticsService]. */
data class DiagnosticReport(
    val generatedAt: String,
    val device: String,
    val roblox: String,
    val shizuku: String,
    val flags: String,
    val settings: String,
    val notes: List<String>
) {
    fun toText(): String = buildString {
        appendLine("==== ModuTrap Diagnostic Report ====")
        appendLine("Generated: $generatedAt")
        appendLine("")
        appendLine("-- Device --")
        appendLine(device)
        appendLine("")
        appendLine("-- Roblox --")
        appendLine(roblox)
        appendLine("")
        appendLine("-- Shizuku --")
        appendLine(shizuku)
        appendLine("")
        appendLine("-- Fast Flags --")
        appendLine(flags)
        appendLine("")
        appendLine("-- Settings summary --")
        appendLine(settings)
        if (notes.isNotEmpty()) {
            appendLine("")
            appendLine("-- Notes --")
            notes.forEach { appendLine("- $it") }
        }
    }
}
