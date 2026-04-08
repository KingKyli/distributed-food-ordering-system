# Complete startup script for Distributed Food Ordering System
# This script starts everything: emulator, server, and app

param(
    [switch]$SkipEmulator,
    [switch]$SkipBuild
)

Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "  Food Ordering System - Quick Start" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Magenta

$ErrorActionPreference = "Stop"

# 1. Start emulator if needed
if (-not $SkipEmulator) {
    Write-Host "[1/4] Starting Emulator..." -ForegroundColor Cyan
    & ".\start-emulator.ps1"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to start emulator" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[1/4] Skipping emulator start (already running)" -ForegroundColor Yellow
    # Still set up port forwarding
    adb reverse tcp:8765 tcp:8765
}

# 2. Build app if needed
if (-not $SkipBuild) {
    Write-Host "`n[2/4] Building app..." -ForegroundColor Cyan
    .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Build failed" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "`n[2/4] Skipping build" -ForegroundColor Yellow
}

# 3. Start server in background
Write-Host "`n[3/4] Starting server..." -ForegroundColor Cyan
# Kill any existing server process on port 8765
$serverProcess = Get-NetTCPConnection -LocalPort 8765 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -First 1
if ($serverProcess) {
    Write-Host "Killing existing server process..." -ForegroundColor Yellow
    Stop-Process -Id $serverProcess -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

# Start server
Start-Job -Name "ServerJob" -ScriptBlock {
    Set-Location "C:\Users\anezi\distributed-food-ordering-system"
    .\gradlew.bat :server:run
} | Out-Null

Write-Host "Waiting for server to start..." -ForegroundColor Yellow
$timeout = 30
$elapsed = 0
while ($elapsed -lt $timeout) {
    $listening = netstat -an | Select-String "8765.*LISTENING"
    if ($listening) {
        Write-Host "Server is running on port 8765" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 1
    $elapsed += 1
}

if ($elapsed -ge $timeout) {
    Write-Host "WARNING: Server might not have started properly" -ForegroundColor Yellow
}

# 4. Install and launch app
Write-Host "`n[4/4] Installing and launching app..." -ForegroundColor Cyan
.\gradlew.bat installDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Installation failed" -ForegroundColor Red
    exit 1
}

adb shell am start -n com.example.restaurantapp/.WelcomeActivity
Start-Sleep -Seconds 2

# Check if app is running
$currentFocus = adb shell dumpsys window | Select-String "mCurrentFocus"
if ($currentFocus -match "restaurantapp") {
    Write-Host "App launched successfully!" -ForegroundColor Green
} else {
    Write-Host "WARNING: App might not have launched properly" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "  System is ready!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "Emulator: Running" -ForegroundColor Green
Write-Host "Server: Running on port 8765" -ForegroundColor Green
Write-Host "App: Installed and launched" -ForegroundColor Green
Write-Host "`nUseful commands:" -ForegroundColor Cyan
Write-Host "  - View server logs: Receive-Job -Name ServerJob -Keep" -ForegroundColor White
Write-Host "  - Stop server: Stop-Job -Name ServerJob; Remove-Job -Name ServerJob" -ForegroundColor White
Write-Host "  - View app logs: adb logcat -s RestaurantApp:*" -ForegroundColor White
Write-Host "========================================`n" -ForegroundColor Magenta

