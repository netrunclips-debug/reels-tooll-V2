# 🚀 START HERE - Easiest Way to Build & Install

## ⚡ The Fastest Way (Windows)

### Step 1: Double-Click the Script
```
1. Open File Explorer
2. Navigate to: C:\Users\YourUsername\Documents\GitHub\new\
3. Find: BUILD.bat
4. Double-click to run
5. Wait for the script to complete
```

**That's it!** The script does everything automatically:
- ✅ Downloads MediaPipe model (~40 MB)
- ✅ Builds the APK
- ✅ Tells you what to do next

---

## 📱 Install on Android Phone

### Option A: Via USB Cable (Best)

**Prerequisites:**
- Android phone with USB cable
- USB Debugging enabled on phone

**Steps:**

1. **Enable USB Debugging on Phone:**
   - Settings → About Phone → tap "Build Number" 7 times
   - Settings → Developer Options → Enable "USB Debugging"

2. **Connect Phone to Computer**
   - Plug in USB cable
   - Allow USB debugging when prompted on phone

3. **Install APK:**
   ```powershell
   cd C:\Users\YourUsername\Documents\GitHub\new
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

4. **App installs automatically** ✓

---

### Option B: File Transfer (Easy, No Cable)

1. **Find the APK:**
   ```
   C:\Users\YourUsername\Documents\GitHub\new\app\build\outputs\apk\debug\app-debug.apk
   ```

2. **Send to Phone:**
   - Email to yourself and download
   - Upload to Google Drive, Dropbox, OneDrive
   - Share via Bluetooth/WiFi File Transfer

3. **On Your Phone:**
   - Tap the APK file to install
   - When asked "Unknown app source?", tap "Install anyway"
   - Wait for installation ✓

---

### Option C: Drag & Drop to Android Studio

1. **Open Android Studio**
2. **Connect phone via USB**
3. **Drag APK file into Android Studio window**
4. **Confirm installation**
5. **App installs automatically** ✓

---

## ✅ First-Time Setup on Phone

After installation, open the app and:

### 1️⃣ Grant Camera Permission
- Tap "Grant Camera Permission"
- System dialog appears
- Tap "Allow"

### 2️⃣ Grant Overlay Permission  
- Tap "Grant Overlay Permission"
- Settings opens automatically
- Find: Settings → Apps → Permissions → Display over other apps
- Find GestureTikTok in list
- Toggle ON
- Go back to app

### 3️⃣ Enable Accessibility Service
- Tap "Enable Accessibility Service"
- Settings opens to Accessibility
- Find: "GestureTikTok Gesture Control"
- Toggle ON
- Confirm any security messages
- Go back to app

### ✓ All Done!

---

## 🎮 Now Test It

1. **In the App:**
   - Tap "Start Gesture Recognition"
   - Look at camera - should see "Face detected" in debug area

2. **Open TikTok:**
   - Launch the TikTok app
   - Start watching a video

3. **Try Gestures:**
   - **Wink right eye** → Scrolls DOWN (next video)
   - **Wink left eye** → Scrolls UP (previous video)
   - **Open mouth wide** → LIKES video
   - **Smile for 1 second** → LIKES video

---

## 🆘 Troubleshooting

### Script Failed to Run?

**Error: "Java not found"**
- Download Java 11: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
- Install it
- Restart the script

**Error: "gradle command not found"**
- Make sure you're in the correct folder when running the script
- Run the script from: `C:\Users\YourUsername\Documents\GitHub\new\`

**Error: Download failed**
- Check internet connection
- Try manual download: https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task
- Save to: `app\src\main\assets\face_landmarker_v2_with_blendshapes.task`

---

### Installation Failed?

**"ADB not found"**
- Install Android SDK Platform Tools
- Or use File Transfer option instead

**"APK already installed"**
- That's fine! Use: `adb install -r app\build\outputs\apk\debug\app-debug.apk` (the `-r` replaces it)

**Phone not detected**
- Enable USB Debugging on phone
- Install USB driver for your phone brand
- Try different USB port
- Restart phone and computer

---

### App Crashes After Install?

**"MediaPipe model not found"**
- Make sure file exists at: `app/src/main/assets/face_landmarker_v2_with_blendshapes.task`
- Check file size is 30-40 MB
- Re-run BUILD.bat script

**"Permission denied"**
- Make sure you granted all 3 permissions
- Settings → Apps → GestureTikTok → Permissions
- Enable Camera permission
- Go to Accessibility → Enable service

---

## 📊 What the Script Does

```
BUILD.bat Script Flow:
├─ [1/5] Check Java installation ✓
├─ [2/5] Create app/src/main/assets folder ✓
├─ [3/5] Download MediaPipe model (~30-40 MB) ✓
├─ [4/5] Build APK using gradle ✓
└─ [5/5] Show next steps ✓
```

**Total Time:** 
- First run: 5-10 minutes (gradle downloads dependencies)
- Subsequent runs: 1-2 minutes

---

## 🎯 Summary

### To Build:
```
1. Double-click: BUILD.bat
2. Wait for completion
3. Done!
```

### To Install:
```
Option A: adb install -r app\build\outputs\apk\debug\app-debug.apk
Option B: Copy APK to phone and tap to install
Option C: Drag APK into Android Studio
```

### To Use:
```
1. Open app
2. Grant 3 permissions
3. Open TikTok
4. Try gestures
5. Enjoy! 🎉
```

---

## 📞 Still Stuck?

See the full guides:
- **QUICKSTART.md** - Detailed step-by-step
- **TROUBLESHOOTING.md** - Common problems & solutions
- **BUILD-AND-INSTALL-GUIDE.md** - Multiple build options

---

**You've got this! 🚀**
