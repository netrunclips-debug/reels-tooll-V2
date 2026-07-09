# Building & Using GestureTikTok on Android

## 🤔 Understanding the Build Process

### Important: Where to Build

**Android apps CANNOT be built directly on Android devices.** You need a computer (Windows, Mac, or Linux) with Android Studio or command-line tools to compile the APK.

However, you can **use/test the built APK on Android devices.**

---

## 📋 Your Options

### Option 1: Build on Windows (Recommended)
✅ **Best for:** Most users  
✅ **Requirements:** Windows PC with Android Studio  
✅ **Difficulty:** Medium  
✅ See: [Windows Build Guide](#option-1-build-on-windows-pc)

### Option 2: Build on Mac or Linux
✅ **Best for:** Mac/Linux users  
✅ **Requirements:** Mac or Linux computer  
✅ **Difficulty:** Medium  
✅ See: [Mac/Linux Build Guide](#option-2-build-on-mac-or-linux)

### Option 3: Use Pre-built APK (Easiest)
✅ **Best for:** No build setup needed  
✅ **Requirements:** Just Android device  
✅ **Difficulty:** Easy  
✅ See: [Install Pre-built APK](#option-3-install-pre-built-apk-easiest)

### Option 4: Build on Android Phone (Advanced)
⚠️ **Best for:** No computer available  
⚠️ **Requirements:** Termux app + tools  
⚠️ **Difficulty:** Hard  
✅ See: [Build on Android Phone](#option-4-build-on-android-phone-advanced)

---

## Option 1: Build on Windows PC

### Step 1: Install Requirements

#### A. Install Java
```powershell
# Download Java 11 from: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
# Or use installer if already have Java

# Verify installation
java -version
```

#### B. Install Android Studio
1. Download from: https://developer.android.com/studio
2. Run installer
3. Choose "Standard Installation"
4. Wait for downloads (2-3 GB)

#### C. Configure Android SDK
1. Open Android Studio
2. Go to: **File > Settings > Languages & Frameworks > Android SDK**
3. Install:
   - Android SDK Platform 34
   - Android SDK Build Tools 34.x.x
   - Android Emulator (optional, if testing)

### Step 2: Download Project

```powershell
# Create projects folder
mkdir C:\Users\$env:USERNAME\Documents\Android-Projects
cd C:\Users\$env:USERNAME\Documents\Android-Projects

# Download the project
# Option A: If you have git installed
git clone <your-project-url> GestureTikTok

# Option B: Download ZIP and extract
# Extract to: C:\Users\$env:USERNAME\Documents\Android-Projects\GestureTikTok
```

### Step 3: Download MediaPipe Model

```powershell
cd C:\Users\$env:USERNAME\Documents\Android-Projects\GestureTikTok\app\src\main

# Create assets folder if not exists
mkdir assets -Force

cd assets

# Download model (~30-40 MB, may take 1-2 minutes)
curl -L "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task" `
  -o face_landmarker_v2_with_blendshapes.task

# Verify download (should be 30-40 MB)
Get-Item face_landmarker_v2_with_blendshapes.task | Select-Object Length

# Go back to project root
cd ..\..\..\..\
```

### Step 4: Build APK in Android Studio

**Method A: Using Android Studio GUI (Easiest)**

1. Open Android Studio
2. Click: **File > Open** → Select project folder
3. Wait for Gradle sync (5-10 minutes first time)
4. Click: **Build > Build Bundle(s) / APK(s) > Build APK(s)**
5. Wait for build to complete (~2-3 minutes)
6. Success message at bottom right
7. APK location: `app/build/outputs/apk/debug/app-debug.apk`

**Method B: Using Command Line**

```powershell
# Navigate to project
cd C:\Users\$env:USERNAME\Documents\Android-Projects\GestureTikTok

# Build APK
.\gradlew assembleDebug

# Or with verbose output
.\gradlew assembleDebug --info
```

### Step 5: Install on Android Device

#### Via Android Studio:
1. Connect Android device via USB
2. Enable USB Debugging on device: **Settings > Developer Options > USB Debugging**
3. Click: **Run > Run 'app'**
4. Select device and click OK

#### Via Command Line (adb):
```powershell
# List connected devices
adb devices

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# If already installed, use -r flag to replace
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Verify installation
adb shell pm list packages | grep gesturetiktok
```

### Step 6: Test on Android Device

1. **Disconnect USB** (if using USB debugging)
2. Launch app: Find "GestureTikTok" in app drawer
3. Grant permissions when prompted
4. Tap "Start Gesture Recognition"
5. Open TikTok app
6. Try gestures:
   - Wink right eye → scroll down
   - Wink left eye → scroll up
   - Open mouth → like video

---

## Option 2: Build on Mac or Linux

### Mac Setup

```bash
# 1. Install Java (if not already installed)
brew install openjdk@11

# 2. Install Android Studio
brew install --cask android-studio

# 3. Download project
cd ~/Projects
git clone <your-project-url> GestureTikTok
cd GestureTikTok

# 4. Download MediaPipe model
mkdir -p app/src/main/assets
cd app/src/main/assets
curl -L "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task" \
  -o face_landmarker_v2_with_blendshapes.task
cd ../../../../

# 5. Build
./gradlew assembleDebug

# 6. Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Linux Setup (Ubuntu/Debian)

```bash
# 1. Install Java
sudo apt-get update
sudo apt-get install openjdk-11-jdk

# 2. Install Android Studio
# Download from: https://developer.android.com/studio
# Or use snap: sudo snap install android-studio --classic

# 3. Download project
cd ~/Projects
git clone <your-project-url> GestureTikTok
cd GestureTikTok

# 4. Download MediaPipe model
mkdir -p app/src/main/assets
cd app/src/main/assets
curl -L "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task" \
  -o face_landmarker_v2_with_blendshapes.task
cd ../../../../

# 5. Build
./gradlew assembleDebug

# 6. Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Option 3: Install Pre-built APK (Easiest)

### If Someone Already Built the APK

**Step 1: Get APK File**
- Request the built `app-debug.apk` file
- Or download from release/website

**Step 2: Transfer to Android Device**

#### Method A: USB Cable + ADB
```bash
# On computer with APK file
adb install path/to/app-debug.apk
```

#### Method B: Bluetooth/Cloud Transfer
1. Upload APK to: Google Drive, Dropbox, or Email
2. Download on Android phone
3. Open file manager → Tap APK to install

#### Method C: Direct Download Link
1. Get download link to APK
2. Tap link on Android phone
3. Tap "Install" when prompted

**Step 3: Grant Permissions**
1. Launch app
2. Tap "Grant Camera Permission"
3. Tap "Grant Overlay Permission"
4. Tap "Enable Accessibility Service"
5. Follow system prompts

**Step 4: Use App**
1. Tap "Start Gesture Recognition"
2. Open TikTok
3. Try gestures

---

## Option 4: Build on Android Phone (Advanced)

⚠️ **WARNING:** This is complex and requires advanced setup. Only for users without a computer.

### Install Termux

1. Install from: https://f-droid.org/en/packages/com.termux/ (F-Droid store)
   - Or Play Store: "Termux"
2. Open Termux app
3. Grant storage permissions when prompted

### Setup Build Environment

```bash
# Update package manager
pkg update && pkg upgrade -y

# Install Java
pkg install -y openjdk-17

# Install Git
pkg install -y git

# Install Gradle (if not included)
pkg install -y gradle

# Verify installations
java -version
git --version
gradle --version
```

### Download Project

```bash
# Create workspace
mkdir ~/workspace
cd ~/workspace

# Clone project (requires ~500 MB storage)
git clone <your-project-url> GestureTikTok
cd GestureTikTok
```

### Download MediaPipe Model

```bash
# Create assets folder
mkdir -p app/src/main/assets
cd app/src/main/assets

# Download model (this is slow on phones - ~10 minutes)
wget "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task"

# Or using curl
curl -L "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task" \
  -o face_landmarker_v2_with_blendshapes.task

cd ../../../../
```

### Build APK

```bash
# Navigate to project root
cd ~/workspace/GestureTikTok

# Build (this takes 15-30 minutes on phone)
./gradlew assembleDebug

# Output will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Install APK

```bash
# Install locally
adb install app/build/outputs/apk/debug/app-debug.apk

# Or copy to another device
# From Termux file manager, find the APK and share via Bluetooth/cloud
```

### Issues & Limitations

⚠️ **Problems you might face:**
- **Storage:** Needs 2-3 GB free space
- **RAM:** Phone must have 4GB+ RAM
- **Speed:** Takes 20-30 minutes to build
- **Heat:** Phone gets hot during build
- **Build might fail** if phone runs out of memory

**Better Alternative:** Use a computer if possible

---

## 📱 On Your Android Device

### Installation via File Manager (Easiest for Pre-built APK)

1. **Copy APK to phone** (USB, Bluetooth, or cloud)
2. **Open file manager**
3. **Navigate to APK file**
4. **Tap to install**
5. **Grant "Unknown Sources" permission** if prompted
6. **Wait for installation** (20-30 seconds)
7. **Tap "Open"** to launch app

### First-Time Setup

1. **Launch the app** - Should show main screen
2. **Grant Camera Permission**
   - Tap "Grant Camera Permission"
   - System dialog appears
   - Tap "Allow"
3. **Grant Overlay Permission**
   - Tap "Grant Overlay Permission"
   - Settings page opens
   - Find "GestureTikTok" in list
   - Toggle "Display over other apps" ON
   - Go back to app
4. **Enable Accessibility Service**
   - Tap "Enable Accessibility Service"
   - Settings page opens
   - Find "GestureTikTok Gesture Control" in services
   - Toggle ON
   - Confirm any security warnings
   - Go back to app
5. **All permissions should now show ✓**

### Testing the App

1. **Tap "Start Gesture Recognition"** - Status shows "Running"
2. **Open TikTok app**
3. **Position face toward camera** - Debug shows "Face detected"
4. **Try gestures:**

| Gesture | Action | Result |
|---------|--------|--------|
| Wink right eye | Hold wink 0.2-0.5 sec | Scroll DOWN (next video) |
| Wink left eye | Hold wink 0.2-0.5 sec | Scroll UP (previous video) |
| Open mouth wide | Quick mouth open | LIKE video |
| Smile big | Hold smile 1+ sec | LIKE video |

### Troubleshooting on Device

**Problem: "No face detected"**
- Move face 20-50 cm from camera
- Ensure good lighting
- Face should be centered in camera

**Problem: "Permissions not granted"**
- Go to Settings > Apps > GestureTikTok > Permissions
- Grant Camera permission manually
- Go to Settings > Accessibility > find GestureTikTok > Toggle ON

**Problem: "Gestures not working in TikTok"**
- Verify accessibility service is ON (Settings > Accessibility)
- Try swiping manually to verify it works
- Close and reopen TikTok app

---

## 🚀 Recommended Workflow

### For Most Users (Windows PC)

```
1. Install Android Studio on Windows PC
2. Download project
3. Download MediaPipe model
4. Build APK in Android Studio (via GUI)
5. Connect Android phone via USB
6. Install APK via "Run" button in Android Studio
7. Test on phone
```

**Total time:** ~1 hour (first time)

### For Mac/Linux Users

Same as above, but use command line:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### If No Computer Available

Use **Option 4** (build on Termux) - difficult but possible

---

## 📊 Build Times

| Method | Time |
|--------|------|
| Windows PC (first build) | 5-10 minutes |
| Windows PC (incremental) | 1-2 minutes |
| Mac (first build) | 5-10 minutes |
| Linux (first build) | 5-10 minutes |
| Android Phone (first build) | 20-30 minutes |
| Android Phone (incremental) | 10-15 minutes |

---

## ✅ Quick Reference

### Windows
```powershell
# 1. Download model
mkdir app/src/main/assets
cd app/src/main/assets
curl -L "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task" -o face_landmarker_v2_with_blendshapes.task
cd ..\..\..\..\

# 2. Build
.\gradlew assembleDebug

# 3. Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Mac/Linux
```bash
# 1. Download model
mkdir -p app/src/main/assets
cd app/src/main/assets
curl -L "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task" -o face_landmarker_v2_with_blendshapes.task
cd ../../../../

# 2. Build
./gradlew assembleDebug

# 3. Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Android Phone (Termux)
```bash
# Setup (one time)
pkg update && pkg upgrade -y
pkg install -y openjdk-17 git

# Build
git clone <url>
cd GestureTikTok
mkdir -p app/src/main/assets
cd app/src/main/assets
wget "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task"
cd ../../../../
./gradlew assembleDebug
```

---

## 📞 Need Help?

- **Can't build?** See: QUICKSTART.md or TROUBLESHOOTING.md
- **APK won't install?** Check device storage (need 200 MB free)
- **App crashes?** Ensure MediaPipe model file is correct size (30-40 MB)
- **Gestures not working?** Verify accessibility service is enabled

---

**Start with Option 1 (Windows) if you have a PC - it's the easiest!** 🚀
