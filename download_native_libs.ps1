# PowerShell script to download Piper TTS and ONNX Runtime native libraries
# This script downloads pre-built binaries for Windows, macOS, and Linux

$ErrorActionPreference = "Stop"

# Define versions
$ONNX_VERSION = "1.16.3"
$PIPER_VERSION = "2023.11.14-2"

# Define base directory
$BASE_DIR = "desktop/src/main/resources/native"

Write-Host "=== Downloading Native Libraries for Piper TTS ===" -ForegroundColor Cyan
Write-Host ""

# Create directories if they don't exist
$platforms = @("windows-x64", "macos-x64", "macos-arm64", "linux-x64")
foreach ($platform in $platforms) {
    $dir = Join-Path $BASE_DIR $platform
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "Created directory: $dir" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "=== Downloading ONNX Runtime Libraries ===" -ForegroundColor Yellow
Write-Host ""

# Download ONNX Runtime for Windows
Write-Host "Downloading ONNX Runtime for Windows x64..." -ForegroundColor Cyan
$onnxWinUrl = "https://github.com/microsoft/onnxruntime/releases/download/v$ONNX_VERSION/onnxruntime-win-x64-$ONNX_VERSION.zip"
$onnxWinZip = "onnxruntime-win-x64.zip"
Invoke-WebRequest -Uri $onnxWinUrl -OutFile $onnxWinZip -UseBasicParsing
Expand-Archive -Path $onnxWinZip -DestinationPath "temp_onnx_win" -Force
Copy-Item "temp_onnx_win/onnxruntime-win-x64-$ONNX_VERSION/lib/onnxruntime.dll" "$BASE_DIR/windows-x64/onnxruntime.dll" -Force
Remove-Item $onnxWinZip -Force
Remove-Item "temp_onnx_win" -Recurse -Force
Write-Host "Windows ONNX Runtime downloaded" -ForegroundColor Green

# Download ONNX Runtime for macOS x64
Write-Host "Downloading ONNX Runtime for macOS x64..." -ForegroundColor Cyan
$onnxMacUrl = "https://github.com/microsoft/onnxruntime/releases/download/v$ONNX_VERSION/onnxruntime-osx-x86_64-$ONNX_VERSION.tgz"
$onnxMacTgz = "onnxruntime-osx-x64.tgz"
Invoke-WebRequest -Uri $onnxMacUrl -OutFile $onnxMacTgz -UseBasicParsing
tar -xzf $onnxMacTgz
Copy-Item "onnxruntime-osx-x86_64-$ONNX_VERSION/lib/libonnxruntime.$ONNX_VERSION.dylib" "$BASE_DIR/macos-x64/libonnxruntime.dylib" -Force
Remove-Item $onnxMacTgz -Force
Remove-Item "onnxruntime-osx-x86_64-$ONNX_VERSION" -Recurse -Force
Write-Host "macOS x64 ONNX Runtime downloaded" -ForegroundColor Green

# Download ONNX Runtime for macOS ARM64
Write-Host "Downloading ONNX Runtime for macOS ARM64..." -ForegroundColor Cyan
$onnxMacArmUrl = "https://github.com/microsoft/onnxruntime/releases/download/v$ONNX_VERSION/onnxruntime-osx-arm64-$ONNX_VERSION.tgz"
$onnxMacArmTgz = "onnxruntime-osx-arm64.tgz"
Invoke-WebRequest -Uri $onnxMacArmUrl -OutFile $onnxMacArmTgz -UseBasicParsing
tar -xzf $onnxMacArmTgz
Copy-Item "onnxruntime-osx-arm64-$ONNX_VERSION/lib/libonnxruntime.$ONNX_VERSION.dylib" "$BASE_DIR/macos-arm64/libonnxruntime.dylib" -Force
Remove-Item $onnxMacArmTgz -Force
Remove-Item "onnxruntime-osx-arm64-$ONNX_VERSION" -Recurse -Force
Write-Host "macOS ARM64 ONNX Runtime downloaded" -ForegroundColor Green

# Download ONNX Runtime for Linux
Write-Host "Downloading ONNX Runtime for Linux x64..." -ForegroundColor Cyan
$onnxLinuxUrl = "https://github.com/microsoft/onnxruntime/releases/download/v$ONNX_VERSION/onnxruntime-linux-x64-$ONNX_VERSION.tgz"
$onnxLinuxTgz = "onnxruntime-linux-x64.tgz"
Invoke-WebRequest -Uri $onnxLinuxUrl -OutFile $onnxLinuxTgz -UseBasicParsing
tar -xzf $onnxLinuxTgz
Copy-Item "onnxruntime-linux-x64-$ONNX_VERSION/lib/libonnxruntime.so.$ONNX_VERSION" "$BASE_DIR/linux-x64/libonnxruntime.so" -Force
Remove-Item $onnxLinuxTgz -Force
Remove-Item "onnxruntime-linux-x64-$ONNX_VERSION" -Recurse -Force
Write-Host "Linux ONNX Runtime downloaded" -ForegroundColor Green

Write-Host ""
Write-Host "=== Downloading Piper TTS Libraries ===" -ForegroundColor Yellow
Write-Host ""
Write-Host "NOTE: Piper JNI libraries need to be built from source." -ForegroundColor Yellow
Write-Host "The piper_jni libraries are not available as pre-built binaries." -ForegroundColor Yellow
Write-Host ""
Write-Host "You have two options:" -ForegroundColor Cyan
Write-Host "1. Build from source: https://github.com/rhasspy/piper" -ForegroundColor White
Write-Host "2. Use pre-built Piper binaries and create JNI wrapper" -ForegroundColor White
Write-Host ""

# Download Piper pre-built binaries for reference
Write-Host "Downloading Piper pre-built binaries for reference..." -ForegroundColor Cyan

# Windows
$piperWinUrl = "https://github.com/rhasspy/piper/releases/download/$PIPER_VERSION/piper_windows_amd64.zip"
$piperWinZip = "piper_windows.zip"
Invoke-WebRequest -Uri $piperWinUrl -OutFile $piperWinZip -UseBasicParsing
Write-Host "Piper Windows binary downloaded (reference)" -ForegroundColor Green

# macOS
$piperMacUrl = "https://github.com/rhasspy/piper/releases/download/$PIPER_VERSION/piper_macos_x64.tar.gz"
$piperMacTgz = "piper_macos.tar.gz"
Invoke-WebRequest -Uri $piperMacUrl -OutFile $piperMacTgz -UseBasicParsing
Write-Host "Piper macOS binary downloaded (reference)" -ForegroundColor Green

# Linux
$piperLinuxUrl = "https://github.com/rhasspy/piper/releases/download/$PIPER_VERSION/piper_linux_x86_64.tar.gz"
$piperLinuxTgz = "piper_linux.tar.gz"
Invoke-WebRequest -Uri $piperLinuxUrl -OutFile $piperLinuxTgz -UseBasicParsing
Write-Host "Piper Linux binary downloaded (reference)" -ForegroundColor Green

Write-Host ""
Write-Host "=== Download Summary ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "ONNX Runtime libraries downloaded for all platforms" -ForegroundColor Green
Write-Host "Piper reference binaries downloaded" -ForegroundColor Green
Write-Host ""
Write-Host "IMPORTANT: You still need to build or obtain piper_jni libraries:" -ForegroundColor Yellow
Write-Host "  - piper_jni.dll (Windows)" -ForegroundColor White
Write-Host "  - libpiper_jni.dylib (macOS)" -ForegroundColor White
Write-Host "  - libpiper_jni.so (Linux)" -ForegroundColor White
Write-Host ""
Write-Host "Libraries are located in: $BASE_DIR" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Build piper_jni from source or obtain pre-built binaries" -ForegroundColor White
Write-Host "2. Place piper_jni libraries in the appropriate platform directories" -ForegroundColor White
Write-Host "3. Run gradlew :desktop:verifyNativeLibraries to verify" -ForegroundColor White
Write-Host ""
