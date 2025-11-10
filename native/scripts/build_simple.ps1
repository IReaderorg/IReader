# Simple build script for piper_jni.dll
# Usage: .\build_simple.ps1

$ErrorActionPreference = "Stop"

Write-Host "=== Building piper_jni.dll ===" -ForegroundColor Cyan
Write-Host ""

# Check JAVA_HOME
if (-not $env:JAVA_HOME) {
    Write-Host "ERROR: JAVA_HOME is not set!" -ForegroundColor Red
    Write-Host "Please set JAVA_HOME to your JDK installation directory" -ForegroundColor Yellow
    Write-Host "Example: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'" -ForegroundColor Yellow
    exit 1
}

Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green

# Navigate to native directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$nativeDir = Split-Path -Parent $scriptDir
$projectRoot = Split-Path -Parent $nativeDir

Write-Host "Native directory: $nativeDir" -ForegroundColor Cyan
Write-Host ""

# Create build directory
$buildDir = Join-Path $nativeDir "build"
if (-not (Test-Path $buildDir)) {
    New-Item -ItemType Directory -Path $buildDir | Out-Null
}

Set-Location $buildDir

# Configure CMake
Write-Host "Configuring CMake..." -ForegroundColor Cyan
cmake .. -G "Visual Studio 17 2022" -A x64 -DCMAKE_BUILD_TYPE=Release

if ($LASTEXITCODE -ne 0) {
    Write-Host "CMake configuration failed!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Possible issues:" -ForegroundColor Yellow
    Write-Host "1. Visual Studio 2022 not installed" -ForegroundColor Yellow
    Write-Host "2. CMake not found in PATH" -ForegroundColor Yellow
    Write-Host "3. JAVA_HOME not set correctly" -ForegroundColor Yellow
    exit 1
}

Write-Host "Configuration complete!" -ForegroundColor Green
Write-Host ""

# Build
Write-Host "Building..." -ForegroundColor Cyan
cmake --build . --config Release --parallel

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Build complete!" -ForegroundColor Green
Write-Host ""

# Find the DLL
$dllPath = Join-Path $buildDir "Release\piper_jni.dll"

if (Test-Path $dllPath) {
    Write-Host "SUCCESS! DLL created:" -ForegroundColor Green
    Write-Host "  $dllPath" -ForegroundColor White
    
    $size = (Get-Item $dllPath).Length / 1KB
    Write-Host "  Size: $([math]::Round($size, 2)) KB" -ForegroundColor White
    Write-Host ""
    
    # Copy to resources directory
    $resourcesDir = Join-Path $projectRoot "domain\src\desktopMain\resources\native\windows"
    
    if (-not (Test-Path $resourcesDir)) {
        New-Item -ItemType Directory -Path $resourcesDir -Force | Out-Null
    }
    
    Copy-Item $dllPath $resourcesDir -Force
    Write-Host "DLL copied to resources:" -ForegroundColor Green
    Write-Host "  $resourcesDir\piper_jni.dll" -ForegroundColor White
    Write-Host ""
    
    # Verify dependencies are present
    Write-Host "Checking dependencies..." -ForegroundColor Cyan
    $deps = @("onnxruntime.dll", "espeak-ng.dll", "piper_phonemize.dll")
    $allPresent = $true
    
    foreach ($dep in $deps) {
        $depPath = Join-Path $resourcesDir $dep
        if (Test-Path $depPath) {
            Write-Host "  ✓ $dep" -ForegroundColor Green
        } else {
            Write-Host "  ✗ $dep (MISSING!)" -ForegroundColor Red
            $allPresent = $false
        }
    }
    
    Write-Host ""
    
    if ($allPresent) {
        Write-Host "=== BUILD SUCCESSFUL ===" -ForegroundColor Green
        Write-Host "All files are ready in: $resourcesDir" -ForegroundColor White
        Write-Host ""
        Write-Host "Next steps:" -ForegroundColor Cyan
        Write-Host "1. Run: .\gradlew desktop:run" -ForegroundColor White
        Write-Host "2. Test TTS functionality" -ForegroundColor White
    } else {
        Write-Host "WARNING: Some dependencies are missing!" -ForegroundColor Yellow
        Write-Host "Make sure all DLL files are in: $resourcesDir" -ForegroundColor Yellow
    }
} else {
    Write-Host "ERROR: DLL not found at: $dllPath" -ForegroundColor Red
    Write-Host "Check the build output above for errors" -ForegroundColor Yellow
    exit 1
}
