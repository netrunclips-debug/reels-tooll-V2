package com.example.gesturetiktok.overlay

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.gesturetiktok.R
import com.example.gesturetiktok.util.NotificationHelper

/**
 * OverlayService: Foreground service that displays a floating overlay showing
 * camera feed status and detected gestures while TikTok is in the foreground.
 *
 * This ensures the gesture recognition continues even when the app is not actively displayed.
 */
class OverlayService : Service() {

    companion object {
        private const val TAG = "OverlayService"
        private const val NOTIFICATION_ID = 1
    }

    private var overlayView: OverlayView? = null
    private var windowManager: WindowManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OverlayService created")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "OverlayService started")
        
        // Create foreground notification
        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setContentTitle("Gesture Recognition Active")
            .setContentText("Gesture TikTok is monitoring facial gestures")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        // Start as foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Create and add overlay view
        createOverlay()

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "OverlayService destroyed")
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createOverlay() {
        if (overlayView == null && windowManager != null) {
            overlayView = OverlayView(this)
            val params = WindowManager.LayoutParams().apply {
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                width = 120
                height = 120
                x = 20
                y = 100
            }

            try {
                windowManager?.addView(overlayView, params)
                Log.d(TAG, "Overlay view added to window")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding overlay view: ${e.message}")
            }
        }
    }

    private fun removeOverlay() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager?.removeView(overlayView)
                overlayView = null
                Log.d(TAG, "Overlay view removed")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view: ${e.message}")
            }
        }
    }

    fun updateGestureLabel(label: String) {
        overlayView?.updateGestureLabel(label)
    }

    fun updateFaceDetection(detected: Boolean) {
        overlayView?.updateFaceDetection(detected)
    }
}
