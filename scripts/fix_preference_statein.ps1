# Fix Preference.stateIn() Calls
# Preference.stateIn() only takes scope parameter, not SharingStarted or initialValue

Write-Host "Fixing Preference.stateIn() calls..." -ForegroundColor Cyan

# Fix LibraryViewModel
Write-Host "1. Fixing LibraryViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/LibraryViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Remove extra parameters from Preference.stateIn() calls
    # Pattern: .stateIn(scope, SharingStarted.WhileSubscribed(5000), value)
    # Should be: .stateIn(scope)
    $content = $content -replace '\.stateIn\(scope,\s*SharingStarted\.WhileSubscribed\(\d+\),\s*[^)]+\)', '.stateIn(scope)'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed LibraryViewModel" -ForegroundColor Green
}

# Fix BadgeManagementViewModel
Write-Host "2. Fixing BadgeManagementViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/manage/BadgeManagementViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix stateIn with proper combine
    $content = $content -replace '\.stateIn\(scope,\s*SharingStarted\.WhileSubscribed\(\d+\),\s*emptyList\(\)\)', '.stateIn(scope)'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed BadgeManagementViewModel" -ForegroundColor Green
}

# Fix NFTBadgeViewModel
Write-Host "3. Fixing NFTBadgeViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/nft/NFTBadgeViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    $content = $content -replace '\.stateIn\(scope,\s*SharingStarted\.WhileSubscribed\(\d+\),\s*emptyList\(\)\)', '.stateIn(scope)'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed NFTBadgeViewModel" -ForegroundColor Green
}

# Fix BadgeStoreViewModel
Write-Host "4. Fixing BadgeStoreViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/store/BadgeStoreViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # This one uses combine, need to keep the stateIn at the end of combine
    $content = $content -replace '\.stateIn\(scope,\s*SharingStarted\.WhileSubscribed\(\d+\),\s*BadgeStoreState\(\)\)', '.stateIn(scope)'
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed BadgeStoreViewModel" -ForegroundColor Green
}

# Fix SettingsNotificationViewModel
Write-Host "5. Fixing SettingsNotificationViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/notifications/SettingsNotificationViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # These are combine flows that need stateIn conversion
    # Pattern: combine(...) { ... } -> combine(...) { ... }.stateIn(scope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))
    # But if they're Preference flows, they already have stateIn
    
    # Check if these are using Preference or regular Flow
    if ($content -match 'combine\([^)]+\)\s*\{[^}]+\}\s*$') {
        # Add stateIn to combine results
        $content = $content -replace '(val \w+: StateFlow<Pair<Int, Int>> = combine\([^)]+\)\s*\{[^}]+\})', '$1.stateIn(scope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))'
    }
    
    Set-Content $file $content -NoNewline
    Write-Host "   Fixed SettingsNotificationViewModel" -ForegroundColor Green
}

Write-Host "`nPreference.stateIn() fixes applied!" -ForegroundColor Green
Write-Host "Note: Preference.stateIn() only needs scope parameter" -ForegroundColor Yellow
