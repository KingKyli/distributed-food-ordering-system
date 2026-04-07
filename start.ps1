# ============================================================
#  start.ps1 — One-click launcher for Distributed Food App
#  Run from project root: .\start.ps1
# ============================================================

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$AdbPath     = "$env:LOCALAPPDATA\Android\Sdk\platform-tools"
$ServerPort  = 8765

# Add ADB to PATH if not already there
if ($env:Path -notlike "*platform-tools*") {
    $env:Path += ";$AdbPath"
}

Write-Host ""
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "  Distributed Food App — Launcher" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

# ── 1. Kill old MockServer if running on port 8765 ──────────
$existing = netstat -ano | Select-String ":$ServerPort " | ForEach-Object {
    ($_ -split '\s+')[-1]
} | Select-Object -First 1

if ($existing) {
    Write-Host "[1/4] Stopping old server (PID $existing)..." -ForegroundColor Yellow
    Stop-Process -Id $existing -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
} else {
    Write-Host "[1/4] No old server running." -ForegroundColor Green
}

# ── 2. Start MockServer in background window ─────────────────
Write-Host "[2/4] Starting MockServer on port $ServerPort..." -ForegroundColor Yellow
Set-Location $ProjectRoot
Start-Process -FilePath "$ProjectRoot\gradlew.bat" -ArgumentList ":server:run" `
    -WorkingDirectory $ProjectRoot -WindowStyle Normal
Start-Sleep -Seconds 2

# Verify server started
$listening = netstat -ano | Select-String ":$ServerPort "
if ($listening) {
    Write-Host "      MockServer is RUNNING on port $ServerPort with SQLite persistence ✓" -ForegroundColor Green
} else {
    Write-Host "      ERROR: MockServer did not start!" -ForegroundColor Red
    exit 1
}

# ── 3. ADB reverse port forwarding ───────────────────────────
Write-Host "[3/4] Setting up ADB port forwarding..." -ForegroundColor Yellow
$devices = adb devices 2>&1 | Where-Object { $_ -match "device$" }

if (-not $devices) {
    Write-Host "      No Android device/emulator found via ADB." -ForegroundColor Red
    Write-Host "      Make sure USB debugging is ON and cable is connected." -ForegroundColor Red
} else {
    foreach ($line in $devices) {
        $serial = ($line -split '\s+')[0]
        Write-Host "      Device: $serial" -ForegroundColor Gray
        adb -s $serial reverse tcp:$ServerPort tcp:$ServerPort 2>&1 | Out-Null
        Write-Host "      Port forward $serial -> 127.0.0.1:$ServerPort ✓" -ForegroundColor Green
    }
}

# ── 4. Launch app on all connected devices ───────────────────
Write-Host "[4/4] Launching app..." -ForegroundColor Yellow
foreach ($line in $devices) {
    $serial = ($line -split '\s+')[0]
    adb -s $serial shell am force-stop com.example.restaurantapp 2>&1 | Out-Null
    Start-Sleep -Milliseconds 500
    adb -s $serial shell am start -n com.example.restaurantapp/.WelcomeActivity 2>&1 | Out-Null
    Write-Host "      App launched on $serial ✓" -ForegroundColor Green
}

Write-Host ""
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "  All done! App should be connecting." -ForegroundColor Green
Write-Host ""
Write-Host "  If you reconnect the USB cable, run:" -ForegroundColor Gray
Write-Host "  adb reverse tcp:8765 tcp:8765" -ForegroundColor White
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

