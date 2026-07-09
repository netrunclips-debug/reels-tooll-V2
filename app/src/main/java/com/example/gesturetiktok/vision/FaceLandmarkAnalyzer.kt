package com.example.gesturetiktok.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.Executor
import kotlin.math.hypot

/**
 * FaceLandmarkAnalyzer: CameraX ImageAnalysis.Analyzer that detects facial landmarks using MediaPipe.
 *
 * Extracts:
 * - Eye aspect ratio (EAR) for wink detection
 * - Mouth aspect ratio for mouth-open detection
 * - Smile elevation for smile detection
 * - Face detected status
 */
class FaceLandmarkAnalyzer(
    private val context: Context,
    private val executor: Executor
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "FaceLandmarkAnalyzer"
        
        // MediaPipe face landmark indices
        private const val LEFT_EYE_LEFT = 33
        private const val LEFT_EYE_TOP = 159
        private const val LEFT_EYE_RIGHT = 133
        private const val LEFT_EYE_BOTTOM = 145
        
        private const val RIGHT_EYE_LEFT = 263
        private const val RIGHT_EYE_TOP = 386
        private const val RIGHT_EYE_RIGHT = 362
        private const val RIGHT_EYE_BOTTOM = 374
        
        private const val MOUTH_LEFT = 61
        private const val MOUTH_RIGHT = 291
        private const val MOUTH_TOP = 13
        private const val MOUTH_BOTTOM = 14
        
        private const val LEFT_MOUTH_CORNER = 88
        private const val RIGHT_MOUTH_CORNER = 318
        
        // Typical resting EAR (for normalization)
        private const val RESTING_EAR = 0.4f
    }

    private var faceLandmarker: FaceLandmarker? = null
    private val _faceDetectionEvents = MutableSharedFlow<FaceDetectionData>(extraBufferCapacity = 10)
    val faceDetectionEvents: SharedFlow<FaceDetectionData> = _faceDetectionEvents

    private var lastProcessedTimeMs = 0L
    private var faceDetected = false

    init {
        initializeFaceLandmarker()
    }

    private fun initializeFaceLandmarker() {
        executor.execute {
            try {
                val baseOptions = BaseOptions.builder()
                    .setModelAssetPath("face_landmarker_v2_with_blendshapes.task")
                    .build()

                val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setResultListener { result: FaceLandmarkerResult, imageProxyHeight: Long ->
                        processFaceLandmarks(result)
                    }
                    .setErrorListener { error: RuntimeException ->
                        Log.e(TAG, "MediaPipe error: ${error.message}")
                    }
                    .build()

                faceLandmarker = FaceLandmarker.createFromOptions(context, options)
                Log.d(TAG, "FaceLandmarker initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize FaceLandmarker: ${e.message}")
            }
        }
    }

    override fun analyze(imageProxy: ImageProxy) {
        val analyzer = faceLandmarker ?: run {
            imageProxy.close()
            return
        }

        try {
            val bitmap = imageProxy.toBitmap()?.rotate(imageProxy.imageInfo.rotationDegrees)
                ?: run {
                    imageProxy.close()
                    return
                }

            val mediaImage = BitmapImageBuilder(bitmap).build()
            val currentTimeMs = System.currentTimeMillis()
            
            analyzer.detectAsync(mediaImage, currentTimeMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing frame: ${e.message}")
        } finally {
            imageProxy.close()
        }
    }

    private fun processFaceLandmarks(result: FaceLandmarkerResult) {
        if (result.faceLandmarks().isEmpty()) {
            faceDetected = false
            return
        }

        faceDetected = true
        val landmarks = result.faceLandmarks()[0]
        
        if (landmarks.isEmpty()) {
            faceDetected = false
            return
        }

        try {
            // Calculate eye aspect ratios
            val leftEAR = calculateEyeAspectRatio(
                landmarks[LEFT_EYE_LEFT],
                landmarks[LEFT_EYE_TOP],
                landmarks[LEFT_EYE_RIGHT],
                landmarks[LEFT_EYE_BOTTOM]
            )

            val rightEAR = calculateEyeAspectRatio(
                landmarks[RIGHT_EYE_LEFT],
                landmarks[RIGHT_EYE_TOP],
                landmarks[RIGHT_EYE_RIGHT],
                landmarks[RIGHT_EYE_BOTTOM]
            )

            // Calculate mouth aspect ratio
            val mouthRatio = calculateMouthAspectRatio(
                landmarks[MOUTH_LEFT],
                landmarks[MOUTH_RIGHT],
                landmarks[MOUTH_TOP],
                landmarks[MOUTH_BOTTOM]
            )

            // Calculate smile elevation (corner lift)
            val smileElevation = calculateSmileElevation(
                landmarks[LEFT_MOUTH_CORNER],
                landmarks[RIGHT_MOUTH_CORNER],
                landmarks[MOUTH_TOP]
            )

            val data = FaceDetectionData(
                faceDetected = true,
                leftEyeAspectRatio = leftEAR,
                rightEyeAspectRatio = rightEAR,
                mouthAspectRatio = mouthRatio,
                smileElevation = smileElevation,
                timestamp = System.currentTimeMillis()
            )

            // Emit with minimal frequency (~30 Hz)
            val now = System.currentTimeMillis()
            if (now - lastProcessedTimeMs >= 33L) {
                executor.execute {
                    kotlin.runCatching {
                        _faceDetectionEvents.tryEmit(data)
                    }
                }
                lastProcessedTimeMs = now
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing face landmarks: ${e.message}")
        }
    }

    /**
     * Calculate Eye Aspect Ratio (EAR) from eye landmarks.
     * EAR = ||p2 - p6|| + ||p3 - p5|| / (2 * ||p1 - p4||)
     * where p1-p6 are the eye region landmarks.
     */
    private fun calculateEyeAspectRatio(left: Any?, top: Any?, right: Any?, bottom: Any?): Float {
        return try {
            val p1 = left as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return RESTING_EAR
            val p2 = top as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return RESTING_EAR
            val p3 = right as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return RESTING_EAR
            val p4 = bottom as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return RESTING_EAR

            val verticalDist1 = hypot((p2.x() - p1.x()), (p2.y() - p1.y()))
            val verticalDist2 = hypot((p3.x() - p1.x()), (p3.y() - p1.y()))
            val horizontalDist = hypot((p4.x() - p1.x()), (p4.y() - p1.y()))

            ((verticalDist1 + verticalDist2) / (2.0f * horizontalDist)).coerceIn(0f, 1f)
        } catch (e: Exception) {
            Log.w(TAG, "Error calculating EAR: ${e.message}")
            RESTING_EAR
        }
    }

    /**
     * Calculate mouth opening ratio.
     */
    private fun calculateMouthAspectRatio(left: Any?, right: Any?, top: Any?, bottom: Any?): Float {
        return try {
            val p1 = left as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return 0f
            val p2 = right as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return 0f
            val p3 = top as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return 0f
            val p4 = bottom as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return 0f

            val verticalDist = hypot((p4.x() - p3.x()), (p4.y() - p3.y()))
            val horizontalDist = hypot((p2.x() - p1.x()), (p2.y() - p1.y()))

            (verticalDist / horizontalDist).coerceIn(0f, 1f)
        } catch (e: Exception) {
            Log.w(TAG, "Error calculating mouth ratio: ${e.message}")
            0f
        }
    }

    /**
     * Calculate smile elevation (how much mouth corners are lifted).
     */
    private fun calculateSmileElevation(leftCorner: Any?, rightCorner: Any?, mouthTop: Any?): Float {
        return try {
            val p1 = leftCorner as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return 0f
            val p2 = rightCorner as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return 0f
            val p3 = mouthTop as? com.google.mediapipe.tasks.components.containers.NormalizedLandmark ?: return 0f

            // Average Y position of mouth corners
            val cornerY = (p1.y() + p2.y()) / 2f
            // Elevation = how much corners are higher than baseline (normalized by mouth height)
            val elevation = (cornerY - p3.y()).coerceAtLeast(0f)
            elevation.coerceIn(0f, 1f)
        } catch (e: Exception) {
            Log.w(TAG, "Error calculating smile elevation: ${e.message}")
            0f
        }
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        return try {
            val planes = this.planes
            val buffer = planes[0].buffer
            buffer.rewind()
            val pixelStride = planes[0].pixelStride
            val rowPadding = planes[0].rowPadding
            val rowSize = width * pixelStride + rowPadding
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            buffer.apply {
                val data = ByteArray(buffer.capacity())
                get(data)
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error converting ImageProxy to Bitmap: ${e.message}")
            null
        }
    }

    private fun Bitmap.rotate(degrees: Int): Bitmap {
        return if (degrees == 0) {
            this
        } else {
            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }
    }

    fun release() {
        faceLandmarker?.close()
        faceLandmarker = null
    }

    /**
     * Data class for face detection results.
     */
    data class FaceDetectionData(
        val faceDetected: Boolean,
        val leftEyeAspectRatio: Float,
        val rightEyeAspectRatio: Float,
        val mouthAspectRatio: Float,
        val smileElevation: Float,
        val timestamp: Long
    )
}
