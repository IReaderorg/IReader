# Build Windows MSI Installer for IReader
# Requires WiX Toolset 3.11 or later

param(
    [string]$SourceDir = "..\..\build\release",
    [string]$OutputDir = "..\..\build\installers",
    [string]$Version = "1.0.0"
)

$ErrorActionPreference = "Stop"

Write-Host "Building IReader Windows MSI Installer..." -ForegroundColor Cyan

# Check for WiX Toolset
$wixPath = "${env:WIX}bin"
if (-not (Test-Path $wixPath)) {
    Write-Error "WiX Toolset not found. Please install from https://wixtoolset.org/"
    exit 1
}

$candle = Join-Path $wixPath "candle.exe"
$light = Join-Path $wixPath "light.exe"

# Verify source files exist
if (-not (Test-Path $SourceDir)) {
    Write-Error "Source directory not found: $SourceDir"
    exit 1
}

# Create output directory
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

# Compile WiX source
Write-Host "Compiling WiX source..." -ForegroundColor Yellow
& $candle `
    -dSourceDir=$SourceDir `
    -dVersion=$Version `
    -out "$OutputDir\ireader.wixobj" `
    -arch x64 `
    -ext WixUIExtension `
    ireader.wxs

if ($LASTEXITCODE -ne 0) {
    Write-Error "WiX compilation failed"
    exit 1
}

# Link to create MSI
Write-Host "Linking MSI package..." -ForegroundColor Yellow
& $light `
    -out "$OutputDir\IReader-$Version-x64.msi" `
    -ext WixUIExtension `
    -cultures:en-us `
    "$OutputDir\ireader.wixobj"

if ($LASTEXITCODE -ne 0) {
    Write-Error "WiX linking failed"
    exit 1
}

# Clean up intermediate files
Remove-Item "$OutputDir\ireader.wixobj" -ErrorAction SilentlyContinue
Remove-Item "$OutputDir\*.wixpdb" -ErrorAction SilentlyContinue

Write-Host "MSI installer created successfully: $OutputDir\IReader-$Version-x64.msi" -ForegroundColor Green

# Calculate checksum
$msiPath = "$OutputDir\IReader-$Version-x64.msi"
$hash = Get-FileHash -Path $msiPath -Algorithm SHA256
Write-Host "SHA256: $($hash.Hash)" -ForegroundColor Cyan

# Save checksum to file
$hash.Hash | Out-File -FilePath "$OutputDir\IReader-$Version-x64.msi.sha256" -NoNewline

Write-Host "Build complete!" -ForegroundColor Green
