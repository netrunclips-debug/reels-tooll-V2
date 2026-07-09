package com.example.gesturetiktok

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.gesturetiktok.accessibility.GestureAccessibilityService
import com.example.gesturetiktok.overlay.OverlayService
import com.example.gesturetiktok.util.NotificationHelper
import com.example.gesturetiktok.util.PermissionsHelper
import com.example.gesturetiktok.vision.FaceLandmarkAnalyzer
import com.example.gesturetiktok.vision.GestureDetector
import com.example.gesturetiktok.vision.GestureEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * MainActivity: Settings, permissions, and onboarding screen.
 *
 * Responsibilities:
 * - Display permission status and request buttons
 * - Show current status (running/stopped)
 * - Start/stop gesture recognition
 * - Display debug information about detected gestures
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

    // UI Components
    private lateinit var titleText: TextView
    private lateinit var statusText: TextView
    private lateinit var debugText: TextView
    private lateinit var cameraPermissionCheck: CheckBox
    private lateinit var overlayPermissionCheck: CheckBox
    private lateinit var accessibilityPermissionCheck: CheckBox
    private lateinit var grantCameraButton: Button
    private lateinit var grantOverlayButton: Button
    private lateinit var enableAccessibilityButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    // Vision and gesture detection
    private var faceLandmarkAnalyzer: FaceLandmarkAnalyzer? = null
    private var gestureDetector: GestureDetector? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // State
    private var isRunning = false
    private val debugLog = mutableListOf<String>()

    // Permission launcher
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        updatePermissionStatus()
        Log.d(TAG, "Permissions result: $permissions")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUI()
        setupPermissionButtons()
        setupControlButtons()
        NotificationHelper.createNotificationChannels(this)
        updatePermissionStatus()

        // Start listening to gesture events
        startGestureListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGestureRecognition()
        cameraExecutor.shutdown()
    }

    private fun initializeUI() {
        titleText = findViewById(R.id.titleText)
        statusText = findViewById(R.id.statusText)
        debugText = findViewById(R.id.debugText)
        cameraPermissionCheck = findViewById(R.id.cameraPermissionCheck)
        overlayPermissionCheck = findViewById(R.id.overlayPermissionCheck)
        accessibilityPermissionCheck = findViewById(R.id.accessibilityPermissionCheck)
        grantCameraButton = findViewById(R.id.grantCameraButton)
        grantOverlayButton = findViewById(R.id.grantOverlayButton)
        enableAccessibilityButton = findViewById(R.id.enableAccessibilityButton)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
    }

    private fun setupPermissionButtons() {
        grantCameraButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissionsLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA)
                )
            }
        }

        grantOverlayButton.setOnClickListener {
            PermissionsHelper.openOverlayPermissionSettings(this)
        }

        enableAccessibilityButton.setOnClickListener {
            PermissionsHelper.openAccessibilitySettings(this)
        }
    }

    private fun setupControlButtons() {
        startButton.setOnClickListener {
            if (checkAllPermissions()) {
                startGestureRecognition()
            } else {
                addDebugLog("Permissions not granted")
                updateStatus("Permissions required")
            }
        }

        stopButton.setOnClickListener {
            stopGestureRecognition()
        }
    }

    private fun updatePermissionStatus() {
        val cameraGranted = PermissionsHelper.isCameraPermissionGranted(this)
        val overlayGranted = PermissionsHelper.isOverlayPermissionGranted(this)
        val accessibilityEnabled = PermissionsHelper.isAccessibilityServiceEnabled(this)

        cameraPermissionCheck.isChecked = cameraGranted
        overlayPermissionCheck.isChecked = overlayGranted
        accessibilityPermissionCheck.isChecked = accessibilityEnabled

        Log.d(TAG, "Permissions - Camera: $cameraGranted, Overlay: $overlayGranted, Accessibility: $accessibilityEnabled")
    }

    private fun checkAllPermissions(): Boolean {
        return PermissionsHelper.isCameraPermissionGranted(this) &&
                PermissionsHelper.isOverlayPermissionGranted(this) &&
                PermissionsHelper.isAccessibilityServiceEnabled(this)
    }

    private fun startGestureRecognition() {
        if (isRunning) return

        isRunning = true
        addDebugLog("Starting gesture recognition...")
        updateStatus("Initializing...")

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                // Initialize gesture detector
                gestureDetector = GestureDetector()

                // Initialize face landmark analyzer
                faceLandmarkAnalyzer = FaceLandmarkAnalyzer(this@MainActivity, cameraExecutor)

                // Setup camera
                setupCamera()

                // Start overlay service
                startOverlayService()

                addDebugLog("Gesture recognition started")
                updateStatus("Running")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting gesture recognition: ${e.message}")
                addDebugLog("Error: ${e.message}")
                updateStatus("Error: ${e.message}")
                isRunning = false
            }
        }
    }

    private fun stopGestureRecognition() {
        if (!isRunning) return

        isRunning = false
        addDebugLog("Stopping gesture recognition...")

        // Stop camera
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding camera: ${e.message}")
        }

        // Stop overlay service
        stopOverlayService()

        // Release analyzer
        faceLandmarkAnalyzer?.release()
        faceLandmarkAnalyzer = null

        addDebugLog("Gesture recognition stopped")
        updateStatus("Stopped")
    }

    private suspend fun setupCamera() {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(this).await()
            this@MainActivity.cameraProvider = cameraProvider

            val preview = Preview.Builder().build().also {
                // Can bind to PreviewView if we add one to layout
            }

            // Image analysis for gesture detection
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also {
                    faceLandmarkAnalyzer?.let { analyzer ->
                        it.setAnalyzer(cameraExecutor, analyzer)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
            )

            addDebugLog("Camera setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up camera: ${e.message}")
            addDebugLog("Camera error: ${e.message}")
        }
    }

    private fun startGestureListener() {
        lifecycleScope.launch {
            faceLandmarkAnalyzer?.faceDetectionEvents?.collect { data ->
                lifecycleScope.launch(Dispatchers.Main) {
                    // Process face detection data through gesture detector
                    gestureDetector?.processFrame(
                        data.leftEyeAspectRatio,
                        data.rightEyeAspectRatio,
                        data.mouthAspectRatio,
                        data.smileElevation,
                        data.timestamp
                    )

                    val metrics = gestureDetector?.getSmoothedMetrics()
                    if (metrics != null) {
                        addDebugLog(
                            "L:%.2f R:%.2f M:%.2f".format(
                                metrics.leftEAR,
                                metrics.rightEAR,
                                metrics.mouthRatio
                            )
                        )
                    }
                }
            }
        }

        // Listen for gesture events
        lifecycleScope.launch {
            gestureDetector?.gestureEvents?.collect { gesture ->
                Log.d(TAG, "Gesture detected: $gesture")
                addDebugLog("Gesture: $gesture")

                // Perform accessibility action
                when (gesture) {
                    is GestureEvent.SwipeDown -> {
                        addDebugLog("↓ Scroll down")
                        GestureAccessibilityService.performGesture(
                            GestureAccessibilityService.GESTURE_SWIPE_DOWN
                        )
                    }
                    is GestureEvent.SwipeUp -> {
                        addDebugLog("↑ Scroll up")
                        GestureAccessibilityService.performGesture(
                            GestureAccessibilityService.GESTURE_SWIPE_UP
                        )
                    }
                    is GestureEvent.Like -> {
                        addDebugLog("❤ Like")
                        GestureAccessibilityService.performGesture(
                            GestureAccessibilityService.GESTURE_LIKE
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        addDebugLog("Overlay service started")
    }

    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
        addDebugLog("Overlay service stopped")
    }

    private fun updateStatus(status: String) {
        statusText.text = status
    }

    private fun addDebugLog(message: String) {
        debugLog.add("[${System.currentTimeMillis()}] $message")
        if (debugLog.size > 50) {
            debugLog.removeAt(0)
        }
        debugText.text = debugLog.joinToString("\n")
        // Auto-scroll to bottom
        debugText.post {
            debugText.layout?.let {
                debugText.scrollTo(0, debugText.height)
            }
        }
    }
}

// Extension function for suspending ProcessCameraProvider.getInstance
private suspend fun ProcessCameraProvider.Companion.getInstance(context: android.content.Context) =
    androidx.concurrent.futures.await()
