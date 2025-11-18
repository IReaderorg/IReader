# Master Script to Fix All Compilation Errors
# This script runs all fix scripts in the correct order

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IReader Compilation Error Fixer" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Fix critical errors first
Write-Host "Step 1: Fixing critical errors..." -ForegroundColor Green
& "$PSScriptRoot\fix_critical_errors.ps1"
Write-Host ""

# Step 2: Fix ViewModel stateIn issues
Write-Host "Step 2: Fixing ViewModel stateIn issues..." -ForegroundColor Green
& "$PSScriptRoot\fix_viewmodel_statein.ps1"
Write-Host ""

# Step 3: Fix UI and string resources
Write-Host "Step 3: Fixing UI and string resources..." -ForegroundColor Green
& "$PSScriptRoot\fix_ui_and_strings.ps1"
Write-Host ""

# Step 4: Fix migration errors
Write-Host "Step 4: Fixing migration errors..." -ForegroundColor Green
& "$PSScriptRoot\fix_migration_errors.ps1"
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All fixes applied!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Run: .\gradlew :presentation:compileKotlinDesktop" -ForegroundColor White
Write-Host "2. Check for any remaining errors" -ForegroundColor White
Write-Host "3. Run full build: .\gradlew build" -ForegroundColor White
Write-Host ""
