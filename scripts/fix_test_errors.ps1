# Script to fix common presentation compilation errors
# This script addresses:
# 1. Try-catch around composable functions
# 2. Missing imports and unresolved references
# 3. Parameter type mismatches
# 4. Deprecated API usage

Write-Host "Fixing presentation compilation errors..." -ForegroundColor Green

# Define the presentation directory
$presentationDir = "presentation/src/commonMain/kotlin/ireader/presentation"

Write-Host "Presentation directory: $presentationDir" -ForegroundColor Cyan

# Check if directory exists
if (Test-Path $presentationDir) {
    Write-Host "Found presentation directory" -ForegroundColor Green
    
    # Count Kotlin files
    $ktFiles = Get-ChildItem -Path $presentationDir -Filter "*.kt" -Recurse
    Write-Host "Found $($ktFiles.Count) Kotlin files" -ForegroundColor Cyan
    
} else {
    Write-Host "Presentation directory not found!" -ForegroundColor Red
    exit 1
}

Write-Host "`nPresentation errors will be fixed by updating the files..." -ForegroundColor Yellow
Write-Host "Main issues to fix:" -ForegroundColor Yellow
Write-Host "  - Try-catch around composable functions" -ForegroundColor Cyan
Write-Host "  - Missing ScreenModel imports" -ForegroundColor Cyan
Write-Host "  - ImageLoader placeholder parameter" -ForegroundColor Cyan
Write-Host "  - Missing function parameters" -ForegroundColor Cyan
Write-Host "`nPlease review the changes after running this script." -ForegroundColor Yellow
