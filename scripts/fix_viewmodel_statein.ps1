# Fix ViewModel stateIn Issues
# This script fixes all the stateIn parameter issues in ViewModels

Write-Host "Fixing ViewModel stateIn issues..." -ForegroundColor Cyan

# Function to fix stateIn calls with proper parameters
function Fix-StateIn {
    param($file)
    
    $content = Get-Content $file -Raw
    
    # Pattern 1: stateIn(scope) -> stateIn(scope, SharingStarted.WhileSubscribed(5000), initialValue)
    # For boolean flows
    $content = $content -replace '\.stateIn\(scope\)(\s*//.*)?$', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)$1'
    
    # Pattern 2: Fix stateIn with wrong number of arguments
    $content = $content -replace '\.stateIn\(\s*scope\s*,\s*SharingStarted\.\w+\(\d+\)\s*,\s*\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())'
    
    # Add SharingStarted import if not present
    if ($content -match '\.stateIn\(' -and $content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.SharingStarted"
    }
    
    Set-Content $file $content -NoNewline
}

# Fix LibraryViewModel
Write-Host "1. Fixing LibraryViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/LibraryViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix activeFilters type mismatch
    $content = $content -replace 'val activeFilters: StateFlow<Set<LibraryFilter\.Type>> =', 'val activeFilters: StateFlow<Collection<LibraryFilter.Type>> ='
    
    # Fix stateIn calls with proper parameters
    $content = $content -replace '\.stateIn\(scope, SharingStarted\.WhileSubscribed\(5000\), false\)\.stateIn\(scope, SharingStarted\.WhileSubscribed\(5000\), false\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)'
    
    # Fix the count stateIn
    $content = $content -replace '\.stateIn\(scope, SharingStarted\.WhileSubscribed\(5000\), 0\)\.stateIn\(scope, SharingStarted\.WhileSubscribed\(5000\), 0\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)'
    
    # Add SharingStarted import
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.SharingStarted"
    }
    
    Set-Content $file $content -NoNewline
}

# Fix VoiceSelectionViewModel (reader)
Write-Host "2. Fixing VoiceSelectionViewModel (reader)..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/VoiceSelectionViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Add stateIn import and fix usage
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.stateIn`nimport kotlinx.coroutines.flow.SharingStarted"
    }
    
    # Fix stateIn calls
    $content = $content -replace '\.stateIn\(scope\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)'
    
    Set-Content $file $content -NoNewline
}

# Fix BadgeManagementViewModel
Write-Host "3. Fixing BadgeManagementViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/manage/BadgeManagementViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.stateIn`nimport kotlinx.coroutines.flow.SharingStarted"
    }
    
    $content = $content -replace '\.stateIn\(scope\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())'
    
    Set-Content $file $content -NoNewline
}

# Fix NFTBadgeViewModel
Write-Host "4. Fixing NFTBadgeViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/nft/NFTBadgeViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.stateIn`nimport kotlinx.coroutines.flow.SharingStarted"
    }
    
    $content = $content -replace '\.stateIn\(scope\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())'
    
    Set-Content $file $content -NoNewline
}

# Fix BadgeStoreViewModel
Write-Host "5. Fixing BadgeStoreViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/store/BadgeStoreViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix type mismatch and stateIn
    $content = $content -replace 'val state: StateFlow<BadgeStoreState> = .*\.stateIn\(scope, SharingStarted\.WhileSubscribed\(5000\), BadgeStoreState\(\)\)\.stateIn\(scope, SharingStarted\.WhileSubscribed\(5000\), BadgeStoreState\(\)\)', 'val state: StateFlow<BadgeStoreState> = combine(...) { ... }.stateIn(scope, SharingStarted.WhileSubscribed(5000), BadgeStoreState())'
    
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.SharingStarted"
    }
    
    Set-Content $file $content -NoNewline
}

# Fix SettingsNotificationViewModel
Write-Host "6. Fixing SettingsNotificationViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/notifications/SettingsNotificationViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix Flow to StateFlow
    $content = $content -replace ': StateFlow<Pair<Int, Int>> = combine', ': StateFlow<Pair<Int, Int>> = combine'
    
    # Add stateIn at the end of combine
    $content = $content -replace '(combine\([^)]+\) \{ [^}]+ \})\s*$', '$1.stateIn(scope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))'
    
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.stateIn`nimport kotlinx.coroutines.flow.SharingStarted"
    }
    
    Set-Content $file $content -NoNewline
}

# Fix SettingsSecurityViewModel
Write-Host "7. Fixing SettingsSecurityViewModel..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/security/SettingsSecurityViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix Flow to StateFlow
    $content = $content -replace ': StateFlow<Int> = ', ': StateFlow<Int> = '
    
    # Add stateIn
    $content = $content -replace '(securityPreferences\.securityType\(\)\.changes\(\))\s*$', '$1.stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)'
    
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.stateIn`nimport kotlinx.coroutines.flow.SharingStarted"
    }
    
    Set-Content $file $content -NoNewline
}

# Fix VoiceSelectionViewModel (settings)
Write-Host "8. Fixing VoiceSelectionViewModel (settings)..." -ForegroundColor Yellow
$file = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/viewmodels/VoiceSelectionViewModel.kt"
if (Test-Path $file) {
    $content = Get-Content $file -Raw
    
    # Fix type mismatch
    $content = $content -replace 'val downloadProgress: StateFlow<DownloadProgress\?> = .*\.stateIn\(scope, SharingStarted\.WhileSubscribed\(5000\), null\)\.stateIn\(scope, SharingStarted\.WhileSubscribed\(5000\), null\)', 'val downloadProgress: StateFlow<DownloadProgress?> = MutableStateFlow<DownloadProgress?>(null).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)'
    
    # Fix Set vs Collection
    $content = $content -replace 'val installedVoices: StateFlow<Set<String>>', 'val installedVoices: StateFlow<Collection<String>>'
    
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "`$1`nimport kotlinx.coroutines.flow.SharingStarted"
    }
    
    Set-Content $file $content -NoNewline
}

Write-Host "`nViewModel stateIn fixes applied!" -ForegroundColor Green
