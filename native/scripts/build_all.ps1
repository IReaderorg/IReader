# build_all.ps1 - Master build script for all platforms
# This script is intended to run in CI/CD environments with cross-compilation support
# For local builds, use platform-specific scripts

param(
    [string]$Platform = "current",  # current, windows, macos, linux, all
    [string]$Config = "Release",
    [switch]$Clean,
    [switch]$Test,
    [switch]$Package,
    [switch]$Help
)

if ($Help) {
    Write-Host "Usage: .\build_all.ps1 [-Platform current|windows|macos|linux|all] [-Config Release|Debug] [-Clean] [-Test] [-Package]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Platform  Target platform (current, windows, macos, linux, all)"
    Write-Host "  -Config    Build configuration (Release or Debug, default: Release)"
    Write-Host "  -Clean     Clean build directories before building"
    Write-Host "  -Test      Run tests after building"
    Write-Host "  -Package   Create distribution package"
    Write-Host "  -Help      Show this help message"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\build_all.ps1                    # Build for current platform"
    Write-Host "  .\build_all.ps1 -Platform all      # Build for all platforms (CI/CD)"
    Write-Host "  .\build_all.ps1 -Clean -Test       # Clean build and test"
    exit 0
}

$ErrorActionPreference = "Stop"

# Colors
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warn { Write-Host $args -ForegroundColor Yellow }
function Write-Err { Write-Host $args -ForegroundColor Red }

Write-Info "=== Piper JNI Master Build Script ==="
Write-Info "Platform: $Platform"
Write-Info "Configuration: $Config"

# Detect current platform
$CurrentPlatform = "unknown"
if ($IsWindows -or $env:OS -eq "Windows_NT") {
    $CurrentPlatform = "windows"
}
elseif ($IsMacOS) {
    $CurrentPlatform = "macos"
}
elseif ($IsLinux) {
    $CurrentPlatform = "linux"
}

Write-Info "Current platform: $CurrentPlatform"

# Determine which platforms to build
$PlatformsToBuild = @()
if ($Platform -eq "current") {
    $PlatformsToBuild = @($CurrentPlatform)
}
elseif ($Platform -eq "all") {
    $PlatformsToBuild = @("windows", "macos", "linux")
}
else {
    $PlatformsToBuild = @($Platform)
}

Write-Info "Platforms to build: $($PlatformsToBuild -join ', ')"

# Navigate to script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$nativeDir = Split-Path -Parent $scriptDir
Set-Location $nativeDir

# Build results
$BuildResults = @{}

# Build each platform
foreach ($plat in $PlatformsToBuild) {
    Write-Info "`n=== Building for $plat ==="
    
    $buildScript = Join-Path $scriptDir "build_$plat.ps1"
    if ($plat -eq "macos" -or $plat -eq "linux") {
        $buildScript = Join-Path $scriptDir "build_$plat.sh"
    }
    
    if (-not (Test-Path $buildScript)) {
        Write-Warn "Build script not found: $buildScript"
        $BuildResults[$plat] = "SKIPPED"
        continue
    }
    
    # Check if we can build this platform
    if ($plat -ne $CurrentPlatform -and $Platform -ne "all") {
        Write-Warn "Cannot build $plat on $CurrentPlatform without cross-compilation"
        Write-Info "Use Docker or CI/CD for cross-platform builds"
        $BuildResults[$plat] = "SKIPPED"
        continue
    }
    
    try {
        if ($plat -eq "windows") {
            $args = @("-Config", $Config)
            if ($Clean) { $args += "-Clean" }
            if ($Test) { $args += "-Test" }
            
            & $buildScript @args
            
            if ($LASTEXITCODE -eq 0) {
                $BuildResults[$plat] = "SUCCESS"
            }
            else {
                $BuildResults[$plat] = "FAILED"
            }
        }
        else {
            # macOS/Linux
            $args = @($Config.ToLower())
            if ($Clean) { $args += "clean" }
            if ($Test) { $args += "test" }
            
            if ($CurrentPlatform -eq $plat) {
                bash $buildScript @args
                
                if ($LASTEXITCODE -eq 0) {
                    $BuildResults[$plat] = "SUCCESS"
                }
                else {
                    $BuildResults[$plat] = "FAILED"
                }
            }
            else {
                Write-Warn "Cross-compilation not supported in this script"
                $BuildResults[$plat] = "SKIPPED"
            }
        }
    }
    catch {
        Write-Err "Build failed for $plat : $_"
        $BuildResults[$plat] = "FAILED"
    }
}

# Package if requested
if ($Package) {
    Write-Info "`n=== Creating Distribution Package ==="
    
    $packageDir = Join-Path $nativeDir "build\package"
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $packageName = "piper-jni-$timestamp"
    $packagePath = Join-Path $packageDir $packageName
    
    if (Test-Path $packagePath) {
        Remove-Item -Recurse -Force $packagePath
    }
    New-Item -ItemType Directory -Path $packagePath -Force | Out-Null
    
    # Copy libraries
    foreach ($plat in $PlatformsToBuild) {
        if ($BuildResults[$plat] -eq "SUCCESS") {
            $libDir = Join-Path $nativeDir "build\$plat"
            $destDir = Join-Path $packagePath $plat
            
            if (Test-Path $libDir) {
                New-Item -ItemType Directory -Path $destDir -Force | Out-Null
                
                # Copy library files
                if ($plat -eq "windows") {
                    Copy-Item "$libDir\Release\piper_jni.dll" $destDir -ErrorAction SilentlyContinue
                }
                elseif ($plat -eq "macos") {
                    Copy-Item "$libDir\libpiper_jni.dylib" $destDir -ErrorAction SilentlyContinue
                }
                elseif ($plat -eq "linux") {
                    Copy-Item "$libDir\libpiper_jni.so" $destDir -ErrorAction SilentlyContinue
                }
            }
        }
    }
    
    # Create archive
    $archivePath = "$packagePath.zip"
    Compress-Archive -Path $packagePath -DestinationPath $archivePath -Force
    
    Write-Success "Package created: $archivePath"
}

# Print summary
Write-Info "`n=== Build Summary ==="
foreach ($plat in $PlatformsToBuild) {
    $status = $BuildResults[$plat]
    $color = switch ($status) {
        "SUCCESS" { "Green" }
        "FAILED" { "Red" }
        "SKIPPED" { "Yellow" }
        default { "White" }
    }
    Write-Host "  $plat : $status" -ForegroundColor $color
}

# Exit with error if any builds failed
$failedBuilds = $BuildResults.Values | Where-Object { $_ -eq "FAILED" }
if ($failedBuilds.Count -gt 0) {
    Write-Err "`nSome builds failed!"
    exit 1
}

Write-Success "`n=== All Builds Complete ==="
