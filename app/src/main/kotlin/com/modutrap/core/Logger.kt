package com.modutrap.core

import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tiny logging facade. Writes to both Logcat and a rolling file under
 * [AppPaths.logsDir] so diagnostics can be exported from the Integrações screen.
 *
 * All methods are defensive: a logging failure must never crash the app.
 */
object Logger {

    private const val TAG = "ModuTrap"
    private const val MAX_LOG_BYTES = 512L * 1024L

    private val lock = Any()

    private fun logFile(): File? = runCatching { AppPaths.logFile("app.log") }.getOrNull()

    fun writeLine(tag: String, message: String) {
        Log.d(TAG, "[$tag] $message")
        append("$tag: $message")
    }

    fun writeException(tag: String, throwable: Throwable) {
        Log.e(TAG, "[$tag] ${throwable.message}", throwable)
        append("$tag: ${throwable.javaClass.simpleName}: ${throwable.message}")
        throwable.stackTrace.take(6).forEach {
            append("    at $it")
        }
    }

    fun clear() {
        synchronized(lock) {
            runCatching { logFile()?.writeText("") }
        }
    }

    private fun append(line: String) {
        synchronized(lock) {
            val file = logFile() ?: return
            val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
            try {
                var text = file.readText()
                text += "$stamp $line\n"
                if (text.toByteArray(Charsets.UTF_8).size > MAX_LOG_BYTES) {
                    // Keep the tail, drop the oldest half.
                    val half = text.length / 2
                    text = text.substring(half)
                }
                file.writeText(text)
            } catch (_: Exception) {
                // Logging must never throw.
            }
        }
    }
}
