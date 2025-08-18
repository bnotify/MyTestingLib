package com.example.mycustomlib.socket

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlin.let

class SocketService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        SocketManager.initialize(applicationContext)
        SocketManager.connect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { SocketManager.handleNotificationClickedIntent(it) }
        return START_STICKY
    }

    override fun onDestroy() {
        SocketManager.disconnect()
        super.onDestroy()
    }
}