# build_windows.ps1 - Build script for Windows
# Usage: .\build_windows.ps1 [-Config Release|Debug] [-Clean] [-Test]

param(
    [string]$Config = "Release",
    [switch]$Clean,
    [switch]$Test,
    [switch]$Help
)

if ($Help) {
    Write-Host "Usage: .\build_windows.ps1 [-Config Release|Debug] [-Clean] [-Test]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Config    Build configuration (Release or Debug, default: Release)"
    Write-Host "  -Clean     Clean build directory before building"
    Write-Host "  -Test      Run tests after building"
    Write-Host "  -Help      Show this help message"
    exit 0
}

$ErrorActionPreference = "Stop"

# Colors for output
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warn { Write-Host $args -ForegroundColor Yellow }
function Write-Err { Write-Host $args -ForegroundColor Red }

Write-Info "=== Piper JNI Build Script for Windows ==="
Write-Info "Configuration: $Config"

# Check prerequisites
Write-Info "`nChecking prerequisites..."

# Check CMake
try {
    $cmakeVersion = cmake --version | Select-Object -First 1
    Write-Success "✓ CMake found: $cmakeVersion"
}
catch {
    Write-Err "✗ CMake not found. Please install CMake 3.15 or later."
    Write-Info "Download from: https://cmake.org/download/"
    exit 1
}

# Check Visual Studio
$vsWhere = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\vswhere.exe"
if (Test-Path $vsWhere) {
    $vsPath = & $vsWhere -latest -property installationPath
    if ($vsPath) {
        Write-Success "✓ Visual Studio found: $vsPath"
    }
    else {
        Write-Warn "⚠ Visual Studio not found. Build may fail."
    }
}
else {
    Write-Warn "⚠ vswhere.exe not found. Cannot detect Visual Studio."
}

# Check Java/JDK
if ($env:JAVA_HOME) {
    Write-Success "✓ JAVA_HOME set: $env:JAVA_HOME"
}
else {
    Write-Warn "⚠ JAVA_HOME not set. Attempting to find Java..."
    $javaPath = (Get-Command java -ErrorAction SilentlyContinue).Source
    if ($javaPath) {
        $javaHome = Split-Path (Split-Path $javaPath)
        $env:JAVA_HOME = $javaHome
        Write-Info "  Found Java at: $javaHome"
    }
    else {
        Write-Err "✗ Java not found. Please install JDK 11 or later and set JAVA_HOME."
        exit 1
    }
}

# Navigate to native directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$nativeDir = Split-Path -Parent $scriptDir
Set-Location $nativeDir

Write-Info "`nNative directory: $nativeDir"

# Build directory
$buildDir = Join-Path $nativeDir "build\windows"

# Clean if requested
if ($Clean -and (Test-Path $buildDir)) {
    Write-Info "`nCleaning build directory..."
    Remove-Item -Recurse -Force $buildDir
    Write-Success "✓ Build directory cleaned"
}

# Create build directory
if (-not (Test-Path $buildDir)) {
    New-Item -ItemType Directory -Path $buildDir | Out-Null
}

# Configure
Write-Info "`nConfiguring CMake..."
Set-Location $buildDir

try {
    cmake ../.. `
        -G "Visual Studio 17 2022" `
        -A x64 `
        -DCMAKE_BUILD_TYPE=$Config `
        -DCMAKE_INSTALL_PREFIX="$buildDir/install"
    
    if ($LASTEXITCODE -ne 0) {
        throw "CMake configuration failed"
    }
    Write-Success "✓ CMake configuration complete"
}
catch {
    Write-Err "✗ CMake configuration failed: $_"
    exit 1
}

# Build
Write-Info "`nBuilding..."
try {
    cmake --build . --config $Config --parallel
    
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed"
    }
    Write-Success "✓ Build complete"
}
catch {
    Write-Err "✗ Build failed: $_"
    exit 1
}

# Find the built library
$libPath = Join-Path $buildDir "$Config\piper_jni.dll"
if (Test-Path $libPath) {
    Write-Success "`n✓ Library built successfully: $libPath"
    $libSize = (Get-Item $libPath).Length / 1KB
    Write-Info "  Size: $([math]::Round($libSize, 2)) KB"
}
else {
    Write-Warn "⚠ Library not found at expected location: $libPath"
}

# Run tests if requested
if ($Test) {
    Write-Info "`nRunning tests..."
    try {
        ctest --build-config $Config --output-on-failure
        if ($LASTEXITCODE -eq 0) {
            Write-Success "✓ All tests passed"
        }
        else {
            Write-Warn "⚠ Some tests failed"
        }
    }
    catch {
        Write-Warn "⚠ Test execution failed: $_"
    }
}

Write-Success "`n=== Build Complete ==="
Write-Info "Build artifacts located in: $buildDir"
Write-Info "Library copied to: domain/src/desktopMain/resources/native/windows/"
