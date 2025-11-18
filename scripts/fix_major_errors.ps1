# Fix major compilation errors in IReader project
# This script addresses the most critical errors preventing compilation

Write-Host "Fixing Major Compilation Errors" -ForegroundColor Cyan
Write-Host "================================`n" -ForegroundColor Cyan

$fixCount = 0

# Fix 1: Add StateScreenModel imports to files using it
Write-Host "1. Adding StateScreenModel imports..." -ForegroundColor Yellow

$screenModelFiles = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/download/DownloadScreenModel.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/StatsScreenModel.kt"
)

foreach ($filePath in $screenModelFiles) {
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        $originalContent = $content
        
        # Replace Voyager imports with IReader imports
        $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.StateScreenModel', 'import ireader.presentation.core.viewmodel.StateScreenModel'
        $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.screenModelScope', ''
        $content = $content -replace 'import cafe\.adriel\.voyager\.core\.model\.mutableState', ''
        
        # Replace class inheritance
        $content = $content -replace ': StateScreenModel<([^>]+)>\(([^)]+)\)', ': StateScreenModel<$1>($2)'
        
        # Replace mutableState { } with updateState { }
        $content = $content -replace 'mutableState\s*\{\s*([^}]+)\s*\}', 'updateState { $1 }'
        
        if ($content -ne $originalContent) {
            Set-Content $filePath -Value $content -NoNewline
            Write-Host "  Fixed: $filePath" -ForegroundColor Green
            $fixCount++
        }
    }
}

# Fix 2: Fix Settings screen parameter names
Write-Host "`n2. Fixing Settings screen parameters..." -ForegroundColor Yellow

$settingsScreens = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/appearance/SettingsAppearanceScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/data/SettingsDataScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/downloads/SettingsDownloadScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/library/SettingsLibraryScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/main/SettingsMainScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/notifications/SettingsNotificationScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/reader/SettingsReaderScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/security/SettingsSecurityScreen.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/tracking/SettingsTrackingScreen.kt"
)

foreach ($filePath in $settingsScreens) {
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        $originalContent = $content
        
        # Replace onPopBackStack = with popBackStack =
        $content = $content -replace 'onPopBackStack\s*=\s*', 'popBackStack = '
        
        if ($content -ne $originalContent) {
            Set-Content $filePath -Value $content -NoNewline
            Write-Host "  Fixed: $filePath" -ForegroundColor Green
            $fixCount++
        }
    }
}

# Fix 3: Fix Preference.map() calls
Write-Host "`n3. Fixing Preference map calls..." -ForegroundColor Yellow

$viewModelFiles = @(
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/notifications/SettingsNotificationViewModel.kt",
    "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/security/SettingsSecurityViewModel.kt"
)

foreach ($filePath in $viewModelFiles) {
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        $originalContent = $content
        
        # Fix .map { it } on preferences - need to use .changes().map { it }.stateIn()
        $content = $content -replace '(preferenceStore\.\w+\([^)]+\))\.map\s*\{\s*it\s*\}', '$1.changes().map { it }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())'
        
        # Add SharingStarted import if not present
        if ($content -match 'SharingStarted' -and $content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
            $content = $content -replace '(import kotlinx\.coroutines\.flow\.StateFlow)', "import kotlinx.coroutines.flow.SharingStarted`n`$1"
        }
        
        if ($content -ne $originalContent) {
            Set-Content $filePath -Value $content -NoNewline
            Write-Host "  Fixed: $filePath" -ForegroundColor Green
            $fixCount++
        }
    }
}

Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "Total files fixed: $fixCount" -ForegroundColor Green
Write-Host "`nNote: Manual fixes may still be needed for complex errors." -ForegroundColor Yellow
