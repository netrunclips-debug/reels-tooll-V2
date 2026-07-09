package com.example.gesturetiktok.vision

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * GestureDetector: Implements gesture state machine with debounce logic.
 * 
 * Detects the following gestures:
 * - Right eye wink (EAR drops below threshold) -> SWIPE_DOWN
 * - Left eye wink (EAR drops below threshold) -> SWIPE_UP
 * - Mouth wide open (mouth ratio > threshold) -> LIKE
 * - Sustained smile (smile elevation > threshold for ~1 second) -> LIKE
 */
class GestureDetector(
    // Eye aspect ratio (EAR) threshold for detecting a wink
    var eyeAspectRatioThreshold: Float = 0.15f,
    // Mouth aspect ratio threshold for detecting mouth open
    var mouthAspectRatioThreshold: Float = 0.5f,
    // Smile elevation threshold (relative mouth corner height)
    var smileThreshold: Float = 0.2f,
    // Debounce cooldown in milliseconds
    var debounceMs: Long = 1200L,
    // Minimum wink duration in milliseconds (to filter natural blinks)
    var minWinkDurationMs: Long = 150L,
    // Minimum smile duration to trigger Like gesture
    var minSmileDurationMs: Long = 1000L
) {
    companion object {
        private const val TAG = "GestureDetector"
    }

    private val _gestureEvents = MutableSharedFlow<GestureEvent>(extraBufferCapacity = 10)
    val gestureEvents: SharedFlow<GestureEvent> = _gestureEvents

    // Debounce tracking
    private var lastGestureTimeMs = 0L

    // Wink tracking
    private var leftEyeClosedTimeMs = 0L
    private var rightEyeClosedTimeMs = 0L

    // Smile tracking
    private var smileStartTimeMs = 0L
    private var isSmiling = false

    // Temporal smoothing buffers (3-frame rolling average)
    private val leftEarBuffer = FloatArray(3) { 1.0f }
    private val rightEarBuffer = FloatArray(3) { 1.0f }
    private val mouthRatioBuffer = FloatArray(3) { 0.0f }
    private var bufferIndex = 0

    /**
     * Process facial landmarks and detect gestures.
     * Should be called for each video frame.
     * 
     * @param leftEyeAspectRatio Eye aspect ratio for left eye (0-1, lower = more closed)
     * @param rightEyeAspectRatio Eye aspect ratio for right eye
     * @param mouthAspectRatio Mouth openness ratio (0-1, higher = more open)
     * @param smileElevation Smile elevation score (0-1, higher = more smiling)
     * @param currentTimeMs Current system time in milliseconds
     */
    suspend fun processFrame(
        leftEyeAspectRatio: Float,
        rightEyeAspectRatio: Float,
        mouthAspectRatio: Float,
        smileElevation: Float,
        currentTimeMs: Long
    ) {
        // Apply temporal smoothing
        leftEarBuffer[bufferIndex] = leftEyeAspectRatio
        rightEarBuffer[bufferIndex] = rightEyeAspectRatio
        mouthRatioBuffer[bufferIndex] = mouthAspectRatio
        bufferIndex = (bufferIndex + 1) % 3

        val smoothLeftEar = leftEarBuffer.average().toFloat()
        val smoothRightEar = rightEarBuffer.average().toFloat()
        val smoothMouthRatio = mouthRatioBuffer.average().toFloat()

        val timeSinceLastGesture = currentTimeMs - lastGestureTimeMs

        // Debounce check
        if (timeSinceLastGesture < debounceMs) {
            return
        }

        // Detect right eye wink (SWIPE_DOWN)
        if (smoothRightEar < eyeAspectRatioThreshold) {
            if (rightEyeClosedTimeMs == 0L) {
                rightEyeClosedTimeMs = currentTimeMs
            }
            val winkDuration = currentTimeMs - rightEyeClosedTimeMs
            if (winkDuration >= minWinkDurationMs && winkDuration < 500L) {
                // Valid wink detected
                Log.d(TAG, "Right eye wink detected")
                _gestureEvents.emit(GestureEvent.SwipeDown)
                lastGestureTimeMs = currentTimeMs
                rightEyeClosedTimeMs = 0L
            }
        } else {
            rightEyeClosedTimeMs = 0L
        }

        // Detect left eye wink (SWIPE_UP)
        if (smoothLeftEar < eyeAspectRatioThreshold) {
            if (leftEyeClosedTimeMs == 0L) {
                leftEyeClosedTimeMs = currentTimeMs
            }
            val winkDuration = currentTimeMs - leftEyeClosedTimeMs
            if (winkDuration >= minWinkDurationMs && winkDuration < 500L) {
                // Valid wink detected
                Log.d(TAG, "Left eye wink detected")
                _gestureEvents.emit(GestureEvent.SwipeUp)
                lastGestureTimeMs = currentTimeMs
                leftEyeClosedTimeMs = 0L
            }
        } else {
            leftEyeClosedTimeMs = 0L
        }

        // Detect mouth open (LIKE)
        if (smoothMouthRatio > mouthAspectRatioThreshold) {
            Log.d(TAG, "Mouth wide open detected (ratio: $smoothMouthRatio)")
            _gestureEvents.emit(GestureEvent.Like)
            lastGestureTimeMs = currentTimeMs
        }

        // Detect sustained smile (LIKE)
        if (smileElevation > smileThreshold) {
            if (!isSmiling) {
                smileStartTimeMs = currentTimeMs
                isSmiling = true
            }
            val smileDuration = currentTimeMs - smileStartTimeMs
            if (smileDuration >= minSmileDurationMs) {
                Log.d(TAG, "Sustained smile detected (duration: $smileDuration ms)")
                _gestureEvents.emit(GestureEvent.Like)
                lastGestureTimeMs = currentTimeMs
                isSmiling = false
            }
        } else {
            isSmiling = false
        }
    }

    /**
     * Get smoothed facial metrics for debugging.
     */
    fun getSmoothedMetrics(): FacialMetrics {
        return FacialMetrics(
            leftEarBuffer.average().toFloat(),
            rightEarBuffer.average().toFloat(),
            mouthRatioBuffer.average().toFloat()
        )
    }

    data class FacialMetrics(
        val leftEAR: Float,
        val rightEAR: Float,
        val mouthRatio: Float
    )
}
