# Quick Start Scripts

This folder contains PowerShell scripts to quickly start the Distributed Food Ordering System.

## Scripts

### `quick-start.ps1` - Complete Setup (Recommended)

Starts everything: emulator, server, and app in one command.

```powershell
.\quick-start.ps1
```

**Options:**
- `-SkipEmulator` - Use if emulator is already running
- `-SkipBuild` - Skip building the app (use existing APK)

**Example:**
```powershell
# Full start (first time)
.\quick-start.ps1

# Restart with emulator already running
.\quick-start.ps1 -SkipEmulator

# Quick restart without rebuild
.\quick-start.ps1 -SkipEmulator -SkipBuild
```

### `start-emulator.ps1` - Emulator Only

Starts just the Android emulator with optimized settings to avoid memory crashes.

```powershell
.\start-emulator.ps1
```

**What it does:**
- Kills any existing emulator processes
- Starts emulator with 2GB RAM (prevents Out of Memory errors)
- Waits for full boot
- Sets up port forwarding (tcp:8765)

## Troubleshooting

### Emulator crashes with "Out of Memory"

The scripts already use reduced memory (2GB). If still crashing:
1. Close other applications
2. Check available RAM: `Get-WmiObject Win32_OperatingSystem | Select-Object FreePhysicalMemory`

### Server won't start

Check if port 8765 is already in use:
```powershell
netstat -an | Select-String "8765"
```

Kill existing process:
```powershell
$pid = Get-NetTCPConnection -LocalPort 8765 | Select-Object -ExpandProperty OwningProcess
Stop-Process -Id $pid -Force
```

### App won't install

Clear existing installation:
```powershell
adb uninstall com.example.restaurantapp
.\gradlew.bat clean assembleDebug installDebug
```

### Check server status

View server logs:
```powershell
Receive-Job -Name ServerJob -Keep
```

Stop server:
```powershell
Stop-Job -Name ServerJob
Remove-Job -Name ServerJob
```

### View app logs

```powershell
# Real-time logs
adb logcat -s RestaurantApp:*

# Filtered logs
adb logcat | Select-String "RestaurantApp"
```

## Manual Steps (Alternative)

If you prefer manual control:

1. **Start Emulator:**
   ```powershell
   & "C:\Users\anezi\AppData\Local\Android\Sdk\emulator\emulator.exe" -avd Medium_Phone_API_36.0 -memory 2048 -no-snapshot-load
   ```

2. **Set up port forwarding:**
   ```powershell
   adb reverse tcp:8765 tcp:8765
   ```

3. **Start server:**
   ```powershell
   .\gradlew.bat :server:run
   ```

4. **Build and install app:**
   ```powershell
   .\gradlew.bat assembleDebug installDebug
   ```

5. **Launch app:**
   ```powershell
   adb shell am start -n com.example.restaurantapp/.WelcomeActivity
   ```

## System Requirements

- Windows 10/11 with PowerShell 5.1+
- Android SDK installed
- At least 4GB free RAM
- Java 11+ for Gradle

## Notes

- The emulator uses the AVD: `Medium_Phone_API_36.0`
- Server runs on port `8765`
- App package name: `com.example.restaurantapp`
- Port forwarding allows the app to connect to `127.0.0.1:8765` (which forwards to your PC's server)

