# ✅ GestureTikTok - One-Page Quick Reference

## 🚀 FASTEST BUILD (Windows)

### Step 1: Double-Click Script
```
📁 Project folder
  └─ 📄 BUILD.bat
     (just double-click this!)
```

### Step 2: Wait & Follow Prompts
The script will automatically:
- ✅ Check Java
- ✅ Create folders
- ✅ Download model (~40 MB)
- ✅ Build APK

**Time:** 5-10 minutes first run

---

## 📱 INSTALL ON PHONE

### Method 1: USB Cable + ADB (Best)
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Method 2: Copy File
- Find: `app\build\outputs\apk\debug\app-debug.apk`
- Copy to phone (USB/Bluetooth/Cloud)
- Tap APK to install on phone

### Method 3: Android Studio
- Connect phone
- Click: Run → Run 'app'

---

## 📋 PERMISSIONS SETUP (On Phone)

After app installs, grant permissions in this order:

| Step | Action | Where |
|------|--------|-------|
| 1️⃣ | Camera | Tap "Grant Camera Permission" in app |
| 2️⃣ | Overlay | Tap "Grant Overlay Permission" in app |
| 3️⃣ | Accessibility | Tap "Enable Accessibility Service" in app |

**All checkmarks should show ✓**

---

## 🎮 TEST IT

### Launch App
- ✅ Tap "Start Gesture Recognition"
- ✅ Open TikTok

### Try Gestures
| Gesture | Action | Result |
|---------|--------|--------|
| 👁️ Right wink | Hold 0.2-0.5s | ⬇️ Next video |
| 👁️ Left wink | Hold 0.2-0.5s | ⬆️ Prev video |
| 👄 Open mouth | Quick open | ❤️ Like |
| 😊 Smile | Hold 1+ second | ❤️ Like |

---

## 🆘 QUICK FIX

| Problem | Fix |
|---------|-----|
| Script won't run | Install Java 11 first |
| Build fails | Run: `.\gradlew clean assembleDebug` |
| APK won't install | Enable "Unknown Sources" on phone |
| No face detected | Check camera permission |
| Gestures don't work | Check accessibility service is ON |

---

## 📂 KEY FILES

```
Your Project:
├─ 📄 BUILD.bat           ← DOUBLE-CLICK THIS
├─ 📄 BUILD.ps1           ← Or this (PowerShell)
├─ 📄 EASY-START.md       ← Read this first
├─ 📄 QUICKSTART.md       ← Detailed instructions
├─ 📄 TROUBLESHOOTING.md  ← If something breaks
└─ app/
   └─ build/
      └─ outputs/
         └─ apk/
            └─ debug/
               └─ app-debug.apk  ← Your APK!
```

---

## ⏱️ TIME ESTIMATES

| Task | Time |
|------|------|
| Run BUILD.bat (first run) | 5-10 min |
| Run BUILD.bat (after first) | 1-2 min |
| Install APK | 1-2 min |
| Setup permissions on phone | 3-5 min |
| Test & play | ∞ (have fun!) |

**Total:** ~15-20 minutes first time

---

## 📞 DOCS

- 🟢 **Just want it working?** → EASY-START.md
- 🟡 **Need step-by-step?** → QUICKSTART.md
- 🔴 **Something broke?** → TROUBLESHOOTING.md
- 🔵 **Want tech details?** → ARCHITECTURE.md

---

## ✨ PRO TIPS

1. **Adjust sensitivity:** Edit thresholds in `GestureDetector.kt`
2. **Faster builds:** Run `.\gradlew clean` if stuck
3. **Battery drain:** Stop app when not using TikTok
4. **Better accuracy:** Good lighting helps face detection
5. **Distance matters:** Keep face 20-50 cm from camera

---

## 🎯 What's Installed

✅ **Milestones Completed:**
- M1: Camera + face detection
- M2: Gesture detection (4 gestures)
- M3: Accessibility service (swipes + taps)
- M4: End-to-end TikTok control
- M5: Floating overlay UI
- M6: Full settings & debugging

✅ **Ready to Use:**
- No Python needed
- No cross-compilation
- Pure native Android
- ~3000 lines of Kotlin code
- Comprehensive documentation

---

## 🚀 You're Ready!

### Next Action:
```
1. Find: BUILD.bat
2. Double-click
3. Wait
4. Install on phone
5. Have fun!
```

---

**Questions?** Check **EASY-START.md** or **QUICKSTART.md**
**Still stuck?** See **TROUBLESHOOTING.md**
**Want details?** Read **ARCHITECTURE.md**

---

**Built with ❤️ for hands-free TikTok browsing**
