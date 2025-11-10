# verify_build.ps1 - Verify built libraries
# This script checks that all required libraries are present and valid

param(
    [switch]$Help
)

if ($Help) {
    Write-Host "Usage: .\verify_build.ps1"
    Write-Host ""
    Write-Host "This script verifies that all platform libraries are built and valid."
    exit 0
}

# Colors
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warn { Write-Host $args -ForegroundColor Yellow }
function Write-Err { Write-Host $args -ForegroundColor Red }

Write-Info "=== Piper JNI Library Verification ==="

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$nativeDir = Split-Path -Parent $scriptDir
$resourcesDir = Join-Path (Split-Path -Parent $nativeDir) "domain\src\desktopMain\resources\native"

# Define expected libraries
$expectedLibraries = @{
    "windows" = @{
        "path" = "windows\piper_jni.dll"
        "minSize" = 50KB
    }
    "macos-x64" = @{
        "path" = "macos-x64\libpiper_jni.dylib"
        "minSize" = 50KB
    }
    "macos-arm64" = @{
        "path" = "macos-arm64\libpiper_jni.dylib"
        "minSize" = 50KB
    }
    "linux" = @{
        "path" = "linux\libpiper_jni.so"
        "minSize" = 50KB
    }
}

$results = @{}

Write-Info "`nChecking libraries in: $resourcesDir"

foreach ($platform in $expectedLibraries.Keys) {
    $libInfo = $expectedLibraries[$platform]
    $libPath = Join-Path $resourcesDir $libInfo.path
    
    Write-Info "`nPlatform: $platform"
    Write-Info "  Expected: $libPath"
    
    if (Test-Path $libPath) {
        $file = Get-Item $libPath
        $size = $file.Length
        
        if ($size -ge $libInfo.minSize) {
            $sizeKB = [math]::Round($size/1KB, 2)
            Write-Success "  Found: $sizeKB KB"
            Write-Info "  Last modified: $($file.LastWriteTime)"
            $results[$platform] = "PRESENT"
        }
        else {
            $sizeKB = [math]::Round($size/1KB, 2)
            $minSizeKB = [math]::Round($libInfo.minSize/1KB, 2)
            Write-Warn "  Found but too small: $sizeKB KB (minimum: $minSizeKB KB)"
            $results[$platform] = "INVALID"
        }
    }
    else {
        Write-Err "  Not found"
        $results[$platform] = "MISSING"
    }
}

# Summary
Write-Info "`n=== Verification Summary ==="
$present = ($results.Values | Where-Object { $_ -eq "PRESENT" }).Count
$missing = ($results.Values | Where-Object { $_ -eq "MISSING" }).Count
$invalid = ($results.Values | Where-Object { $_ -eq "INVALID" }).Count
$total = $results.Count

Write-Info "Total platforms: $total"
Write-Success "Present: $present"
if ($missing -gt 0) {
    Write-Err "Missing: $missing"
}
if ($invalid -gt 0) {
    Write-Warn "Invalid: $invalid"
}

# Build instructions for missing platforms
if ($missing -gt 0 -or $invalid -gt 0) {
    Write-Info "`n=== Build Instructions ==="
    
    foreach ($platform in $results.Keys) {
        if ($results[$platform] -ne "PRESENT") {
            Write-Info "`nTo build $platform :"
            
            switch -Wildcard ($platform) {
                "windows" {
                    Write-Info "  .\native\scripts\build_windows.ps1 -Config Release"
                }
                "macos-x64" {
                    Write-Info "  On macOS (Intel):"
                    Write-Info "  ./native/scripts/build_macos.sh release"
                }
                "macos-arm64" {
                    Write-Info "  On macOS (Apple Silicon):"
                    Write-Info "  ./native/scripts/build_macos.sh release"
                    Write-Info "  Or build universal binary:"
                    Write-Info "  ./native/scripts/build_macos.sh release universal"
                }
                "linux" {
                    Write-Info "  On Linux:"
                    Write-Info "  ./native/scripts/build_linux.sh release"
                }
            }
        }
    }
    
    Write-Info "`nFor CI/CD cross-platform builds, see .github/workflows/build-native-libs.yml"
}

if ($present -eq $total) {
    Write-Success "`nAll libraries present and valid!"
    exit 0
}
else {
    Write-Warn "`nSome libraries are missing or invalid"
    exit 1
}
