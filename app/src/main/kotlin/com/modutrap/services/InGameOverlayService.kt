package com.modutrap.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Minimize
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.modutrap.BloxstrapMobileApp
import com.modutrap.MainActivity
import com.modutrap.R
import com.modutrap.core.AppContainer
import com.modutrap.personalization.ThemeManager
import com.modutrap.ui.theme.BloxstrapMobileTheme
import kotlinx.coroutines.launch

/**
 * Floating in-game overlay for ModuTrap.
 *
 * Hosts a Compose window as a [android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY]
 * on top of the Roblox game, so the menu lives *inside* the game instead of
 * behind a separate app screen. The overlay is:
 *
 *  - **Draggable** — drag the header (or the minimized pill) to reposition it.
 *  - **Minimizable** — the minimize button collapses the card to a small pill.
 *  - **Direct** — quick toggles and an "apply" action write settings and
 *    re-inject `ClientAppSettings.json` (via Shizuku) so changes take effect
 *    immediately in the running game, plus a "relaunch" button.
 *
 * Runs as a foreground service so the window survives while Roblox is in the
 * foreground. Requires the `SYSTEM_ALERT_WINDOW` permission, granted through
 * the "Menu no jogo" card on the Home screen.
 */
class InGameOverlayService : LifecycleService(), SavedStateRegistryOwner {

    private val savedStateRegistryController: SavedStateRegistryController by lazy {
        SavedStateRegistryController.create(this)
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    /** Window position, in pixels, relative to the top-left of the screen. */
    private var windowX: Int = -1
    private var windowY: Int = -1

    /** Whether the card is expanded (full menu) or minimized (small pill). */
    private var expanded by mutableStateOf(true)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForegroundCompat()
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (overlayView == null) showOverlay()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        layoutParams = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ---- Window management ------------------------------------------------

    private fun showOverlay() {
        val container: AppContainer = (application as BloxstrapMobileApp).container
        val view = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                InGameOverlayContent(
                    container = container,
                    expanded = expanded,
                    onExpand = { expanded = true },
                    onMinimize = { expanded = false },
                    onDismiss = { stopSelf() },
                    onMove = { dx, dy -> moveWindow(dx, dy) }
                )
            }
            // Bind the three ViewTree owners Compose needs inside a Service.
            this.setViewTreeLifecycleOwner(this@InGameOverlayService)
            this.setViewTreeViewModelStoreOwner(this@InGameOverlayService)
            this.setViewTreeSavedStateRegistryOwner(this@InGameOverlayService)
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (windowX < 0 || windowY < 0) {
                // Center-top of the screen by default.
                windowX = resources.displayMetrics.widthPixels / 3
                windowY = 120
            }
            x = windowX
            y = windowY
        }

        overlayView = view
        layoutParams = params
        windowManager.addView(view, params)
    }

    private fun moveWindow(dx: Int, dy: Int) {
        val view = overlayView ?: return
        val params = layoutParams ?: return
        windowX += dx
        windowY += dy
        params.x = windowX
        params.y = windowY
        windowManager.updateViewLayout(view, params)
    }

    // ---- Foreground notification -----------------------------------------

    private fun startForegroundCompat() {
        val channelId = "modutrap_overlay"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Menu no jogo",
                NotificationManager.IMPORTANCE_LOW
            )
        )
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Menu no jogo ativo")
            .setContentText("ModuTrap sobrepondo o Roblox — arraste para mover")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this, NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        /** Start the overlay service (as a foreground service on O+). */
        fun start(context: Context) {
            val intent = Intent(context.applicationContext, InGameOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context.applicationContext, InGameOverlayService::class.java))
        }

        fun isRunning(context: Context): Boolean {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            return am.getRunningServices(Integer.MAX_VALUE)
                .any { it.service.className == InGameOverlayService::class.java.name }
        }
    }
}

// ===========================================================================
// Overlay UI
// ===========================================================================

/**
 * The floating menu. When [expanded] it shows the full control card; when
 * minimized it collapses to a compact pill that can still be dragged and
 * tapped to re-expand.
 */
@Composable
private fun InGameOverlayContent(
    container: AppContainer,
    expanded: Boolean,
    onExpand: () -> Unit,
    onMinimize: () -> Unit,
    onDismiss: () -> Unit,
    onMove: (Int, Int) -> Unit
) {
    val settings by container.settings.settings.collectAsState()
    val accent = ThemeManager.parseColor(settings.accentColor) ?: Color(0xFF8B5CF6)
    val scope = rememberCoroutineScope()

    BloxstrapMobileTheme(accentColor = accent, darkTheme = true) {
        if (expanded) {
            InGameMenuCard(
                container = container,
                settings = settings,
                onMinimize = onMinimize,
                onDismiss = onDismiss,
                onMove = onMove,
                onToggle = { key, value ->
                    scope.launch {
                        container.settings.put(key, value)
                        applyInGame(container)
                    }
                },
                onApplyNow = {
                    scope.launch { applyInGame(container) }
                },
                onRelaunch = {
                    scope.launch { container.launcher.launch() }
                }
            )
        } else {
            InGameMinimizedPill(onExpand = onExpand, onMove = onMove)
        }
    }
}

/** Re-inject ClientAppSettings.json into the running game (best-effort). */
private suspend fun applyInGame(container: AppContainer) {
    try {
        val settings = container.settings.settings.value
        val pkg = container.roblox.resolvePackage(settings.preferredPackage, settings.customPackage)
        val json = container.flagApplier.buildClientAppSettings(settings)
        container.flagApplier.inject(json, container.roblox.dataDir(pkg))
    } catch (_: Exception) {
        // Best-effort: never crash the overlay.
    }
}

@Composable
private fun InGameMenuCard(
    container: AppContainer,
    settings: com.modutrap.settings.AppSettings,
    onMinimize: () -> Unit,
    onDismiss: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onToggle: (androidx.datastore.preferences.core.Preferences.Key<Boolean>, Boolean) -> Unit,
    onApplyNow: () -> Unit,
    onRelaunch: () -> Unit
) {
    // Local drag offset, accumulated from gesture deltas and reported to the
    // service so the real window moves with the content.
    var dragOffset by remember { mutableStateOf(IntOffset.Zero) }

    Card(
        modifier = Modifier
            .width(320.dp)
            .pointerInput(Unit) {
                detectDragGestures { _, drag ->
                    dragOffset = IntOffset(
                        (dragOffset.x + drag.x.roundToInt()),
                        (dragOffset.y + drag.y.roundToInt())
                    )
                    onMove(drag.x.roundToInt(), drag.y.roundToInt())
                }
            }
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xCC121219),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header (drag handle + actions).
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DragHandle,
                        contentDescription = "Arrastar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "ModuTrap",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onMinimize, contentDescription = "Minimizar") {
                    Icon(Icons.Outlined.Minimize, contentDescription = null)
                }
                IconButton(onClick = onDismiss, contentDescription = "Fechar") {
                    Icon(Icons.Outlined.Close, contentDescription = null)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Quick toggles.
            ToggleRow(
                label = "Sombras",
                checked = settings.shadowsEnabled,
                onToggle = { onToggle(com.modutrap.settings.SettingsRepository.Keys.shadowsEnabled, it) }
            )
            ToggleRow(
                label = "Pós-processamento",
                checked = settings.postFxEnabled,
                onToggle = { onToggle(com.modutrap.settings.SettingsRepository.Keys.postFxEnabled, it) }
            )
            ToggleRow(
                label = "Texturas de terreno",
                checked = settings.terrainTextures,
                onToggle = { onToggle(com.modutrap.settings.SettingsRepository.Keys.terrainTextures, it) }
            )
            ToggleRow(
                label = "Resolução dinâmica",
                checked = settings.dynamicResolution,
                onToggle = { onToggle(com.modutrap.settings.SettingsRepository.Keys.dynamicResolution, it) }
            )
            ToggleRow(
                label = "Otimização GC",
                checked = settings.enableGCOptimization,
                onToggle = { onToggle(com.modutrap.settings.SettingsRepository.Keys.enableGCOptimization, it) }
            )
            Spacer(Modifier.height(12.dp))

            // Action buttons.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onApplyNow,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Aplicar agora") }
                OutlinedButton(
                    onClick = onRelaunch,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reiniciar")
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

/** Minimized compact pill: still draggable, tap to expand. */
@Composable
private fun InGameMinimizedPill(onExpand: () -> Unit, onMove: (Int, Int) -> Unit) {
    var dragOffset by remember { mutableStateOf(IntOffset.Zero) }
    Surface(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { _, drag ->
                    dragOffset = IntOffset(
                        (dragOffset.x + drag.x.roundToInt()),
                        (dragOffset.y + drag.y.roundToInt())
                    )
                    onMove(drag.x.roundToInt(), drag.y.roundToInt())
                }
            }
            .clip(RoundedCornerShape(28.dp))
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xEE8B5CF6),
        contentColor = Color.White,
        onClick = onExpand
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.DragHandle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("ModuTrap", style = MaterialTheme.typography.labelLarge)
        }
    }
}
