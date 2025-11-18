# Master Script to Fix All Remaining Compilation Errors
# Run this after the initial fixes have been applied

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Fixing Remaining Compilation Errors" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Fix Preference.stateIn() calls
Write-Host "Step 1: Fixing Preference.stateIn() calls..." -ForegroundColor Green
& "$PSScriptRoot\fix_preference_statein.ps1"
Write-Host ""

# Step 2: Fix string resource imports
Write-Host "Step 2: Fixing string resource imports..." -ForegroundColor Green
& "$PSScriptRoot\fix_string_resources.ps1"
Write-Host ""

# Step 3: Fix AccessibilityUtils ripple and size issues
Write-Host "Step 3: Fixing AccessibilityUtils..." -ForegroundColor Green
& "$PSScriptRoot\fix_accessibility_ripple.ps1"
Write-Host ""

# Step 4: Fix migration models
Write-Host "Step 4: Fixing Migration models..." -ForegroundColor Green
& "$PSScriptRoot\fix_migration_models.ps1"
Write-Host ""

# Step 5: Fix remaining critical errors
Write-Host "Step 5: Fixing remaining critical errors..." -ForegroundColor Green
& "$PSScriptRoot\fix_remaining_critical.ps1"
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All remaining fixes applied!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Run: .\gradlew :presentation:compileKotlinDesktop" -ForegroundColor White
Write-Host "2. Check for any remaining errors" -ForegroundColor White
Write-Host "3. If successful, run full build: .\gradlew build" -ForegroundColor White
Write-Host ""
