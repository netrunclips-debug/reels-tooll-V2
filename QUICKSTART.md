# Quick Start Guide

## Prerequisites

- Android Studio 2023.1 or later (with Android SDK 34 installed)
- Java 11 or later
- A physical Android device (API 24+) or emulator for testing

## Installation Steps

### 1. Download MediaPipe Model (REQUIRED)

The app requires the MediaPipe Face Landmarker TFLite model:

```bash
cd app/src/main

# Create assets directory if it doesn't exist
mkdir -p assets

# Download the model (using curl, wget, or your browser)
# URL: https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task

# If using curl:
cd assets
curl -L "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task" \
  -o face_landmarker_v2_with_blendshapes.task
cd ../../../
```

The file should be placed at: `app/src/main/assets/face_landmarker_v2_with_blendshapes.task`

### 2. Build the APK

```bash
# From the project root directory
./gradlew assembleDebug

# Or on Windows:
# gradlew.bat assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### 3. Install on Device

```bash
# Via adb
adb install app/build/outputs/apk/debug/app-debug.apk

# Or use Android Studio: Run > Run 'app'
```

### 4. Grant Permissions

Launch the app and follow the on-screen prompts:

1. **Camera Permission**: Tap "Grant Camera Permission" button
   - This will show the standard Android permission dialog
   - Grant camera access

2. **Overlay Permission**: Tap "Grant Overlay Permission" button
   - This opens system Settings
   - Navigate to: Settings > Apps > Permissions > Special App Access > Display over other apps
   - Find GestureTikTok and toggle ON

3. **Accessibility Service**: Tap "Enable Accessibility Service" button
   - This opens Accessibility settings
   - Find GestureTikTok in the services list
   - Toggle it ON

### 5. Test the App

1. Tap "Start Gesture Recognition"
2. Look at the camera - verify face detection in debug console
3. Open TikTok in another app
4. Try gestures:
   - **Wink right eye** → scroll down
   - **Wink left eye** → scroll up
   - **Open mouth wide** → like video
   - **Smile for ~1 second** → like video

## Build Troubleshooting

### Gradle Sync Fails
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

### MediaPipe Model Not Found
- Verify the file exists: `app/src/main/assets/face_landmarker_v2_with_blendshapes.task`
- File size should be ~30-40 MB
- If missing, download again from the link above

### Camera Not Working
- Verify camera permission is granted
- Check device has a front-facing camera
- Try opening another camera app to verify camera hardware works

### Accessibility Service Won't Enable
- Ensure permission is declared in AndroidManifest.xml (it is)
- Go to Settings > Accessibility and find GestureTikTok service
- Toggle it ON manually

## Development Notes

### Key Files to Modify

**For gesture sensitivity adjustments:**
- `app/src/main/java/com/example/gesturetiktok/vision/GestureDetector.kt`
  - Lines showing threshold values (0.15f, 0.5f, 0.2f, etc.)

**For gesture actions/coordinates:**
- `app/src/main/java/com/example/gesturetiktok/accessibility/GestureAccessibilityService.kt`
  - `performSwipeDown()`, `performSwipeUp()`, `performDoubleTap()` methods

**For UI changes:**
- `app/src/main/res/layout/activity_main.xml` - main screen layout
- `app/src/main/res/layout/overlay_view.xml` - floating overlay layout
- `app/src/main/res/values/strings.xml` - all app text

### Building a Release APK

```bash
# Requires signing configuration in build.gradle.kts
./gradlew assembleRelease
```

For production builds, you'll need to create/configure a keystore file for signing.

## Performance Tips

1. **Reduce logging overhead**: Comment out `Log.d()` calls in production
2. **Optimize frame analysis**: Increase minimum frame skip time in `FaceLandmarkAnalyzer`
3. **Battery optimization**: Disable overlay when not needed
4. **Memory usage**: Clear debug logs periodically in `MainActivity`

## Emulator vs Physical Device

**Physical Device (Recommended)**
- Better camera quality
- More accurate gesture detection
- Can test true TikTok integration
- Faster performance

**Emulator**
- Can test basic app functionality
- Some camera emulation support
- Slower MediaPipe inference
- May not have proper camera hardware support

## Next Steps

1. Customize gesture thresholds for your face/lighting conditions
2. Adjust swipe coordinates if needed for your screen size
3. Test different gestures to find what works best
4. Explore adding more gesture types by extending `GestureEvent` sealed class

See README.md for detailed documentation and customization options.
