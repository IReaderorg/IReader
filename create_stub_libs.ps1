# PowerShell script to create stub/placeholder native libraries
# These stubs allow the application to compile and run in simulation mode
# Replace with real JNI libraries when available

$ErrorActionPreference = "Stop"

Write-Host "=== Creating Stub Native Libraries ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "These are placeholder files that allow the application to build." -ForegroundColor Yellow
Write-Host "The TTS service will fall back to simulation mode until real libraries are provided." -ForegroundColor Yellow
Write-Host ""

$BASE_DIR = "desktop/src/main/resources/native"

# Create stub content (minimal valid library format markers)
$stubContent = @(0x4D, 0x5A) # MZ header for Windows DLL
$stubContentUnix = @(0x7F, 0x45, 0x4C, 0x46) # ELF header for Linux
$stubContentMac = @(0xCF, 0xFA, 0xED, 0xFE) # Mach-O header for macOS

# Windows stub
Write-Host "Creating Windows stub..." -ForegroundColor Cyan
$winPath = "$BASE_DIR/windows-x64/piper_jni.dll"
[System.IO.File]::WriteAllBytes($winPath, $stubContent)
Write-Host "Created: $winPath" -ForegroundColor Green

# macOS x64 stub
Write-Host "Creating macOS x64 stub..." -ForegroundColor Cyan
$macX64Path = "$BASE_DIR/macos-x64/libpiper_jni.dylib"
[System.IO.File]::WriteAllBytes($macX64Path, $stubContentMac)
Write-Host "Created: $macX64Path" -ForegroundColor Green

# macOS ARM64 stub
Write-Host "Creating macOS ARM64 stub..." -ForegroundColor Cyan
$macArmPath = "$BASE_DIR/macos-arm64/libpiper_jni.dylib"
[System.IO.File]::WriteAllBytes($macArmPath, $stubContentMac)
Write-Host "Created: $macArmPath" -ForegroundColor Green

# Linux stub
Write-Host "Creating Linux x64 stub..." -ForegroundColor Cyan
$linuxPath = "$BASE_DIR/linux-x64/libpiper_jni.so"
[System.IO.File]::WriteAllBytes($linuxPath, $stubContentUnix)
Write-Host "Created: $linuxPath" -ForegroundColor Green

Write-Host ""
Write-Host "=== Stub Libraries Created ===" -ForegroundColor Green
Write-Host ""
Write-Host "IMPORTANT: These are placeholder files only!" -ForegroundColor Yellow
Write-Host "- The application will compile and run" -ForegroundColor White
Write-Host "- TTS will operate in simulation mode" -ForegroundColor White
Write-Host "- Replace with real JNI libraries for actual TTS functionality" -ForegroundColor White
Write-Host ""
Write-Host "See BUILD_NATIVE_LIBS.md for instructions on building real libraries" -ForegroundColor Cyan
Write-Host ""
