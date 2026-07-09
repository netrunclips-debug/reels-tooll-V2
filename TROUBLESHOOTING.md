# Troubleshooting Guide

## Build Issues

### Gradle Sync Fails

**Error Message:** `Gradle sync failed`

**Solution:**
```bash
# Clean and try again
./gradlew clean
./gradlew build

# If using Android Studio:
File > Invalidate Caches > Invalidate and Restart
```

### MediaPipe Model Not Found

**Error Message:** `FileNotFoundException: face_landmarker_v2_with_blendshapes.task`

**Root Cause:** The model file is missing from assets folder.

**Solution:**
1. Create directory: `app/src/main/assets/`
2. Download the model from:
   ```
   https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task
   ```
3. Place file at: `app/src/main/assets/face_landmarker_v2_with_blendshapes.task`
4. Verify file size is ~30-40 MB
5. Rebuild and reinstall APK

### Dependency Resolution Errors

**Error:** `Could not resolve com.google.mediapipe:tasks-vision`

**Solution:**
- Ensure internet connection is active
- Check that `settings.gradle.kts` has correct repositories (Google, Maven Central)
- Delete `.gradle` folder and rebuild:
  ```bash
  rm -rf .gradle
  ./gradlew build
  ```

### Invalid Kotlin Version

**Error:** `Kotlin DSL script compilation errors`

**Solution:**
- Ensure Kotlin plugin version 1.9.23 is installed
- In Android Studio: Tools > Kotlin > Configure Kotlin Plugin Updates

## Runtime Issues

### App Crashes on Launch

**Error in Logcat:**
```
Fatal Exception: java.lang.RuntimeException: Unable to start activity...
```

**Common Causes & Solutions:**

1. **Missing Camera Permission**
   - Check: Settings > Apps > GestureTikTok > Permissions
   - Grant Camera permission manually

2. **MediaPipe Initialization Failed**
   - Verify model file exists: `app/src/main/assets/face_landmarker_v2_with_blendshapes.task`
   - Check file is not corrupted (size ~30-40 MB)
   - Re-download model if suspected corruption

3. **Hardware Compatibility**
   - Ensure device has front-facing camera
   - Test camera in another app (built-in Camera app)

**Debug Steps:**
```bash
adb logcat | grep "GestureTikTok\|MediaPipe\|CameraX"
```

### No Face Detected

**Symptoms:**
- Debug console shows "No Face Detected"
- Green indicator never appears
- No facial metrics displayed

**Solutions:**

1. **Camera Permission Issues**
   ```bash
   adb shell pm grant com.example.gesturetiktok android.permission.CAMERA
   adb shell pm grant com.example.gesturetiktok android.permission.SYSTEM_ALERT_WINDOW
   ```

2. **Camera Not Working**
   - Restart device
   - Uninstall and reinstall app
   - Test camera in built-in Camera app

3. **Poor Lighting**
   - Move to better lit area
   - Face the light source directly
   - Avoid backlighting

4. **Camera Distance/Angle**
   - Position face 20-50 cm from camera
   - Center face in camera view
   - Avoid extreme angles

5. **Model Loading Issue**
   - Check logcat for MediaPipe errors:
     ```bash
     adb logcat | grep MediaPipe
     ```
   - Verify model file path in `FaceLandmarkAnalyzer.kt`

### Gestures Not Working in TikTok

**Symptoms:**
- Gestures detected (shown in debug console)
- But TikTok doesn't scroll or like

**Diagnosis Steps:**

1. **Verify Gesture Detection**
   - Check debug console shows gestures being detected
   - If not detected → see "No Face Detected" section above
   - If detected → continue to next step

2. **Check Accessibility Service is Enabled**
   - Open: Settings > Accessibility > Installed Services
   - Find "GestureTikTok Gesture Control"
   - Verify toggle is ON
   - If off → tap to enable
   - If can't find → may need to:
     ```bash
     adb shell pm grant com.example.gesturetiktok android.permission.BIND_ACCESSIBILITY_SERVICE
     ```

3. **Verify Overlay Permission**
   - Open: Settings > Apps > Permissions > Display over other apps
   - Find GestureTikTok
   - Verify toggle is ON

4. **Test Gesture Coordinates**
   - Gestures use screen center coordinates
   - For different screen sizes, may need adjustment
   - Edit `GestureAccessibilityService.kt`:
     ```kotlin
     val screenWidth = resources.displayMetrics.widthPixels  // Get actual screen width
     val screenHeight = resources.displayMetrics.heightPixels // Get actual screen height
     // Adjust swipe start/end coordinates based on screen size
     ```

5. **TikTok UI Compatibility**
   - Ensure TikTok app is fully loaded (wait 2-3 seconds)
   - Try gestures on the video feed page (not explore/messages/etc)
   - Some TikTok UI elements may not respond to gestures

### Accessibility Service Disabled After Granting

**Symptoms:**
- Service is enabled, then immediately disabled
- Or crashes when trying to enable

**Solution:**
1. Go to Settings > Accessibility > [App Name]
2. Tap the service name to open settings
3. Grant all required permissions if prompted
4. Ensure "Use service" toggle is ON

**If still fails:**
```bash
# Restart accessibility manager
adb shell cmd accessibility restart
```

### Battery Drain Too Fast

**Symptoms:**
- Battery depletes rapidly while app is running
- Device gets hot

**Causes:** Continuous camera + ML inference is intensive

**Solutions:**

1. **Reduce Detection Frequency**
   - In `FaceLandmarkAnalyzer.kt`, increase frame skip:
     ```kotlin
     // Change from 33ms (30Hz) to 100ms+ (~10Hz)
     if (now - lastProcessedTimeMs >= 100L) {
         // Process frame
     }
     ```

2. **Lower Detection Resolution**
   - In `MainActivity.kt`, reduce input image size:
     ```kotlin
     val imageAnalysis = ImageAnalysis.Builder()
         .setOutputImageSize(480, 480) // Reduced from 640x480
         .build()
     ```

3. **Disable Overlay When Not Needed**
   - Tap "Stop" button when using TikTok
   - Restart when needed

4. **Use Performance Mode**
   - Enable device battery saver mode
   - Close background apps
   - Reduce screen brightness

### Memory Leak Issues

**Symptoms:**
- App slows down after running for a while
- Eventually crashes with OutOfMemoryError

**Solutions:**

1. **Clear Debug Logs**
   - Edit `MainActivity.kt` to limit debug log size:
     ```kotlin
     if (debugLog.size > 100) {  // Was 50
         debugLog.removeAt(0)
     }
     ```

2. **Release Resources Properly**
   - Ensure camera is released in `onDestroy()`
   - Check `cameraExecutor.shutdown()` is called
   - Verify `faceLandmarkAnalyzer?.release()` is called

3. **Restart App Periodically**
   - Memory usage grows over time
   - Restarting frees memory

### High Frame Drop/Lag

**Symptoms:**
- Gesture detection feels slow/delayed
- Face landmarks not updating smoothly

**Solutions:**

1. **Reduce Model Inference**
   - Increase frame skip interval
   - Process every 3rd frame instead of every frame

2. **Profile CPU Usage**
   ```bash
   # View CPU usage in real-time
   adb shell top | grep gesturetiktok
   ```

3. **Check Device Specs**
   - Test on device with more RAM (4GB+)
   - Older devices may struggle

4. **Use Release Build**
   - Debug builds are slower
   - Try release build: `./gradlew assembleRelease`

## Permission Issues

### "Permission Denied" Errors

**Error Messages:**
- `SecurityException: Permission denied: opening provider com.android.providers.settings`
- `Permission android.permission.CAMERA was not granted`

**Solutions:**

1. **Grant via Settings UI**
   - Go to Settings > Apps > GestureTikTok > Permissions
   - Grant Camera and other required permissions

2. **Grant via ADB**
   ```bash
   adb shell pm grant com.example.gesturetiktok android.permission.CAMERA
   adb shell pm grant com.example.gesturetiktok android.permission.SYSTEM_ALERT_WINDOW
   adb shell pm grant com.example.gesturetiktok android.permission.FOREGROUND_SERVICE
   ```

3. **Reinstall App**
   ```bash
   adb uninstall com.example.gesturetiktok
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Accessibility Service Permission Error

**Error:** `java.lang.SecurityException: Accessibil... service permission`

**Solution:**
- Only the SYSTEM can grant this via AccessibilityService
- User must enable manually in Settings
- App cannot request at runtime
- Check manifest has: `android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"`

### Overlay Permission Error

**Error:** `Permission denied: android.permission.SYSTEM_ALERT_WINDOW`

**Solution:**
1. Go to Settings > Apps > Permissions > Display over other apps
2. Find GestureTikTok and toggle ON
3. Or via ADB:
   ```bash
   adb shell pm grant com.example.gesturetiktok android.permission.SYSTEM_ALERT_WINDOW
   ```

## Testing & Verification

### Verify Camera is Working

```bash
# Access camera via adb
adb shell cmd media.camera list
```

### Verify Accessibility Service is Working

```bash
# Check accessibility settings
adb shell settings get secure enabled_accessibility_services

# Should output something like:
# com.example.gesturetiktok/com.example.gesturetiktok.accessibility.GestureAccessibilityService
```

### View Real-Time Logs

```bash
# All app logs
adb logcat | grep GestureTikTok

# Vision module only
adb logcat | grep FaceLandmarkAnalyzer

# Gesture detection only
adb logcat | grep GestureDetector

# Accessibility service only
adb logcat | grep GestureAccessibilityService

# Clear logs before test
adb logcat -c
```

### Test Accessibility Service Manually

```bash
# Inject a swipe gesture (if service is enabled)
adb shell input swipe 500 500 500 100  # Swipe up from center

# Inject a tap
adb shell input tap 500 500  # Tap center
```

## Device-Specific Issues

### Samsung Devices

**Known Issue:** Accessibility service may be disabled by system optimizations

**Solution:**
1. Go to Settings > Apps > Battery > Battery Optimization
2. Find GestureTikTok and set to "Don't optimize"
3. Go to Settings > Accessibility and re-enable service

### Google Pixel Devices

**Known Issue:** Camera may have lag in emulator

**Solution:**
- Test on physical device instead
- Pixel emulator camera emulation is limited

### Low-End Devices (RAM < 2GB)

**Known Issue:** MediaPipe inference may crash due to memory

**Solution:**
- Upgrade to device with at least 2GB RAM
- Or optimize model inference (advanced)

## Getting Help

### Collect Debug Information

When reporting issues, collect:

```bash
# Device info
adb shell getprop ro.build.version.release      # Android version
adb shell getprop ro.product.model               # Device model

# App logs
adb logcat -d > logs.txt

# Check file size
adb shell ls -lah app/src/main/assets/           # Verify model file

# Permissions
adb shell pm list permissions | grep gesturetiktok
```

### Check Common Solutions

Before reporting issues:
1. Read this troubleshooting guide fully
2. Check README.md and QUICKSTART.md
3. Verify MediaPipe model file exists and is correct size
4. Restart device
5. Reinstall app

---

**Still Having Issues?** 
Review the logcat output carefully - most errors are logged with context. The log messages will indicate which component is failing (FaceLandmarkAnalyzer, GestureDetector, etc.).
