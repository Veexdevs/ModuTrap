package com.modutrap.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.ContextCompat
import com.modutrap.core.Logger
import com.modutrap.models.ShizukuStatus
import moe.shizuku.server.IRemoteProcess
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

/**
 * Wraps the Shizuku API so the rest of the app never touches its internals.
 *
 * Shizuku provides a privileged binder that lets an app run shell commands as
 * the shell/root user without a rooted device. We use it for exactly two
 * things:
 *   1. Writing [ClientAppSettings.json] into Roblox's private data directory so
 *      Fast Flags actually take effect at launch.
 *   2. Running read-only diagnostics (version, UID, package inspection).
 *
 * Every call is defensive: if Shizuku is not installed, not running, or the user
 * has not granted permission, methods return safe defaults instead of throwing.
 * The app continues to work as a launcher without Shizuku.
 */
object ShizukuManager {

    private const val TAG = "ModuTrap.Shizuku"
    private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    private const val PERMISSION_REQUEST_CODE = 0xC001

    @Volatile
    private lateinit var appContext: Context

    fun initContext(context: Context) {
        appContext = context.applicationContext
    }

    /** Whether the Shizuku manager app is installed on the device. */
    fun isShizukuInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    } catch (_: Throwable) {
        false
    }

    fun isBinderAlive(): Boolean = try {
        Shizuku.pingBinder()
    } catch (_: Throwable) {
        false
    }

    fun hasPermission(): Boolean = try {
        if (Shizuku.isPreV11()) {
            ContextCompat.checkSelfPermission(
                appContext,
                "moe.shizuku.manager.permission.API_V23"
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    } catch (_: Throwable) {
        false
    }

    /** True only when Shizuku is installed, the service is running and permission granted. */
    fun isReady(): Boolean = try {
        Shizuku.pingBinder() && hasPermission()
    } catch (_: Throwable) {
        false
    }

    fun shouldShowRationale(): Boolean = try {
        !Shizuku.isPreV11() && Shizuku.shouldShowRequestPermissionRationale()
    } catch (_: Throwable) {
        false
    }

    fun getUid(): Int? = try {
        if (Shizuku.pingBinder()) Shizuku.getUid() else null
    } catch (_: Throwable) {
        null
    }

    fun getVersion(): String? = try {
        if (Shizuku.pingBinder()) Shizuku.getVersion().toString() else null
    } catch (_: Throwable) {
        null
    }

    /**
     * Request Shizuku permission. Returns true if permission was already granted.
     * Safe to call repeatedly. When the binder is not yet alive the request is
     * queued and shown once Shizuku starts (via the listeners registered in [init]).
     */
    fun requestPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11()) {
                hasPermission()
            } else {
                if (hasPermission()) {
                    true
                } else {
                    Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
                    false
                }
            }
        } catch (e: Throwable) {
            Logger.writeException(TAG, e)
            false
        }
    }

    /** Register binder + permission listeners. Returns a handle for [cleanup]. */
    fun init(onGranted: () -> Unit, onLost: () -> Unit): ShizukuListeners {
        val binderListener = Shizuku.OnBinderReceivedListener {
            Logger.writeLine(TAG, "Binder received")
            if (hasPermission()) onGranted()
        }
        val permissionListener = object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                Logger.writeLine(TAG, "Permission result: $grantResult")
                if (grantResult == PackageManager.PERMISSION_GRANTED) onGranted()
            }
        }
        // Sticky so a grant that happened before registration still fires.
        try {
            Shizuku.addBinderReceivedListenerSticky(binderListener)
            Shizuku.addRequestPermissionResultListener(permissionListener)
        } catch (e: Throwable) {
            Logger.writeException(TAG, e)
        }
        return ShizukuListeners(binderListener, permissionListener)
    }

    fun cleanup(listeners: ShizukuListeners) {
        try { Shizuku.removeBinderReceivedListener(listeners.binderListener) } catch (_: Throwable) { }
        try { Shizuku.removeRequestPermissionResultListener(listeners.permissionListener) } catch (_: Throwable) { }
    }

    /** Full status snapshot for the Integrações screen. */
    fun status(context: Context): ShizukuStatus {
        val installed = isShizukuInstalled(context)
        val binderAlive = isBinderAlive()
        return ShizukuStatus(
            installed = installed,
            binderAlive = binderAlive,
            permissionGranted = if (installed) hasPermission() else false,
            shouldShowRationale = shouldShowRationale(),
            uid = getUid(),
            version = getVersion()
        )
    }

    /**
     * Run a shell command through Shizuku. Returns null when Shizuku is not ready.
     *
     * Implemented via the AIDL `IShizukuService.newProcess(...)` because
     * `Shizuku.newProcess` is package-private. The returned [IRemoteProcess]
     * ParcelFileDescriptors are wrapped in a [Process] subclass so standard
     * stream/exit APIs work.
     */
    fun runShellCommand(script: String, timeoutMs: Long = 15_000L): ShellResult? {
        if (!isReady()) {
            Logger.writeLine(TAG, "runShellCommand: Shizuku not ready, skipping")
            return null
        }
        return try {
            val binder: IBinder = Shizuku.getBinder()
                ?: return ShellResult(-1, "", "Shizuku binder is null")
            val service = IShizukuService.Stub.asInterface(binder)
                ?: return ShellResult(-1, "", "Failed to resolve IShizukuService")

            val remote: IRemoteProcess = service.newProcess(arrayOf("sh", "-c", script), null, null)
            val process = RemoteProcessWrapper(remote)

            val stdout = StringBuilder()
            val stderr = StringBuilder()
            val outThread = Thread {
                runCatching {
                    BufferedReader(InputStreamReader(process.inputStream)).useLines { l ->
                        l.forEach { stdout.append(it).append('\n') }
                    }
                }
            }
            val errThread = Thread {
                runCatching {
                    BufferedReader(InputStreamReader(process.errorStream)).useLines { l ->
                        l.forEach { stderr.append(it).append('\n') }
                    }
                }
            }
            outThread.start()
            errThread.start()

            val waitThread = Thread { runCatching { process.waitFor() } }
            waitThread.start()
            waitThread.join(timeoutMs)
            val exitCode = if (waitThread.isAlive) {
                waitThread.interrupt()
                runCatching { process.destroy() }
                -1
            } else {
                runCatching { process.exitValue() }.getOrDefault(-1)
            }

            outThread.join(1000)
            errThread.join(1000)

            ShellResult(exitCode, stdout.toString(), stderr.toString())
        } catch (e: Throwable) {
            Logger.writeException(TAG, e)
            ShellResult(-1, "", e.message ?: e.javaClass.simpleName)
        }
    }

    /**
     * Write [content] to [path] via a base64-encoded shell redirect. Base64
     * avoids all quoting/escaping issues with arbitrary JSON content.
     */
    fun writeFile(path: String, content: String): Boolean = writeFileEncoded(path, content)

    /** Convenience: write a file, base64-encoding [content] safely. */
    fun writeFileEncoded(path: String, content: String): Boolean {
        val encoded = java.util.Base64.getEncoder().encodeToString(content.toByteArray())
        val safePath = path.replace("'", "'\\''")
        val result = runShellCommand("echo '$encoded' | base64 -d > '$safePath'", timeoutMs = 20_000L)
            ?: return false
        if (!result.success) {
            Logger.writeLine(TAG, "writeFileEncoded failed: ${result.stderr}")
        }
        return result.success
    }

    /** Read a file's text via Shizuku. Returns null if unavailable. */
    fun readFile(path: String): String? {
        val safePath = path.replace("'", "'\\''")
        val result = runShellCommand("cat '$safePath'", timeoutMs = 15_000L) ?: return null
        return if (result.success) result.stdout else null
    }

    data class ShellResult(val exitCode: Int, val stdout: String, val stderr: String) {
        val success: Boolean get() = exitCode == 0
    }

    class ShizukuListeners(
        val binderListener: Shizuku.OnBinderReceivedListener,
        val permissionListener: Shizuku.OnRequestPermissionResultListener
    )

    /** Minimal [Process] around an [IRemoteProcess]. */
    private class RemoteProcessWrapper(private val remote: IRemoteProcess) : Process() {
        override fun getOutputStream() = FileOutputStream(remote.outputStream.fileDescriptor)
        override fun getInputStream() = FileInputStream(remote.inputStream.fileDescriptor)
        override fun getErrorStream() = FileInputStream(remote.errorStream.fileDescriptor)
        override fun waitFor(): Int = remote.waitFor()
        override fun exitValue(): Int = remote.exitValue()
        override fun destroy() { runCatching { remote.destroy() } }
    }
}
