package com.example.mycustomlib.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mycustomlib.network.NetworkMonitor
import com.example.mycustomlib.network.NetworkState
import com.example.mycustomlib.network.NetworkStateListener
import com.example.mycustomlib.network.NetworkType
import com.example.mycustomlib.socket.SocketManager
import kotlin.apply
import kotlin.jvm.java
import kotlin.let
import androidx.core.graphics.createBitmap


class PersistentMessagingService : Service(), NetworkStateListener {
//    private var webSocketClient: WebSocketClient? = null
    private var wakeLock: WakeLock? = null
    private lateinit var networkMonitor: NetworkMonitor
    private var isRestarting = false
    private val NOTIFICATION_ID: Int = 1234
    private val CHANNEL_ID: String = "messaging_channel"


    companion object{
        fun restartService(context: Context) {
            val intent = Intent(context, PersistentMessagingService::class.java).apply {
                action = "ACTION_RESTART"
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        networkMonitor = NetworkMonitor.getInstance(applicationContext)
        networkMonitor.addListener(this)
        networkMonitor.startMonitoring()
        acquireWakeLock()
        createNotificationChannel()
        startForegroundWithHiddenNotification()
        if (!SocketManager.isConnected() && !SocketManager.isConnecting()) {
            startWebSocketConnection()
        }else{
            Log.d("SocketManager", "PersistentMessagingService onCreate isConnected: ${SocketManager.isConnected()} isConnecting: ${SocketManager.isConnecting()}")
        }
    }

    override fun onNetworkStateChanged(state: NetworkState) {
        when (state) {

            is NetworkState.Connected -> {
                if (!SocketManager.isConnected()) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!SocketManager.isConnected()) {
                            restartService()
                        }
                    }, 30000) // Restart if not connected after 30 seconds
                }
                when (state.type) {
                    NetworkType.WIFI -> {
                        Log.d("SocketIO", "Connected to WiFi - reconnecting socket")
                        SocketManager.reconnect()
                    }
                    NetworkType.CELLULAR -> {
                        Log.d("SocketIO", "Connected to Cellular - reconnecting socket")
                        SocketManager.reconnect()
                    }
                }
            }
            NetworkState.Disconnected -> {
                Log.d("SocketIO", "No internet connection - disconnecting socket")
                SocketManager.disconnect()
            }
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MessagingWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Messaging Service",
                NotificationManager.IMPORTANCE_MIN
            )
            channel.description = "Background messaging service"
            channel.setShowBadge(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            channel.setSound(null, null)

            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
    }

    /*private fun startForegroundWithHiddenNotification() {
        // Create empty notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_transparent)
            .setContentTitle("")
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setOngoing(true)

        // Start foreground service with appropriate type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFICATION_ID, builder.build(),
                FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING or
                        FOREGROUND_SERVICE_TYPE_DATA_SYNC or FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, builder.build())
        }

        // Hide the notification if possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID, builder.build())
            manager.cancel(NOTIFICATION_ID)
        }
    }*/
    private fun startForegroundWithHiddenNotification() {
        // Create empty notification
        val appName = getAppName(this)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(getAppLauncherIconResId(this))
            .setContentTitle("${appName}")
            .setContentText("Checking New Notifications...")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setOngoing(true)

        // Start foreground service with version-specific type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            startForeground(
                NOTIFICATION_ID,
                builder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING or
                        FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30)
            startForeground(
                NOTIFICATION_ID,
                builder.build(),
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            // Android 10 and below
            startForeground(NOTIFICATION_ID, builder.build())
        }

        // Hide the notification if possible (optional)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID, builder.build())
            manager.cancel(NOTIFICATION_ID)
        }
    }


    private fun startWebSocketConnection() {
        // Create WebSocket client here
        SocketManager.initialize(applicationContext)
        SocketManager.connect()

    }

    private fun scheduleReconnect() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // reconnect socket here
            SocketManager.reconnect()
        }, 5000) // 5 seconds delay before reconnect
    }

    private fun restartService() {
        isRestarting = true
        val restartIntent = Intent(this, PersistentMessagingService::class.java)
        restartIntent.action = "ACTION_RESTART"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
        stopSelf()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle service restart
        if (intent != null && "ACTION_RESTART" == intent.action) {
            Log.d("PersistentService", "Service restarted - reconnecting socket")
            if (!SocketManager.isConnected() && !SocketManager.isConnecting()) {
                startWebSocketConnection()
            }
            intent?.let { SocketManager.handleNotificationClickedIntent(it) }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only disconnect if the service is really stopping (not just restarting)
        if (!isRestarting) {
            SocketManager.disconnect()
        }
        networkMonitor.removeListener(this)
        if (wakeLock != null && wakeLock!!.isHeld) {
            wakeLock!!.release()
        }


        // Schedule service restart
//        val restartIntent = Intent(
//            this,
//            PersistentMessagingService::class.java
//        )
//        restartIntent.setAction("ACTION_RESTART")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(restartIntent)
//        } else {
//            startService(restartIntent)
//        }
        restartService() // This will properly restart the service
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun getAppName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) {
            applicationInfo.nonLocalizedLabel.toString()
        } else {
            context.getString(stringId)
        }
    }

    fun getAppLauncherIconResId(context: Context): Int {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
        return applicationInfo.icon // this is a resource ID
    }

    fun getAppIcon(context: Context): Drawable? {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            packageManager.getApplicationIcon(applicationInfo)
        } catch (e: Exception) {
            null
        }
    }

    fun getAppIconBitmap(context: Context): Bitmap? {
        val drawable = getAppIcon(context) ?: return null
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = createBitmap(drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 1)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

}