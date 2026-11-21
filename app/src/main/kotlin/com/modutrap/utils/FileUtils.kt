package com.modutrap.utils

import java.io.File

/** Small file helpers used by the personalization/backup layers. */
object FileUtils {

    fun ensureDir(dir: File): File = dir.apply { if (!exists()) mkdirs() }

    fun listFiles(dir: File): List<File> =
        dir.listFiles { f -> f.isFile }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()

    fun exists(path: String): Boolean = File(path).exists()

    /** Human-readable size, e.g. "12.4 KB". */
    fun humanSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB")
        var value = bytes.toDouble()
        var unit = -1
        repeat(units.size) {
            value /= 1024.0
            unit++
            if (value < 1024) return "%.1f ${units[unit]}".format(value)
        }
        return "%.1f TB".format(value / 1024.0)
    }

    fun formatDate(epochMillis: Long): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
            .format(java.util.Date(epochMillis))
    }

    fun isValidFileName(name: String): Boolean =
        name.isNotBlank() &&
            !name.contains('/') &&
            !name.contains('\\') &&
            !name.contains("..") &&
            name.length <= 100
}
