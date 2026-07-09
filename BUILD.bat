@echo off
REM ============================================================================
REM GestureTikTok - Automated Build & Setup Script for Windows
REM ============================================================================
REM This script automates the entire process:
REM   1. Creates folder structure
REM   2. Downloads MediaPipe model
REM   3. Builds APK
REM   4. Installs on Android device
REM ============================================================================

setlocal enabledelayedexpansion
color 0A
cls

echo.
echo ============================================================================
echo   GESTURETIKTOK - AUTOMATED BUILD SETUP (Windows)
echo ============================================================================
echo.

REM Get the project directory
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

if not exist "build.gradle.kts" (
    color 0C
    echo ERROR: build.gradle.kts not found!
    echo Please run this script from the project root directory.
    echo Current directory: %PROJECT_DIR%
    pause
    exit /b 1
)

echo [1/5] Checking requirements...
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    color 0C
    echo ERROR: Java is not installed or not in PATH
    echo Download Java 11 from: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
    echo Or install: choco install openjdk11
    pause
    exit /b 1
)
echo ✓ Java found
java -version

echo.
echo [2/5] Creating folder structure...
echo.

REM Create assets folder
if not exist "app\src\main\assets" (
    mkdir "app\src\main\assets"
    echo ✓ Created: app\src\main\assets
) else (
    echo ✓ Folder already exists: app\src\main\assets
)

cd "app\src\main\assets"

echo.
echo [3/5] Downloading MediaPipe Face Landmarker model...
echo.
echo NOTE: This is ~30-40 MB and may take 1-2 minutes on first run
echo.

REM Check if model already exists
if exist "face_landmarker_v2_with_blendshapes.task" (
    echo ✓ Model file already exists (skipping download)
    dir "face_landmarker_v2_with_blendshapes.task"
) else (
    echo Downloading model from Google MediaPipe...
    powershell -Command "& {$ProgressPreference = 'Continue'; Invoke-WebRequest -Uri 'https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task' -OutFile 'face_landmarker_v2_with_blendshapes.task' -UseBasicParsing}"
    
    if errorlevel 1 (
        color 0C
        echo ERROR: Failed to download MediaPipe model
        echo Try downloading manually from:
        echo https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task
        echo.
        cd ..\..\..\..\
        pause
        exit /b 1
    )
    
    echo ✓ Model downloaded successfully
    dir "face_landmarker_v2_with_blendshapes.task"
)

cd ..\..\..\..\

echo.
echo [4/5] Building APK...
echo.
echo This will take 2-5 minutes on first run (gradle dependencies download)
echo.

REM Run gradle build
call gradlew assembleDebug

if errorlevel 1 (
    color 0C
    echo.
    echo ERROR: Build failed!
    echo Try running: gradlew clean assembleDebug
    pause
    exit /b 1
)

echo.
color 0B
echo ✓ BUILD SUCCESSFUL!
echo.
echo APK created at: app\build\outputs\apk\debug\app-debug.apk
echo.

echo [5/5] Next steps...
echo.
echo Option A - Install via ADB (Android device connected via USB):
echo   1. Enable USB Debugging on your Android device:
echo      Settings ^> Developer Options ^> USB Debugging (ON)
echo   2. Connect device via USB cable
echo   3. Run: adb install -r app/build/outputs/apk/debug/app-debug.apk
echo.
echo Option B - Install manually:
echo   1. Open File Explorer
echo   2. Navigate to: app\build\outputs\apk\debug\app-debug.apk
echo   3. Copy to Android phone (USB/Bluetooth/Cloud)
echo   4. Tap APK on phone to install
echo.
echo Option C - Install via Android Studio:
echo   1. Open Android Studio
echo   2. Click Run ^> Run 'app'
echo   3. Select your device
echo.

echo.
echo ============================================================================
echo   FIRST-TIME SETUP ON ANDROID DEVICE
echo ============================================================================
echo.
echo After installation, the app will ask for permissions:
echo.
echo 1. Camera Permission
echo    - Tap "Grant Camera Permission"
echo    - Allow camera access in system dialog
echo.
echo 2. Overlay Permission
echo    - Tap "Grant Overlay Permission"
echo    - Go to Settings ^> Apps ^> Permissions ^> Display over other apps
echo    - Find GestureTikTok and toggle ON
echo.
echo 3. Accessibility Service
echo    - Tap "Enable Accessibility Service"
echo    - Go to Settings ^> Accessibility
echo    - Find "GestureTikTok Gesture Control"
echo    - Toggle ON (confirm any security warnings)
echo.
echo Once all permissions are granted, you're ready to use!
echo.

color 0B
echo ✓ Setup complete!
echo.
pause
