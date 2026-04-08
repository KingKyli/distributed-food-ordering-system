# Start the backend server for the Distributed Food Ordering System

Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "  Starting Backend Server" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Magenta

# Check if server is already running
$serverRunning = netstat -an | Select-String "8765.*LISTENING"
if ($serverRunning) {
    Write-Host "Server is already running on port 8765!" -ForegroundColor Yellow
    Write-Host "To stop it, run: Stop-Process -Id (Get-NetTCPConnection -LocalPort 8765 | Select-Object -ExpandProperty OwningProcess -First 1) -Force" -ForegroundColor Cyan
    exit 0
}

Write-Host "Starting server on port 8765..." -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop the server`n" -ForegroundColor Yellow

# Start the server
.\gradlew.bat :server:run

