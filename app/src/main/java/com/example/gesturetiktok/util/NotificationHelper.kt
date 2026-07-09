package com.example.gesturetiktok.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * NotificationHelper: Creates notification channels for the app.
 */
object NotificationHelper {

    const val CHANNEL_ID = "gesture_tiktok_channel"
    const val CHANNEL_NAME = "Gesture Recognition"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for gesture recognition activity"
                enableLights(false)
                enableVibration(false)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }
}
