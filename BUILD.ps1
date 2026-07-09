# ============================================================================
# GestureTikTok - Automated Build & Setup Script for Windows PowerShell
# ============================================================================
# Usage: Right-click > Run with PowerShell
# Or: Open PowerShell and run: powershell -File BUILD.ps1
# ============================================================================

param(
    [switch]$SkipModel = $false,
    [switch]$SkipBuild = $false
)

# Set colors
$successColor = "Green"
$errorColor = "Red"
$infoColor = "Cyan"
$warningColor = "Yellow"

# Clear screen
Clear-Host

Write-Host ""
Write-Host "============================================================================" -ForegroundColor $infoColor
Write-Host "  GESTURETIKTOK - AUTOMATED BUILD SETUP (Windows PowerShell)" -ForegroundColor $infoColor
Write-Host "============================================================================" -ForegroundColor $infoColor
Write-Host ""

# Get project directory
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

Write-Host "[1/5] Verifying project structure..." -ForegroundColor $infoColor
if (-not (Test-Path "build.gradle.kts")) {
    Write-Host "ERROR: build.gradle.kts not found!" -ForegroundColor $errorColor
    Write-Host "Please run this script from the project root directory." -ForegroundColor $errorColor
    Write-Host "Current directory: $projectDir" -ForegroundColor $errorColor
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "✓ Project structure verified" -ForegroundColor $successColor
Write-Host ""

# Check Java
Write-Host "[2/5] Checking Java installation..." -ForegroundColor $infoColor
try {
    $javaVersion = java -version 2>&1
    Write-Host "✓ Java found:" -ForegroundColor $successColor
    Write-Host "$javaVersion" -ForegroundColor $infoColor
} catch {
    Write-Host "ERROR: Java is not installed or not in PATH" -ForegroundColor $errorColor
    Write-Host "Download Java 11 from: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html" -ForegroundColor $warningColor
    Write-Host "Or install via: choco install openjdk11" -ForegroundColor $warningColor
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host ""

# Create folder structure
Write-Host "[3/5] Creating folder structure..." -ForegroundColor $infoColor
$assetsDir = "app\src\main\assets"
if (-not (Test-Path $assetsDir)) {
    New-Item -ItemType Directory -Path $assetsDir -Force | Out-Null
    Write-Host "✓ Created: $assetsDir" -ForegroundColor $successColor
} else {
    Write-Host "✓ Folder already exists: $assetsDir" -ForegroundColor $successColor
}
Write-Host ""

# Download MediaPipe model
if (-not $SkipModel) {
    Write-Host "[4/5] Downloading MediaPipe Face Landmarker model..." -ForegroundColor $infoColor
    Write-Host "NOTE: This is ~30-40 MB and may take 1-2 minutes" -ForegroundColor $warningColor
    Write-Host ""
    
    $modelFile = Join-Path $assetsDir "face_landmarker_v2_with_blendshapes.task"
    $modelUrl = "https://storage.googleapis.com/mediapipe-tasks/vision/face_landmarker/face_landmarker_v2_with_blendshapes.task"
    
    if (Test-Path $modelFile) {
        $fileSize = (Get-Item $modelFile).Length / 1MB
        Write-Host "✓ Model file already exists (${fileSize:F1} MB)" -ForegroundColor $successColor
    } else {
        try {
            Write-Host "Downloading from Google MediaPipe..." -ForegroundColor $infoColor
            $ProgressPreference = 'Continue'
            Invoke-WebRequest -Uri $modelUrl -OutFile $modelFile -UseBasicParsing
            
            $fileSize = (Get-Item $modelFile).Length / 1MB
            Write-Host "✓ Model downloaded successfully (${fileSize:F1} MB)" -ForegroundColor $successColor
        } catch {
            Write-Host "ERROR: Failed to download model" -ForegroundColor $errorColor
            Write-Host "Manual download from:" -ForegroundColor $warningColor
            Write-Host "$modelUrl" -ForegroundColor $warningColor
            Read-Host "Press Enter to exit"
            exit 1
        }
    }
} else {
    Write-Host "[4/5] Skipping model download (-SkipModel flag set)" -ForegroundColor $warningColor
}
Write-Host ""

# Build APK
if (-not $SkipBuild) {
    Write-Host "[5/5] Building APK..." -ForegroundColor $infoColor
    Write-Host "This will take 2-5 minutes on first run (gradle dependencies)" -ForegroundColor $warningColor
    Write-Host ""
    
    $env:GRADLE_OPTS = "-Xmx2048m"
    
    & .\gradlew assembleDebug
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "ERROR: Build failed!" -ForegroundColor $errorColor
        Write-Host "Try running: .\gradlew clean assembleDebug" -ForegroundColor $warningColor
        Read-Host "Press Enter to exit"
        exit 1
    }
} else {
    Write-Host "[5/5] Skipping build (-SkipBuild flag set)" -ForegroundColor $warningColor
}
Write-Host ""

# Success message
Write-Host "============================================================================" -ForegroundColor $successColor
Write-Host "✓ BUILD SUCCESSFUL!" -ForegroundColor $successColor
Write-Host "============================================================================" -ForegroundColor $successColor
Write-Host ""

$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "APK created: $apkPath (${apkSize:F1} MB)" -ForegroundColor $successColor
    Write-Host ""
}

# Next steps
Write-Host "NEXT STEPS:" -ForegroundColor $infoColor
Write-Host ""
Write-Host "Option A - Install via ADB (Android device via USB):" -ForegroundColor $warningColor
Write-Host "  1. Enable USB Debugging on Android device" -ForegroundColor "Gray"
Write-Host "     Settings > Developer Options > USB Debugging (ON)" -ForegroundColor "Gray"
Write-Host "  2. Connect device via USB cable" -ForegroundColor "Gray"
Write-Host "  3. Run: adb install -r app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor "Gray"
Write-Host ""

Write-Host "Option B - Install manually:" -ForegroundColor $warningColor
Write-Host "  1. Copy APK to Android phone (USB/Bluetooth/Cloud)" -ForegroundColor "Gray"
Write-Host "  2. Tap APK file on phone to install" -ForegroundColor "Gray"
Write-Host ""

Write-Host "Option C - Install via Android Studio:" -ForegroundColor $warningColor
Write-Host "  1. Open Android Studio" -ForegroundColor "Gray"
Write-Host "  2. Click Run > Run 'app'" -ForegroundColor "Gray"
Write-Host "  3. Select your device" -ForegroundColor "Gray"
Write-Host ""

# First-time setup
Write-Host "============================================================================" -ForegroundColor $infoColor
Write-Host "FIRST-TIME SETUP ON ANDROID DEVICE" -ForegroundColor $infoColor
Write-Host "============================================================================" -ForegroundColor $infoColor
Write-Host ""
Write-Host "After installation, grant these permissions:" -ForegroundColor $warningColor
Write-Host ""
Write-Host "1. Camera Permission" -ForegroundColor "Gray"
Write-Host "   - Tap 'Grant Camera Permission'" -ForegroundColor "Gray"
Write-Host ""
Write-Host "2. Overlay Permission" -ForegroundColor "Gray"
Write-Host "   - Tap 'Grant Overlay Permission'" -ForegroundColor "Gray"
Write-Host "   - Go to Settings > Apps > Permissions > Display over other apps" -ForegroundColor "Gray"
Write-Host "   - Enable for GestureTikTok" -ForegroundColor "Gray"
Write-Host ""
Write-Host "3. Accessibility Service" -ForegroundColor "Gray"
Write-Host "   - Tap 'Enable Accessibility Service'" -ForegroundColor "Gray"
Write-Host "   - Go to Settings > Accessibility > GestureTikTok" -ForegroundColor "Gray"
Write-Host "   - Toggle ON" -ForegroundColor "Gray"
Write-Host ""

Write-Host "✓ Setup complete! You're ready to use GestureTikTok!" -ForegroundColor $successColor
Write-Host ""

Read-Host "Press Enter to exit"
