# Download Piper TTS executable for Windows
$ErrorActionPreference = "Stop"

Write-Host "Downloading Piper TTS for Windows..." -ForegroundColor Cyan

$piperVersion = "2023.11.14-2"
$downloadUrl = "https://github.com/rhasspy/piper/releases/download/$piperVersion/piper_windows_amd64.zip"
$tempZip = "$env:TEMP\piper_windows.zip"
$extractPath = "$env:TEMP\piper_extract"
$destPath = "..\domain\src\desktopMain\resources\native\windows-x64"

# Download
Write-Host "Downloading from: $downloadUrl"
Invoke-WebRequest -Uri $downloadUrl -OutFile $tempZip

# Extract
Write-Host "Extracting..."
Expand-Archive -Path $tempZip -DestinationPath $extractPath -Force

# Copy piper.exe
Write-Host "Installing piper.exe..."
Copy-Item "$extractPath\piper\piper.exe" "$destPath\piper.exe" -Force

# Copy espeak-ng-data if needed
if (Test-Path "$extractPath\piper\espeak-ng-data") {
    Write-Host "Installing espeak-ng-data..."
    Copy-Item "$extractPath\piper\espeak-ng-data" "$destPath\espeak-ng-data" -Recurse -Force
}

# Cleanup
Remove-Item $tempZip -Force
Remove-Item $extractPath -Recurse -Force

Write-Host "Piper installed successfully!" -ForegroundColor Green
Write-Host "Location: $destPath\piper.exe"
