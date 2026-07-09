# Architecture & Design

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     Android System                              │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   MainActivity                           │  │
│  │  - UI Controls (Start/Stop)                             │  │
│  │  - Permission Status Display                            │  │
│  │  - Debug Console                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│           ▲                    ▲                    ▲           │
│           │                    │                    │           │
│           │                    │                    │           │
│  ┌────────┴────────────────────┴────────────────────┴────────┐ │
│  │                  Gesture Recognition Pipeline             │ │
│  └────────────────────────────────────────────────────────────┘ │
│           ▲                    ▲                    ▲           │
│           │                    │                    │           │
│    ┌──────┴──────┐    ┌────────┴────────┐   ┌─────┴──────────┐│
│    │   Vision    │    │  Gesture State  │   │ Accessibility  ││
│    │   Module    │    │    Machine      │   │    Service     ││
│    │             │    │                 │   │                ││
│    │ CameraX +   │    │ GestureDetector │   │ Performs       ││
│    │ MediaPipe   │    │                 │   │ Gestures in    ││
│    │             │    │ - Debounce      │   │ TikTok         ││
│    │ Detects:    │    │ - Wink logic    │   │                ││
│    │ - Face      │    │ - Mouth open    │   │ - Swipes       ││
│    │ - Eyes      │    │ - Smile         │   │ - Double-taps  ││
│    │ - Mouth     │    │                 │   │                ││
│    └─────┬───────┘    └────────┬────────┘   └────────┬───────┘│
│          │                     │                     │         │
│          │ FaceDetectionData   │ GestureEvents      │         │
│          │ (SharedFlow)        │ (SharedFlow)        │         │
│          └─────────────────────┴─────────────────────┘         │
│                          │                                      │
│                          ▼                                      │
│           ┌──────────────────────────────┐                     │
│           │    OverlayService +          │                     │
│           │    OverlayView               │                     │
│           │                              │                     │
│           │ - Shows face detection       │                     │
│           │ - Displays gesture labels    │                     │
│           │ - Floating indicator         │                     │
│           └──────────────────────────────┘                     │
└─────────────────────────────────────────────────────────────────┘
```

## Module Breakdown

### 1. Vision Module (vision/)

**Responsibilities:**
- Capture video frames from front-facing camera
- Extract facial landmarks using MediaPipe
- Calculate facial metrics (EAR, mouth ratio, smile elevation)
- Emit structured face detection data

**Key Classes:**

#### FaceLandmarkAnalyzer
```
Input:  Camera frames via CameraX ImageAnalysis
Process: MediaPipe Face Landmarker inference
Output: FaceDetectionData (via SharedFlow)

Calculates:
├── Left Eye Aspect Ratio (EAR)
├── Right Eye Aspect Ratio (EAR)
├── Mouth Aspect Ratio
└── Smile Elevation Score
```

#### GestureDetector
```
Input:  FaceDetectionData from FaceLandmarkAnalyzer
Process: State machine with temporal smoothing & debounce
Output: GestureEvent (SwipeUp/Down/Like) via SharedFlow

Detection Logic:
├── Wink Detection
│   ├── Left wink (EAR < threshold for 150-500ms)
│   └── Right wink (EAR < threshold for 150-500ms)
├── Mouth Open Detection
│   └── Mouth ratio > threshold
├── Smile Detection
│   └── Smile elevation > threshold for ≥1000ms
└── Debounce
    └── Minimum 1200ms between any gestures
```

#### GestureEvent (Sealed Class)
```kotlin
sealed class GestureEvent {
    object SwipeDown
    object SwipeUp
    object Like
    object None
}
```

**Design Decisions:**

- **Temporal Smoothing**: Uses 3-frame rolling average to reduce false positives from motion blur
- **Debounce Logic**: Prevents repeat-firing by tracking last gesture timestamp
- **Threshold Configurable**: All thresholds are configurable for per-device calibration
- **Separate EAR per Eye**: Enables independent wink detection for left/right distinction

### 2. Accessibility Module (accessibility/)

**Responsibilities:**
- Respond to gesture events
- Execute accessibility actions (swipes, taps) in TikTok
- Manage system accessibility service lifecycle

**Key Classes:**

#### GestureAccessibilityService
```
Extends: AccessibilityService
Bound: BIND_ACCESSIBILITY_SERVICE permission

Methods:
├── performSwipeDown()     - Downward gesture
├── performSwipeUp()       - Upward gesture
└── performDoubleTap()     - Double tap (like)

Internal:
├── buildSwipeGesture()    - Create GestureDescription
├── buildTapGesture()      - Create tap path
└── createPath()           - Build android.graphics.Path
```

**Design Decisions:**

- **Singleton Pattern**: Static instance allows event reception from MainActivity
- **Main Thread Dispatch**: All gestures execute on Main thread (UI thread) for reliability
- **Path-based Gestures**: Uses android.graphics.Path for precise stroke definition
- **Screen-relative Coordinates**: Calculates based on displayMetrics for device adaptability

**Coordinate System:**
```
Screen Layout (TikTok)
┌─────────────────────┐
│                     │  
│   Swipe Up ▲        │  
│   (1/3 height)      │
│                     │
│  ← Video Feed →     │  
│  Center = (W/2, H/2)│
│                     │
│   Swipe Down ▼      │
│   (2/3 height)      │
│                     │
└─────────────────────┘

Like tap: (W - 80px, H/2)  # Right side where heart appears
```

### 3. Overlay Module (overlay/)

**Responsibilities:**
- Display floating status indicator while TikTok is active
- Show face detection status and detected gestures
- Maintain foreground service for background persistence

**Key Classes:**

#### OverlayService (Foreground Service)
```
Type: Foreground service (camera foreground service type)
Lifecycle:
├── onCreate() - Initialize
├── onStartCommand() - Display notification & add overlay
├── onDestroy() - Remove overlay & cleanup

Notification:
└── "Gesture Recognition Active" with ongoing flag
```

#### OverlayView (Custom View)
```
Size: 120x120 dp circular shape
Content:
├── Gesture label text
├── Face detection indicator (color)
└── Status symbol (●, text, etc)

Updates:
├── updateGestureLabel() - Show detected gesture briefly
└── updateFaceDetection() - Show face detection status
```

**Design Decisions:**

- **Foreground Service**: Keeps camera running even when app is backgrounded
- **Circular Shape**: Mimics chat heads UI (familiar to users)
- **TYPE_APPLICATION_OVERLAY**: Works on all Android versions (with fallback)
- **FLAG_NOT_FOCUSABLE**: Allows interaction with apps beneath overlay

### 4. Utility Modules (util/)

#### PermissionsHelper
```
Functions:
├── isCameraPermissionGranted()
├── isOverlayPermissionGranted()
├── isAccessibilityServiceEnabled()
├── openOverlayPermissionSettings()
├── openAccessibilitySettings()
└── get[Runtime|Required]Permissions()

Design:
└── All static methods (singleton pattern)
```

#### NotificationHelper
```
Functions:
└── createNotificationChannels()

Design:
└── Single channel for all app notifications
```

### 5. MainActivity (Main Activity)

**Responsibilities:**
- Settings and onboarding UI
- Permission status display and request handling
- Camera initialization and lifecycle management
- Gesture event listening and logging
- Overlay service control

**Key Workflows:**

```
Startup Workflow:
1. onCreate() → Initialize UI & check permissions
2. setupPermissionButtons() → Attach click listeners
3. startGestureRecognition() → Initialize camera + vision

Recognition Workflow:
1. setupCamera() → Bind CameraX to lifecycle
2. FaceLandmarkAnalyzer analyzes frames
3. GestureDetector processes face data
4. Listen to gestureEvents SharedFlow
5. Route to GestureAccessibilityService
6. Service performs gesture in TikTok

Shutdown Workflow:
1. stopGestureRecognition() → Unbind camera
2. Release FaceLandmarkAnalyzer
3. Stop OverlayService
4. onDestroy() → Shutdown executor
```

## Data Flow

### Frame Processing Pipeline

```
Camera Frame
    ↓
CameraX ImageAnalysis.Analyzer
    ↓
FaceLandmarkAnalyzer.analyze()
    ├─ Convert ImageProxy to Bitmap
    ├─ Build MediaPipe Image object
    ├─ Run Face Landmarker inference
    └─ Calculate metrics
    ↓
FaceDetectionData emitted (SharedFlow)
    ↓
MainActivity collects FaceDetectionData
    ├─ Pass to GestureDetector.processFrame()
    ├─ Update debug UI with metrics
    └─ Listen to gestureEvents
    ↓
GestureDetector emits GestureEvent
    ├─ Apply temporal smoothing
    ├─ Check debounce timer
    ├─ If valid: emit event
    └─ Update last gesture timestamp
    ↓
MainActivity receives GestureEvent
    ├─ Log to debug console
    ├─ Convert to AccessibilityService command
    └─ Call GestureAccessibilityService.performGesture()
    ↓
GestureAccessibilityService processes command
    ├─ performSwipeDown() / performSwipeUp() / performDoubleTap()
    ├─ Build GestureDescription
    └─ Dispatch via dispatchGesture()
    ↓
TikTok receives gesture
    └─ Scrolls video / likes video
```

### Temporal Smoothing

```
Raw Frame Metrics:
L_EAR: [0.40, 0.12, 0.39, 0.38, ...]

3-Frame Rolling Average:
    Frame 1: Buffer = [0.40, 1.0, 1.0]  → avg = 0.80
    Frame 2: Buffer = [0.40, 0.12, 1.0]  → avg = 0.51
    Frame 3: Buffer = [0.40, 0.12, 0.39] → avg = 0.30
    Frame 4: Buffer = [0.12, 0.39, 0.38] → avg = 0.30

Result: Smoothed sequence [0.80, 0.51, 0.30, 0.30, ...]
        Reduces noise and false positives
```

### Debounce State Machine

```
State: IDLE (waiting for gesture)
├─ Timeout: never expires
└─ Action: Detect gestures

When gesture detected:
├─ lastGestureTime = now()
├─ State → DEBOUNCING
└─ Emit gesture event

State: DEBOUNCING
├─ Timeout: lastGestureTime + 1200ms
├─ Action: Ignore all gesture events
└─ onTimeout() → return to IDLE

Example Timeline:
t=0ms:     Right wink detected → emit SWIPE_DOWN
t=0-1200ms: Ignore wink attempts
t=1200ms:  Back to IDLE, ready for next gesture
```

## Key Design Patterns Used

### 1. **Sealed Class (GestureEvent)**
```kotlin
sealed class GestureEvent {
    object SwipeDown : GestureEvent()
    // ... type-safe events
}
```
**Benefit:** Type-safe event handling, compile-time exhaustiveness checking

### 2. **SharedFlow (Reactive Streams)**
```kotlin
private val _gestureEvents = MutableSharedFlow<GestureEvent>(extraBufferCapacity = 10)
val gestureEvents: SharedFlow<GestureEvent> = _gestureEvents
```
**Benefit:** Non-blocking event emission, hot observable, multiple subscribers

### 3. **Singleton Pattern (PermissionsHelper)**
```kotlin
object PermissionsHelper {
    fun isCameraPermissionGranted(): Boolean { ... }
}
```
**Benefit:** Global access, single instance, utility functions

### 4. **Data Class (FaceDetectionData)**
```kotlin
data class FaceDetectionData(
    val faceDetected: Boolean,
    val leftEyeAspectRatio: Float,
    // ...
)
```
**Benefit:** Automatic equals/hashCode, toString, copy methods

### 5. **Foreground Service Pattern**
```kotlin
startForeground(NOTIFICATION_ID, notification)
```
**Benefit:** Background persistence, user transparency, uninterruptible

## Thread Safety & Coroutines

### Thread Model

```
Main Thread (UI):
├─ UI updates (checkboxes, text)
├─ Camera binding
├─ Accessibility dispatching
└─ Debug console updates

Camera Executor Thread:
├─ Frame analysis (FaceLandmarkAnalyzer)
└─ MediaPipe inference

Coroutine Scope (Main):
├─ Camera setup (suspend function)
├─ Event collection (.collect())
└─ Delayed actions (delay())
```

### Coroutine Usage

```kotlin
// Setup camera (suspend)
lifecycleScope.launch(Dispatchers.Main) {
    setupCamera()  // Awaits ProcessCameraProvider
}

// Listen to events
lifecycleScope.launch {
    gestureDetector?.gestureEvents?.collect { gesture ->
        // Automatically on Main dispatcher
    }
}

// Delayed action (double-tap delay)
coroutineScope.launch(Dispatchers.Main) {
    delay(DOUBLE_TAP_DELAY_MS)
    // Execute second tap
}
```

## Performance Considerations

### Frame Processing Rate

```
Target: ~30 Hz (33ms per frame)
├─ CameraX: Newest frame only (backpressure strategy)
├─ MediaPipe: Inference time ~50-200ms
└─ Result: Effective rate ~5-20 Hz (slower on older devices)
```

### Memory Profile

```
MediaPipe Model: ~30 MB (loaded once)
Camera Buffers: ~10 MB (CameraX manages)
Gesture History: ~1 MB (smoothing buffers)
UI Components: ~5 MB
────────────────────────
Total: ~50 MB baseline
```

### Battery Impact

```
Component              Power Cost
─────────────────────────────────
Camera Operation       Medium
MediaPipe Inference    High
CameraX Processing    Medium
Overlay Rendering     Low
Services/Coroutines   Low
```

**Optimization Tips:**
- Increase frame skip interval (process every Nth frame)
- Reduce model input resolution
- Stop service when not needed
- Use release builds (ProGuard/R8 optimized)

## Security Considerations

### Permissions

```
Camera:          Sensitive (can spy on environment)
System Alert:    Dangerous (overlay abuse)
Accessibility:   Highest privilege (system control)
Foreground:      Moderate (background execution)
```

### Safe Practices

- Minimal permissions requested
- No internet access required
- No user data collection
- LocalOnly gesture storage
- Accessibility service only controls TikTok-visible area

## Extensibility Points

### Adding New Gestures

1. Add new event type to `GestureEvent.kt`:
   ```kotlin
   data object DoubleBlink : GestureEvent()
   ```

2. Add detection logic to `GestureDetector.kt`:
   ```kotlin
   if (leftEyeClosedCount > 1) {
       emit(GestureEvent.DoubleBlink)
   }
   ```

3. Add handler to `GestureAccessibilityService.kt`:
   ```kotlin
   GESTURE_DOUBLE_BLINK -> performCustomAction()
   ```

4. Add UI label to `strings.xml`:
   ```xml
   <string name="gesture_double_blink">Double Blink (Custom)</string>
   ```

### Adding Configuration Options

1. Create preferences file: `SharedPreferences` or `DataStore`
2. Add UI in MainActivity
3. Pass thresholds to GestureDetector
4. Persist across app restarts

---

**This architecture prioritizes:**
- ✅ Responsiveness (async/await, flows)
- ✅ Maintainability (modular, single responsibility)
- ✅ Reliability (error handling, debounce)
- ✅ Extensibility (sealed classes, configurable thresholds)
