# generate_checksums.ps1 - Generate SHA256 checksums for built libraries

param(
    [switch]$Help
)

if ($Help) {
    Write-Host "Usage: .\generate_checksums.ps1"
    Write-Host ""
    Write-Host "This script generates SHA256 checksums for all built libraries."
    exit 0
}

# Colors
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warn { Write-Host $args -ForegroundColor Yellow }
function Write-Err { Write-Host $args -ForegroundColor Red }

Write-Info "=== Generating Library Checksums ==="

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$nativeDir = Split-Path -Parent $scriptDir
$resourcesDir = Join-Path (Split-Path -Parent $nativeDir) "domain\src\desktopMain\resources\native"

# Define libraries
$libraries = @(
    @{ Platform = "windows"; Path = "windows\piper_jni.dll" }
    @{ Platform = "macos-x64"; Path = "macos-x64\libpiper_jni.dylib" }
    @{ Platform = "macos-arm64"; Path = "macos-arm64\libpiper_jni.dylib" }
    @{ Platform = "linux"; Path = "linux\libpiper_jni.so" }
)

$checksums = @()
$foundCount = 0

foreach ($lib in $libraries) {
    $libPath = Join-Path $resourcesDir $lib.Path
    
    Write-Info "`nPlatform: $($lib.Platform)"
    Write-Info "  Path: $libPath"
    
    if (Test-Path $libPath) {
        try {
            $hash = Get-FileHash -Path $libPath -Algorithm SHA256
            $checksums += [PSCustomObject]@{
                Platform = $lib.Platform
                File = Split-Path -Leaf $libPath
                SHA256 = $hash.Hash
                Size = (Get-Item $libPath).Length
            }
            Write-Success "  Checksum: $($hash.Hash)"
            $foundCount++
        }
        catch {
            Write-Err "  Error generating checksum: $_"
        }
    }
    else {
        Write-Warn "  Not found - skipping"
    }
}

# Save checksums to file
if ($checksums.Count -gt 0) {
    $checksumFile = Join-Path $resourcesDir "checksums.txt"
    
    Write-Info "`nSaving checksums to: $checksumFile"
    
    $output = @()
    $output += "# Piper JNI Library Checksums"
    $output += "# Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    $output += ""
    
    foreach ($checksum in $checksums) {
        $sizeKB = [math]::Round($checksum.Size / 1KB, 2)
        $output += "# Platform: $($checksum.Platform) | Size: $sizeKB KB"
        $output += "$($checksum.SHA256)  $($checksum.File)"
        $output += ""
    }
    
    $output | Out-File -FilePath $checksumFile -Encoding UTF8
    Write-Success "Checksums saved successfully"
    
    # Also create JSON format
    $jsonFile = Join-Path $resourcesDir "checksums.json"
    $checksums | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonFile -Encoding UTF8
    Write-Success "JSON checksums saved to: $jsonFile"
}

# Summary
Write-Info "`n=== Summary ==="
Write-Info "Libraries processed: $foundCount / $($libraries.Count)"

if ($foundCount -eq $libraries.Count) {
    Write-Success "All libraries checksummed!"
    exit 0
}
else {
    Write-Warn "Some libraries are missing"
    exit 1
}
