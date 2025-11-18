# Batch fix for common compilation errors
# Run this script from the project root directory

Write-Host "IReader Compilation Error Batch Fix" -ForegroundColor Cyan
Write-Host "====================================`n" -ForegroundColor Cyan

$totalFixed = 0

# Fix 1: Replace .asStateFlow() with .stateIn(scope) in ViewModels
Write-Host "Fix 1: Correcting asStateFlow() calls..." -ForegroundColor Yellow
$pattern1Files = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/library/SettingsLibraryViewModel.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/notifications/SettingsNotificationViewModel.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/security/SettingsSecurityViewModel.kt"
)

foreach ($file in $pattern1Files) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        $newContent = $content -replace '\.asStateFlow\(\)', '.stateIn(scope)'
        if ($content -ne $newContent) {
            Set-Content $file -Value $newContent -NoNewline
            Write-Host "  ✓ Fixed: $file" -ForegroundColor Green
            $totalFixed++
        }
    }
}

# Fix 2: Replace .map { it } on Preferences with .changes().map { it }.stateIn(scope)
Write-Host "`nFix 2: Correcting map calls on Preference objects..." -ForegroundColor Yellow
$vmFiles = Get-ChildItem -Path "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings" -Filter "*ViewModel.kt" -Recurse

foreach ($file in $vmFiles) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Fix pattern: preferenceStore.getX().map { it } -> preferenceStore.getX().changes().map { it }.stateIn(scope)
    $content = $content -replace '(preferenceStore\.(getString|getBoolean|getInt|getLong|getFloat|getStringSet)\([^)]+\))\.map\s*\{\s*it\s*\}', '$1.changes().map { it }.stateIn(scope)'
    
    if ($content -ne $originalContent) {
        Set-Content $file.FullName -Value $content -NoNewline
        Write-Host "  ✓ Fixed: $($file.Name)" -ForegroundColor Green
        $totalFixed++
    }
}

# Fix 3: Replace onPopBackStack with popBackStack in Settings screens
Write-Host "`nFix 3: Fixing parameter names in Settings screens..." -ForegroundColor Yellow
$settingsFiles = Get-ChildItem -Path "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings" -Filter "Settings*Screen.kt" -Recurse

foreach ($file in $settingsFiles) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Replace onPopBackStack = with popBackStack =
    $content = $content -replace 'onPopBackStack\s*=', 'popBackStack ='
    
    if ($content -ne $originalContent) {
        Set-Content $file.FullName -Value $content -NoNewline
        Write-Host "  ✓ Fixed: $($file.Name)" -ForegroundColor Green
        $totalFixed++
    }
}

# Fix 4: Add missing imports for StateScreenModel
Write-Host "`nFix 4: Adding StateScreenModel imports..." -ForegroundColor Yellow
$screenModelFiles = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreenModel.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/StatsScreenModel.kt"
)

foreach ($file in $screenModelFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        $originalContent = $content
        
        # Add import if not present
        if ($content -notmatch 'import ireader\.presentation\.core\.viewmodel\.StateScreenModel') {
            $content = $content -replace '(package [^\n]+\n)', "`$1`nimport ireader.presentation.core.viewmodel.StateScreenModel`n"
        }
        
        # Replace cafe.adriel.voyager imports
        $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.StateScreenModel', 'import ireader.presentation.core.viewmodel.StateScreenModel'
        $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.screenModelScope', '// screenModelScope provided by StateScreenModel'
        $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.mutableState', '// use updateState() instead'
        
        if ($content -ne $originalContent) {
            Set-Content $file -Value $content -NoNewline
            Write-Host "  ✓ Fixed: $file" -ForegroundColor Green
            $totalFixed++
        }
    }
}

Write-Host "`n====================================`n" -ForegroundColor Cyan
Write-Host "Total files fixed: $totalFixed" -ForegroundColor $(if ($totalFixed -gt 0) { "Green" } else { "Yellow" })
Write-Host "`nNote: Some errors require manual fixes. Check the error log for details." -ForegroundColor Yellow
