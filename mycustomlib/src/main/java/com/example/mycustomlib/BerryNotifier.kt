package com.example.mycustomlib

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.mycustomlib.activities.PermissionRequestActivity
import com.example.mycustomlib.receiver.AlarmReceiver
import com.example.mycustomlib.receiver.NotificationDismissReceiver
import com.example.mycustomlib.services.PersistentMessagingService
import org.json.JSONObject
import kotlin.apply
import kotlin.jvm.java

object BerryNotifier {
    private const val PERMISSION_REQUEST_CODE = 1001
    private var activityClass: Class<out Activity>? = null

    private var notification_listener:OnNotificationListener? = null

    fun setActivityToOpenOnClick(activity: Class<out Activity>){
        this.activityClass = activity
    }

    internal fun getActivityToOpenOnClick(): Class<out Activity>{
        return activityClass!!
    }

    fun setNotificationListener(listener: OnNotificationListener){
        this.notification_listener = listener
    }

    internal fun getNotificationListener(): OnNotificationListener? {
        return this.notification_listener
    }

    fun NotificationInitializer(activityContext: Context,intent: Intent) {
        if (ContextCompat.checkSelfPermission(
                activityContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkAndRequestExactAlarmPermission(activityContext)
            }

            if (intent.extras != null){
                val action = intent.getStringExtra("action")
                val type = intent.getStringExtra("type")
                val notification_id = intent.getStringExtra("notification_id")

                Log.i("Notification_SocketIO", "CLICKED Notification ID: $notification_id ACTION: $action TYPE: $type")
                val receiverIntent = Intent(activityContext, NotificationDismissReceiver::class.java).apply {
                    putExtra("notification_id", notification_id ?: 0)
                    putExtra("action", "clicked") // Action when dismissed
                    putExtra("type", type ?: null)
                    putExtra("click", true)
                }
                activityContext.sendBroadcast(receiverIntent)
            }else{
                Log.i("Notification_SocketIO", "No notification data")
            }
        } else {
            // Start permission activity
            val intent = Intent(activityContext, PermissionRequestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("request_code", PERMISSION_REQUEST_CODE)
            }
            activityContext.startActivity(intent)
        }

    }

    fun startPersistentService(applicationContext: Context) {
        val serviceIntent = Intent(
            applicationContext,
            PersistentMessagingService::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent)
        } else {
            applicationContext.startService(serviceIntent)
        }
    }

    fun scheduleAlarmManager(applicationContext: Context) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val interval = AlarmManager.INTERVAL_HOUR
        val triggerAtMillis = System.currentTimeMillis() + interval

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires special handling
            if (alarmManager.canScheduleExactAlarms()) {
                // We have permission
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // Request permission
                requestExactAlarmPermission(applicationContext)
                // Fall back to inexact alarm
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis,pendingIntent)
//                alarmManager.setAndAllowWhileIdle(
//                    AlarmManager.RTC_WAKEUP,
//                    triggerAtMillis,
//                    pendingIntent
//                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            // Older versions
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAndRequestExactAlarmPermission(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            AlertDialog.Builder(context)
                .setTitle("Exact Alarms Required")
                .setMessage("This app needs exact alarm permission to function properly")
                .setPositiveButton("Grant Permission") { _, _ ->
                    requestExactAlarmPermission(context)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission(applicationContext: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            applicationContext.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Handle case where the settings activity doesn't exist
            Log.e("Alarm", "Could not request exact alarm permission", e)
        }
    }


    interface OnNotificationListener{
        fun onMessageReceive(data: JSONObject?)
    }
}