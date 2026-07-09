package com.example.gesturetiktok.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.gesturetiktok.vision.GestureEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * GestureAccessibilityService: Handles performing swipes and taps within TikTok
 * based on gesture events received from the vision module.
 *
 * Must be manually enabled by the user via:
 * Settings > Accessibility > [App Name] > Enable
 */
class GestureAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "GestureAccessibilityService"
        const val ACTION_PERFORM_GESTURE = "com.example.gesturetiktok.ACTION_PERFORM_GESTURE"
        const val EXTRA_GESTURE_TYPE = "gesture_type"
        
        // Gesture type constants (must match with GestureEvent types)
        const val GESTURE_SWIPE_UP = "swipe_up"
        const val GESTURE_SWIPE_DOWN = "swipe_down"
        const val GESTURE_LIKE = "like"
        
        // Gesture parameters
        private const val SWIPE_DURATION_MS = 500L
        private const val SWIPE_DISTANCE = 300f
        private const val TAP_DURATION_MS = 100L
        private const val DOUBLE_TAP_DELAY_MS = 50L
        
        // Singleton instance for receiving gesture events
        private var instance: GestureAccessibilityService? = null
        private val _gestureCommandsReceived = MutableSharedFlow<String>(extraBufferCapacity = 10)
        val gestureCommandsReceived: SharedFlow<String> = _gestureCommandsReceived
        
        fun performGesture(gestureType: String) {
            instance?.let { service ->
                service.coroutineScope.launch {
                    _gestureCommandsReceived.emit(gestureType)
                }
            }
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service connected")
        instance = this
        
        // Start listening for gesture events
        startGestureListener()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not actively used in this implementation
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility Service destroyed")
        instance = null
        coroutineScope.launch {
            // Cleanup if needed
        }
    }

    private fun startGestureListener() {
        coroutineScope.launch {
            gestureCommandsReceived.collect { gestureType ->
                when (gestureType) {
                    GESTURE_SWIPE_DOWN -> performSwipeDown()
                    GESTURE_SWIPE_UP -> performSwipeUp()
                    GESTURE_LIKE -> performDoubleTap()
                    else -> Log.w(TAG, "Unknown gesture type: $gestureType")
                }
            }
        }
    }

    /**
     * Perform a downward swipe (next video).
     */
    private fun performSwipeDown() {
        try {
            Log.d(TAG, "Performing swipe down")
            val screenHeight = resources.displayMetrics.heightPixels
            val screenWidth = resources.displayMetrics.widthPixels
            
            val startX = screenWidth / 2f
            val startY = screenHeight / 3f
            val endX = screenWidth / 2f
            val endY = screenHeight * 2 / 3f
            
            val gesture = buildSwipeGesture(startX, startY, endX, endY, SWIPE_DURATION_MS)
            dispatchGesture(gesture, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing swipe down: ${e.message}")
        }
    }

    /**
     * Perform an upward swipe (previous video).
     */
    private fun performSwipeUp() {
        try {
            Log.d(TAG, "Performing swipe up")
            val screenHeight = resources.displayMetrics.heightPixels
            val screenWidth = resources.displayMetrics.widthPixels
            
            val startX = screenWidth / 2f
            val startY = screenHeight * 2 / 3f
            val endX = screenWidth / 2f
            val endY = screenHeight / 3f
            
            val gesture = buildSwipeGesture(startX, startY, endX, endY, SWIPE_DURATION_MS)
            dispatchGesture(gesture, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing swipe up: ${e.message}")
        }
    }

    /**
     * Perform a double tap to like the video.
     */
    private fun performDoubleTap() {
        try {
            Log.d(TAG, "Performing double tap (like)")
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            
            // Double tap on the right side of the screen (where the like heart appears in TikTok)
            val tapX = screenWidth - 80f
            val tapY = screenHeight / 2f
            
            // First tap
            val firstTap = buildTapGesture(tapX, tapY, TAP_DURATION_MS)
            dispatchGesture(firstTap, object : AccessibilityService.GestureResultCallback() {
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Log.w(TAG, "First tap cancelled")
                }
            })
            
            // Second tap after a short delay
            coroutineScope.launch(Dispatchers.Main) {
                kotlinx.coroutines.delay(DOUBLE_TAP_DELAY_MS)
                val secondTap = buildTapGesture(tapX, tapY, TAP_DURATION_MS)
                dispatchGesture(secondTap, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing double tap: ${e.message}")
        }
    }

    /**
     * Build a swipe gesture description.
     */
    private fun buildSwipeGesture(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long
    ): GestureDescription {
        val path = GestureDescription.StrokeDescription(
            createPath(startX, startY, endX, endY),
            0,
            durationMs
        )
        return GestureDescription.Builder().apply {
            addStroke(path)
        }.build()
    }

    /**
     * Build a tap gesture description.
     */
    private fun buildTapGesture(x: Float, y: Float, durationMs: Long): GestureDescription {
        val path = GestureDescription.StrokeDescription(
            createPath(x, y, x, y),
            0,
            durationMs
        )
        return GestureDescription.Builder().apply {
            addStroke(path)
        }.build()
    }

    /**
     * Create a Path object for gesture strokes.
     * Note: GestureDescription.StrokeDescription requires an android.graphics.Path
     */
    private fun createPath(startX: Float, startY: Float, endX: Float, endY: Float): android.graphics.Path {
        return android.graphics.Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
    }
}
