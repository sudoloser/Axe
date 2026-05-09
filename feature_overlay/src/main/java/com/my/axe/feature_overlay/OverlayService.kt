package com.my.axe.feature_overlay

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.my.axe.feature_rpc_base.Constants
import com.my.axe.feature_rpc_base.detection.ShizukuDetectionStrategy
import com.my.axe.feature_rpc_base.detection.UsageStatsDetectionStrategy
import com.my.axe.preference.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class OverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var isExpanded by mutableStateOf(false)

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 0
        y = 300
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopSelf()
            return START_NOT_STICKY
        }

        showOverlay()
        startForeground(Constants.NOTIFICATION_ID + 100, createNotification())
        monitorForegroundApp()
        
        return START_STICKY
    }

    private fun showOverlay() {
        if (composeView != null) return

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            
            setContent {
                Box(modifier = Modifier.fillMaxSize()) {
                    FloatingAxeButton(
                        onExpand = { 
                            isExpanded = true
                            updateWindow()
                        },
                        onDrag = { dx, dy ->
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            windowManager.updateViewLayout(this@apply, params)
                        }
                    )
                    
                    if (isExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            OverlayMenu(onDismiss = { 
                                isExpanded = false
                                updateWindow()
                            })
                        }
                    }
                }
            }
        }

        windowManager.addView(composeView, params)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    private fun updateWindow() {
        composeView?.let { view ->
            if (isExpanded) {
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.MATCH_PARENT
                params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            } else {
                params.width = WindowManager.LayoutParams.WRAP_CONTENT
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            }
            windowManager.updateViewLayout(view, params)
        }
    }

    private fun monitorForegroundApp() {
        serviceScope.launch {
            while (isActive) {
                val isSystemWide = Prefs[Prefs.OVERLAY_SYSTEM_WIDE, false]
                if (!isSystemWide) {
                    val packageName = getForegroundPackage()
                    val isWhitelisted = Prefs.isOverlayWhitelisted(packageName)
                    
                    withContext(Dispatchers.Main) {
                        composeView?.visibility = if (isWhitelisted) View.VISIBLE else View.GONE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        composeView?.visibility = View.VISIBLE
                    }
                }
                delay(2000)
            }
        }
    }

    private fun getForegroundPackage(): String? {
        val useShizuku = Prefs[Prefs.USE_SHIZUKU, false]
        return if (useShizuku) {
            ShizukuDetectionStrategy().getForegroundApp()
        } else {
            UsageStatsDetectionStrategy(this).getForegroundApp()
        }
    }

    private fun createNotification(): Notification {
        val channelId = Constants.CHANNEL_ID
        return Notification.Builder(this, channelId)
            .setContentTitle("Axe Overlay Active")
            .setSmallIcon(com.my.axe.resources.R.drawable.ic_apps)
            .build()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        composeView?.let { 
            if (it.parent != null) windowManager.removeView(it) 
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
