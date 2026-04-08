# Script to start the Android emulator for the Distributed Food Ordering System
# This script starts the emulator with optimized memory settings to avoid crashes

Write-Host "Starting Android Emulator..." -ForegroundColor Green

# Path to Android SDK
$sdkPath = "C:\Users\anezi\AppData\Local\Android\Sdk"
$emulatorExe = Join-Path $sdkPath "emulator\emulator.exe"

# AVD name
$avdName = "Medium_Phone_API_36.0"

# Check if emulator exists
if (-not (Test-Path $emulatorExe)) {
    Write-Host "ERROR: Emulator not found at $emulatorExe" -ForegroundColor Red
    exit 1
}

# Check if AVD exists
$avdList = & $emulatorExe -list-avds
if ($avdList -notcontains $avdName) {
    Write-Host "ERROR: AVD '$avdName' not found" -ForegroundColor Red
    Write-Host "Available AVDs:" -ForegroundColor Yellow
    & $emulatorExe -list-avds
    exit 1
}

# Check if emulator is already running
Write-Host "Checking for existing emulator..." -ForegroundColor Yellow
$existingEmulator = adb devices | Select-String "emulator-"
if ($existingEmulator) {
    Write-Host "Emulator is already running!" -ForegroundColor Green
    $bootCompleted = adb shell getprop sys.boot_completed 2>$null
    if ($bootCompleted -eq "1") {
        Write-Host "Emulator is fully booted and ready!" -ForegroundColor Green
        # Set up port forwarding
        Write-Host "Setting up port forwarding for server connection..." -ForegroundColor Cyan
        adb reverse tcp:8765 tcp:8765
        Write-Host "Port forwarding configured: tcp:8765 -> tcp:8765" -ForegroundColor Green
        Write-Host "`nYou can now run the app from Android Studio!" -ForegroundColor Magenta
        exit 0
    }
}

# Start emulator with reduced memory to avoid OOM errors
Write-Host "Launching emulator with optimized settings..." -ForegroundColor Cyan
Start-Process -FilePath $emulatorExe -ArgumentList "-avd", $avdName, "-memory", "2048", "-no-snapshot-load" -WindowStyle Normal

# Wait for device to be detected
Write-Host "Waiting for emulator to start..." -ForegroundColor Yellow
$timeout = 120
$elapsed = 0
while ($elapsed -lt $timeout) {
    $devices = adb devices | Select-String "emulator-"
    if ($devices) {
        Write-Host "Emulator detected!" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
    $elapsed += 2
    Write-Host "." -NoNewline
}

if ($elapsed -ge $timeout) {
    Write-Host "`nERROR: Emulator failed to start within $timeout seconds" -ForegroundColor Red
    exit 1
}

# Wait for boot to complete
Write-Host "`nWaiting for system to fully boot..." -ForegroundColor Yellow
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done'
Write-Host "Emulator is fully booted and ready!" -ForegroundColor Green

# Set up port forwarding
Write-Host "Setting up port forwarding for server connection..." -ForegroundColor Cyan
adb reverse tcp:8765 tcp:8765
Write-Host "Port forwarding configured: tcp:8765 -> tcp:8765" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "Emulator is ready!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Start server: .\gradlew.bat :server:run" -ForegroundColor White
Write-Host "  2. Install app: .\gradlew.bat installDebug" -ForegroundColor White
Write-Host "  3. Launch app: adb shell am start -n com.example.restaurantapp/.WelcomeActivity" -ForegroundColor White
Write-Host "========================================`n" -ForegroundColor Magenta

