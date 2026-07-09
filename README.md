# Gesture-Controlled TikTok Companion - Native Android App

A native Android application (Kotlin) that enables hands-free control of the TikTok app using facial gestures captured via the front-facing camera.

## Overview

This app uses:
- **CameraX** for front-facing camera access
- **MediaPipe Face Landmarker** for real-time facial landmark detection
- **Android AccessibilityService** for gesture automation (swipes/taps in TikTok)
- **Foreground Service + Overlay UI** to run while TikTok is in the foreground

## Gesture Mappings

| Gesture | Action |
|---------|--------|
| **Right eye wink** | Scroll down (next video) |
| **Left eye wink** | Scroll up (previous video) |
| **Mouth wide open** | Like current video (double-tap) |
| **Sustained smile (~1s)** | Like current video (double-tap) |

All gestures have a 1.2 second debounce cooldown to prevent repeat-firing.

## Project Structure

```
app/
├── src/main/java/com/example/gesturetiktok/
│   ├── MainActivity.kt                          # Main settings/onboarding screen
│   ├── vision/
│   │   ├── FaceLandmarkAnalyzer.kt             # CameraX analyzer, MediaPipe integration
│   │   ├── GestureDetector.kt                  # Gesture state machine, debounce logic
│   │   └── GestureEvent.kt                     # Sealed class for gesture events
│   ├── accessibility/
│   │   └── GestureAccessibilityService.kt      # Accessibility service for swipes/taps
│   ├── overlay/
│   │   ├── OverlayService.kt                   # Foreground service
│   │   └── OverlayView.kt                      # Floating UI overlay
│   └── util/
│       ├── PermissionsHelper.kt                # Permission checking/requesting
│       └── NotificationHelper.kt               # Notification channel creation
├── src/main/res/
│   ├── layout/
│   │   ├── activity_main.xml                   # Main activity UI
│   │   └── overlay_view.xml                    # Overlay floating widget
│   ├── drawable/
│   │   ├── ic_launcher_background.xml
│   │   ├── ic_launcher_foreground.xml
│   │   └── overlay_circle_background.xml       # Overlay circle shape
│   ├── xml/
│   │   ├── accessibility_service_config.xml   # Accessibility service config
│   │   ├── data_extraction_rules.xml
│   │   └── backup_rules.xml
│   ├── values/
│   │   ├── strings.xml
│   │   ├── colors.xml
│   │   └── themes.xml
│   └── AndroidManifest.xml                     # App manifest
└── build.gradle.kts                            # App dependencies
```

## Requirements

- **Android SDK**: API 24+ (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Kotlin**: 1.9.23
- **Android Gradle Plugin**: 8.2.0

## Dependencies

Key dependencies are already configured in `app/build.gradle.kts`:

- `androidx.camera:camera-camera2:1.3.1` - Camera framework
- `com.google.mediapipe:tasks-vision:0.10.12` - Face Landmarker ML model
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3` - Coroutines
- `androidx.lifecycle:lifecycle-runtime-ktx:2.7.0` - Lifecycle management

## Permissions Required

### Runtime Permissions (must be granted at runtime)
- `android.permission.CAMERA` - Access front-facing camera
- `android.permission.SYSTEM_ALERT_WINDOW` - Draw overlay on top of other apps

### System Permissions (declared in manifest)
- `android.permission.FOREGROUND_SERVICE` - Run foreground service
- `android.permission.BIND_ACCESSIBILITY_SERVICE` - Use accessibility service (no runtime prompt; user enables manually in Settings)

## Setup & Build Instructions

### Step 1: Clone/Extract the Project

```bash
cd your/projects/directory
# Extract or clone this project
```

### Step 2: Open in Android Studio

1. Open Android Studio
2. Select `File > Open`
3. Navigate to the project folder
4. Wait for Gradle sync to complete

### Step 3: Install MediaPipe Model

The app requires the MediaPipe Face Landmarker model file. You need to download and add it manually:

1. Download the model from:
   https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task

2. Place it in the assets folder:
   ```
   app/src/main/assets/face_landmarker_v2_with_blendshapes.task
   ```

3. If the `assets` folder doesn't exist, create it under `app/src/main/`

### Step 4: Build the App

```bash
# In terminal from project root:
./gradlew assembleDebug

# Or use Android Studio:
# Build > Build Bundle(s) / APK(s) > Build APK(s)
```

Output APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Step 5: Install on Device/Emulator

```bash
# Install via adb
adb install app/build/outputs/apk/debug/app-debug.apk

# Or use Android Studio:
# Run > Run 'app'
```

### Step 6: Enable Permissions

1. **Camera Permission**: When you first tap "Start", grant camera access in the permission dialog
2. **Overlay Permission**: Tap "Grant Overlay Permission" button → enables via system settings
3. **Accessibility Service**: Tap "Enable Accessibility Service" button → navigate to Settings > Accessibility > GestureTikTok > Toggle ON

## Milestones Implemented

### ✅ M1 - Camera + Basic Face Detection
- CameraX front-facing camera integration
- MediaPipe Face Landmarker initialization
- Facial landmark detection logging

### ✅ M2 - Gesture Detection with Debounce
- Eye aspect ratio (EAR) calculation for wink detection
- Mouth aspect ratio calculation for mouth-open detection
- Smile elevation scoring
- Temporal smoothing (3-frame rolling average)
- Debounce logic (1.2 second cooldown)
- Debug overlay text showing detected gestures

### ✅ M3 - Accessibility Service
- Custom AccessibilityService with gesture dispatch capability
- `performSwipe()` for vertical scrolling
- `performDoubleTap()` for liking videos
- Manual service enablement via system settings

### ✅ M4 - End-to-End Integration
- Vision module outputs gesture events
- Gesture events routed to AccessibilityService
- Gestures trigger actual swipes/taps in TikTok
- Real-time debug logging of all events

### ✅ M5 - Overlay UI & Foreground Service
- Floating circular overlay showing status
- Foreground service keeps recognition active
- Overlay shows face detection indicator and gesture labels

### ✅ M6 - Polish & Settings
- Main activity with permission status checks
- Start/Stop buttons for gesture recognition
- Debug console showing real-time metrics
- On-screen gesture sensitivity parameters (configurable in code)

## Usage

1. **Launch the app** and grant all requested permissions
2. **Tap "Start Gesture Recognition"** button
3. **Look at the camera** - the app should detect your face
4. **Perform gestures** while in TikTok:
   - **Right eye wink** → scroll to next video
   - **Left eye wink** → scroll to previous video
   - **Open mouth wide** → like video
   - **Sustained smile** → like video
5. **Debug info** shows in the bottom section with real-time facial metrics

## Customization

### Adjust Gesture Sensitivity

Open `MainActivity.kt` and modify the `GestureDetector` initialization:

```kotlin
gestureDetector = GestureDetector(
    eyeAspectRatioThreshold = 0.15f,      // Lower = more sensitive to winks
    mouthAspectRatioThreshold = 0.5f,     // Lower = easier to trigger mouth-open
    smileThreshold = 0.2f,                // Lower = easier to trigger smile
    debounceMs = 1200L,                   // Cooldown between gestures
    minWinkDurationMs = 150L,             // Minimum wink duration
    minSmileDurationMs = 1000L            // Minimum smile hold time
)
```

### Change Gesture Actions

Modify `GestureAccessibilityService.kt` methods:
- `performSwipeDown()` - change swipe distance or starting position
- `performSwipeUp()` - same
- `performDoubleTap()` - change tap location or delay

## Known Limitations & Risks

1. **TikTok Terms of Service**: Automated interaction with TikTok may violate its ToS. Use for personal/accessibility purposes only.

2. **Battery Consumption**: Continuous camera + ML inference drains battery significantly. Run for limited sessions.

3. **Accuracy Varies by Device**: 
   - Different screen sizes require coordinate adjustments
   - Camera quality affects facial landmark detection
   - Lighting conditions impact accuracy

4. **MediaPipe Version Compatibility**: The model file and library versions are pinned to avoid breaking changes.

5. **Performance**: ML inference runs on the main thread in production; consider moving to background thread for production use.

## Troubleshooting

### App Crashes on Start
- Ensure MediaPipe model file exists at `app/src/main/assets/face_landmarker_v2_with_blendshapes.task`
- Check that all permissions are granted in system settings

### No Face Detected
- Ensure front-facing camera permission is granted
- Check camera preview is working in other apps
- Try increasing lighting or adjusting camera distance

### Gestures Not Working in TikTok
- Verify accessibility service is enabled (Settings > Accessibility > GestureTikTok)
- Check if coordinates in `GestureAccessibilityService` need adjustment for your screen resolution
- Review debug logs for gesture detection confirmation

### High Battery Drain
- Disable gesture recognition when not in use (tap Stop button)
- Close TikTok and other background apps
- Consider reducing frame analysis frequency in `FaceLandmarkAnalyzer`

## Debug Logging

All major events are logged with the `TAG` prefix. View logs with:

```bash
adb logcat | grep "GestureTikTok\|MainActivity\|GestureAccessibilityService\|FaceLandmarkAnalyzer"
```

The main activity also displays a real-time debug console at the bottom of the screen showing:
- Permission status
- Facial metrics (EAR, mouth ratio)
- Detected gestures
- Service state changes

## Future Enhancements

- [ ] Custom sensitivity calibration UI
- [ ] Gesture recording/training for custom actions
- [ ] Batch normalization for cross-device accuracy
- [ ] History of detected gestures
- [ ] Export metrics to CSV for analysis
- [ ] Multi-face detection
- [ ] Head pose detection for gaze-based control

## License

This project is provided as-is for personal and accessibility use.

## Support

For issues or questions, check the debug logs first and ensure all permissions are properly configured.
