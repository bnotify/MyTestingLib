package com.example.mycustomlib.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.mycustomlib.socket.SocketManager

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.extras?.get("notification_id")
        val action = intent.getStringExtra("action")
        val click = intent.getBooleanExtra("click", false)

        // Handle the dismiss action here
        // You can log it, send to analytics, etc.
        Log.d("Notification_SocketIO", "Notification ID: $notificationId ACTION: $action CLICKED: $click")
//        SocketManager.handleNotificationIntent(intent)
        if (action == "clicked") {
            // Do something when notification is dismissed
            SocketManager.handleNotificationClickedIntent(intent)
            Log.d("Notification_SocketIO", "Notification CLICKED")
        }else if (action == "dismissed") {
            // Do something when notification is dismissed
            SocketManager.handleNotificationDismissedIntent(intent)
            Log.d("Notification_SocketIO", "Notification DISMISSED")
        }
    }
}