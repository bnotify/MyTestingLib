package com.example.mycustomlib.receiver

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.mycustomlib.BerryNotifier
import com.example.mycustomlib.services.PersistentMessagingService
import kotlin.jvm.java


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Check if service is running
        if (!isServiceRunning(context, PersistentMessagingService::class.java)) {
            val serviceIntent = Intent(
                context,
                PersistentMessagingService::class.java
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }


        // Reschedule the alarm
//        val app = context.applicationContext as MyApplication
        BerryNotifier.scheduleAlarmManager(context.applicationContext)
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}