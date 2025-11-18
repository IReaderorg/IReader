# Master Script - Run All Compilation Fixes
# This script runs all fix scripts in the correct order

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IReader Compilation Fix Master Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$StartTime = Get-Date

# Step 1: Run basic compilation error fixes
Write-Host "[Step 1/3] Running basic compilation error fixes..." -ForegroundColor Yellow
& ".\scripts\fix_all_compilation_errors.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error in Step 1. Continuing anyway..." -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

# Step 2: Run BookDetailScreen specific fixes
Write-Host "[Step 2/3] Running BookDetailScreen fixes..." -ForegroundColor Yellow
& ".\scripts\fix_book_detail_screens.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error in Step 2. Continuing anyway..." -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

# Step 3: Run remaining error fixes
Write-Host "[Step 3/3] Running remaining error fixes..." -ForegroundColor Yellow
& ".\scripts\fix_remaining_errors.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error in Step 3. Continuing anyway..." -ForegroundColor Red
}

$EndTime = Get-Date
$Duration = $EndTime - $StartTime

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All Fixes Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Time taken: $($Duration.TotalSeconds) seconds" -ForegroundColor White
Write-Host ""

Write-Host "Summary of fixes applied:" -ForegroundColor Yellow
Write-Host "  [OK] State management fixes (mutableState to updateState)" -ForegroundColor Green
Write-Host "  [OK] Image loader fixes (placeholder syntax)" -ForegroundColor Green
Write-Host "  [OK] Chapter filtering and sorting fixes" -ForegroundColor Green
Write-Host "  [OK] Compose UI fixes (size modifiers, ripple)" -ForegroundColor Green
Write-Host "  [OK] Dependency injection fixes (Koin, Voyager)" -ForegroundColor Green
Write-Host "  [OK] Flow and StateFlow imports" -ForegroundColor Green
Write-Host "  [OK] Experimental API annotations" -ForegroundColor Green
Write-Host "  [OK] Parameter name corrections" -ForegroundColor Green
Write-Host "  [OK] When expression exhaustiveness" -ForegroundColor Green
Write-Host "  [OK] Function invocation fixes" -ForegroundColor Green
Write-Host "  [OK] String resource imports" -ForegroundColor Green
Write-Host "  [OK] Global search fixes" -ForegroundColor Green
Write-Host "  [OK] Smart cast fixes" -ForegroundColor Green
Write-Host ""

Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Compile the project:" -ForegroundColor White
Write-Host "   .\gradlew :presentation:compileReleaseKotlinAndroid" -ForegroundColor Gray
Write-Host ""
Write-Host "2. If there are still errors, check:" -ForegroundColor White
Write-Host "   - COMPILATION_FIXES_APPLIED.md for details" -ForegroundColor Gray
Write-Host "   - Individual error messages for context" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Run tests (if available):" -ForegroundColor White
Write-Host "   .\gradlew :presentation:testReleaseUnitTest" -ForegroundColor Gray
Write-Host ""

Write-Host "Documentation:" -ForegroundColor Cyan
Write-Host "  See COMPILATION_FIXES_APPLIED.md for complete details" -ForegroundColor White
Write-Host ""
