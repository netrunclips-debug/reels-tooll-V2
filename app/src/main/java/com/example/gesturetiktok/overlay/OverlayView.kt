package com.example.gesturetiktok.overlay

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import com.example.gesturetiktok.R

/**
 * OverlayView: Floating overlay widget displayed on top of other apps.
 * Shows face detection status and currently detected gesture labels.
 */
class OverlayView(context: Context) : FrameLayout(context) {

    companion object {
        private const val TAG = "OverlayView"
    }

    private var gestureLabel: TextView? = null
    private var faceDetected = false

    init {
        try {
            setBackgroundColor(android.graphics.Color.parseColor("#CC6200EE"))
            
            gestureLabel = TextView(context).apply {
                text = "Ready"
                textSize = 10f
                setTextColor(android.graphics.Color.WHITE)
                gravity = android.view.Gravity.CENTER
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            }
            
            gestureLabel?.let { addView(it) }
            Log.d(TAG, "OverlayView initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing OverlayView: ${e.message}")
        }
    }

    fun updateGestureLabel(label: String) {
        gestureLabel?.apply {
            text = label
            alpha = 1.0f
            
            // Fade out after 500ms
            postDelayed({
                alpha = 0.3f
            }, 500)
        }
    }

    fun updateFaceDetection(detected: Boolean) {
        faceDetected = detected
        gestureLabel?.apply {
            if (detected) {
                setTextColor(android.graphics.Color.GREEN)
                if (text.isEmpty() || text == "No Face") {
                    text = "●"
                }
            } else {
                setTextColor(android.graphics.Color.YELLOW)
                text = "No Face"
            }
        }
    }
}
