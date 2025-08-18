package com.example.mycustomlib.notification

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.mycustomlib.BerryNotifier
import com.example.mycustomlib.model.NotificationModel
import com.example.mycustomlib.receiver.NotificationDismissReceiver
import com.example.mycustomlib.socket.SocketManager
import java.util.Random
import kotlin.apply
import kotlin.jvm.java
import kotlin.text.isNullOrBlank
import kotlin.text.toIntOrNull

object NotificationsManager {
    private lateinit var notificationManager: NotificationManager
    private var isInitialized = false
    private const val CHANNEL_ID = "custom_notifications_channel"

    fun init(context: Context) {
        if (!isInitialized) {
            notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(context)
            isInitialized = true
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getAppName(context),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "App notifications"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun handleNow(model: NotificationModel, context: Context) {
        if (!isInitialized) init(context)

        val notificationId = model.notificationId?.toIntOrNull() ?: Random().nextInt(10000)
        val notificationBuilder = buildBaseNotification(model, context, notificationId)

        if (!model.imageUrl.isNullOrBlank()) {
            loadImageForNotification(model, notificationBuilder, notificationId,true, context)

        } else {
            showNotification(notificationBuilder.build(), notificationId,model)
        }

    }


    private fun buildBaseNotification(
        model: NotificationModel,
        context: Context,
        notificationId: Int,
    ): NotificationCompat.Builder {

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            Log.i("Notification_SocketIO","PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE ${PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE}")
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
//            Log.i("Notification_SocketIO","FLAG_ONE_SHOT ${PendingIntent.FLAG_ONE_SHOT}")
        }

//        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        } else {
//            PendingIntent.FLAG_ONE_SHOT
//        }

//        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        } else {
//            PendingIntent.FLAG_UPDATE_CURRENT
//        }
        // For the main click action (when notification is clicked)
        val clickIntent = Intent(context, BerryNotifier.getActivityToOpenOnClick()).apply {
            putExtra("from", "notification")
            putExtra("action", "clicked") // Action when clicked
            putExtra("notification_id", model.notificationId ?: 0)
            putExtra("type", model.type ?: null)
            putExtra("click", true)
//            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val clickPendingIntent = PendingIntent.getActivity(
            context,
            model.notificationId?.toIntOrNull() ?: System.currentTimeMillis().toInt(),
            clickIntent,
            pendingIntentFlags
        )

// For the dismiss action (when notification is dismissed)
        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra("notification_id", model.notificationId ?: 0)
            putExtra("action", "dismissed") // Action when dismissed
            putExtra("type", model.type ?: null)
            putExtra("click", false)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (model.notificationId?.toIntOrNull() ?: System.currentTimeMillis().toInt()) + 1, // Different request code
            dismissIntent,
            pendingIntentFlags
        )
        //==========================================================================================

        Log.d("Notification_SocketIO", "Notification ACTION: ${model.action}")
        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setAutoCancel(true)
            setDefaults(Notification.DEFAULT_ALL)
            setWhen(System.currentTimeMillis())
            setSmallIcon(getAppLauncherIconResId(context))
            setLargeIcon(getAppIconBitmap(context))
            setContentTitle(model.title)
            setContentText(model.message)
            setContentIntent(clickPendingIntent)
            setDeleteIntent(dismissPendingIntent) // Set dismiss intent
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setCategory(NotificationCompat.CATEGORY_MESSAGE)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setShowWhen(true)
        }
    }

    private fun loadImageForNotification(
        model: NotificationModel,
        builder: NotificationCompat.Builder,
        notificationId: Int,
        isExpanded: Boolean,
        context: Context
    ) {
        Glide.with(context)
            .asBitmap()
            .load(model.imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    if (isExpanded){
                        builder.setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(resource)
                                .setBigContentTitle(model.title)
                                .setSummaryText(model.message)
                                .bigLargeIcon(getAppIconBitmap(context))
                        )
                    }else{
                        // Initial compact view (no expanded image)
                        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    }

                    showNotification(builder.build(), notificationId, model)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    showNotification(builder.build(), notificationId, model)
                }
            })
    }

    private fun showNotification(notification: Notification, notificationId: Int, model: NotificationModel) {
        try {
            notificationManager.notify(notificationId, notification)
            SocketManager.handleNotificationReceived(model)
            Log.d("NotificationsManager", "Notification shown with ID: $notificationId")
        } catch (e: Exception) {
            Log.e("NotificationsManager", "Error showing notification", e)
        }
    }

    private fun getBitmapFromMipmap(context: Context, mipmapResId: Int): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, mipmapResId) as? BitmapDrawable
        return drawable?.bitmap
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