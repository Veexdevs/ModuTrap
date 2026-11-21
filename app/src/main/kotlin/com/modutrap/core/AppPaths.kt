package com.modutrap.core

import android.content.Context
import java.io.File

/**
 * All on-device paths the app cares about. Initialized exactly once from
 * [com.modutrap.BloxstrapMobileApp].
 *
 * Nothing here touches Roblox's private data directory directly — that is only
 * attempted through the privileged Shizuku shell (see
 * [com.modutrap.shizuku.ShizukuManager]). These paths live inside this app's
 * own storage.
 */
object AppPaths {

    lateinit var filesDir: File
        private set
    lateinit var cacheDir: File
        private set
    lateinit var logsDir: File
        private set
    lateinit var backupsDir: File
        private set
    lateinit var presetsDir: File
        private set
    lateinit var themesDir: File
        private set
    lateinit var customFilesDir: File
        private set
    lateinit var clientSettingsDir: File
        private set

    fun init(context: Context) {
        filesDir = context.filesDir
        cacheDir = context.cacheDir
        logsDir = File(filesDir, "Logs").apply { mkdirs() }
        backupsDir = File(filesDir, "Backups").apply { mkdirs() }
        presetsDir = File(filesDir, "Presets").apply { mkdirs() }
        themesDir = File(filesDir, "Themes").apply { mkdirs() }
        customFilesDir = File(filesDir, "Custom").apply { mkdirs() }
        clientSettingsDir = File(filesDir, "ClientSettings").apply { mkdirs() }
    }

    /**
     * Local copy of the ClientAppSettings.json we *want* Roblox to use.
     *
     * If the app has Shizuku/root it is injected into Roblox's data directory
     * at launch. If not, this local file is still kept so the user can inspect,
     * export and restore it — and the UI clearly reports that it will not take
     * effect inside Roblox without privileges.
     */
    val clientAppSettingsFile: File
        get() = File(clientSettingsDir, "ClientAppSettings.json")

    /** Serialized full-backup file produced by [com.modutrap.personalization.BackupManager]. */
    val settingsBackupFile: File
        get() = File(backupsDir, "modutrap-backup.json")

    fun logFile(name: String): File = File(logsDir, name)
}
