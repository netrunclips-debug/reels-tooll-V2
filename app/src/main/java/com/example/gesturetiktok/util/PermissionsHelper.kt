package com.example.gesturetiktok.util

import android.Manifest
import android.accessibilityservice.AccessibilityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * PermissionsHelper: Utility class for checking and requesting permissions.
 */
object PermissionsHelper {

    private const val TAG = "PermissionsHelper"

    /**
     * Check if camera permission is granted.
     */
    fun isCameraPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if overlay/system alert window permission is granted.
     */
    fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if accessibility service is enabled.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""

            val serviceComponents = enabledServices.split(":")
            val packageName = context.packageName
            val serviceName = "$packageName/.accessibility.GestureAccessibilityService"

            serviceComponents.any { component ->
                component.contains(packageName) && component.contains("GestureAccessibilityService")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking accessibility service: ${e.message}")
            false
        }
    }

    /**
     * Open camera permission request.
     */
    fun requestCameraPermission(context: Context) {
        // This is typically handled by requesting in Activity.requestPermissions
        Log.d(TAG, "Camera permission request should be handled by Activity")
    }

    /**
     * Open overlay permission settings.
     */
    fun openOverlayPermissionSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening overlay settings: ${e.message}")
        }
    }

    /**
     * Open accessibility service settings.
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening accessibility settings: ${e.message}")
        }
    }

    /**
     * Get all required permissions for the app.
     */
    fun getRequiredPermissions(): List<String> {
        return listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.FOREGROUND_SERVICE
        )
    }

    /**
     * Get runtime permissions (must be requested at runtime on API 23+).
     */
    fun getRuntimePermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            )
        } else {
            emptyList()
        }
    }
}
