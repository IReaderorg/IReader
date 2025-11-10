# Minimal build script for piper_jni.dll
$ErrorActionPreference = "Stop"

Write-Host "Building piper_jni.dll..." -ForegroundColor Cyan

# Check JAVA_HOME
if (-not $env:JAVA_HOME) {
    Write-Host "ERROR: JAVA_HOME not set!" -ForegroundColor Red
    exit 1
}

# Go to native directory
cd native

# Create build directory
if (-not (Test-Path "build")) {
    mkdir build
}

cd build

# Configure
Write-Host "Configuring..." -ForegroundColor Yellow
cmake .. -G "Visual Studio 17 2022" -A x64

# Build
Write-Host "Building..." -ForegroundColor Yellow
cmake --build . --config Release

# Check result
if (Test-Path "Release\piper_jni.dll") {
    Write-Host "SUCCESS! DLL created" -ForegroundColor Green
    
    # Copy to resources
    $dest = "..\..\domain\src\desktopMain\resources\native\windows-x64"
    if (-not (Test-Path $dest)) {
        mkdir $dest -Force
    }
    copy "Release\piper_jni.dll" $dest
    
    Write-Host "DLL copied to: $dest" -ForegroundColor Green
} else {
    Write-Host "ERROR: Build failed" -ForegroundColor Red
}
