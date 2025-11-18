# Remaining ViewModel and Settings Error Fixes
# Fixes stateIn errors in various ViewModels and settings screens

Write-Host "Starting remaining ViewModel error fixes..." -ForegroundColor Cyan

$filesFixed = 0

# Fix 1: CloudBackupViewModel.kt - Fix stateIn
Write-Host "`n1. Fixing CloudBackupViewModel..." -ForegroundColor Yellow
$cloudBackupPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/backups/CloudBackupViewModel.kt"
if (Test-Path $cloudBackupPath) {
    $content = Get-Content $cloudBackupPath -Raw
    
    # Add stateIn import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1stateIn`nimport kotlinx.coroutines.flow."
    }
    
    # Add SharingStarted import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1SharingStarted`nimport kotlinx.coroutines.flow."
    }
    
    # Fix stateIn call
    $content = $content -replace '\.stateIn\(scope\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())'
    
    Set-Content $cloudBackupPath $content -NoNewline
    Write-Host "  Fixed: CloudBackupViewModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 2: GoogleDriveViewModel.kt - Fix stateIn
Write-Host "`n2. Fixing GoogleDriveViewModel..." -ForegroundColor Yellow
$googleDrivePath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/backups/GoogleDriveViewModel.kt"
if (Test-Path $googleDrivePath) {
    $content = Get-Content $googleDrivePath -Raw
    
    # Add stateIn import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1stateIn`nimport kotlinx.coroutines.flow."
    }
    
    # Add SharingStarted import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1SharingStarted`nimport kotlinx.coroutines.flow."
    }
    
    # Fix stateIn call
    $content = $content -replace '\.stateIn\(scope\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())'
    
    Set-Content $googleDrivePath $content -NoNewline
    Write-Host "  Fixed: GoogleDriveViewModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 3: BadgeStoreViewModel.kt - Fix stateIn
Write-Host "`n3. Fixing BadgeStoreViewModel..." -ForegroundColor Yellow
$badgeStorePath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/store/BadgeStoreViewModel.kt"
if (Test-Path $badgeStorePath) {
    $content = Get-Content $badgeStorePath -Raw
    
    # Add stateIn import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1stateIn`nimport kotlinx.coroutines.flow."
    }
    
    # Add SharingStarted import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1SharingStarted`nimport kotlinx.coroutines.flow."
    }
    
    # Fix stateIn call
    $content = $content -replace '\.stateIn\(scope\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())'
    
    Set-Content $badgeStorePath $content -NoNewline
    Write-Host "  Fixed: BadgeStoreViewModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 4: SettingsNotificationViewModel.kt - Fix Flow to StateFlow
Write-Host "`n4. Fixing SettingsNotificationViewModel..." -ForegroundColor Yellow
$notificationViewModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/notifications/SettingsNotificationViewModel.kt"
if (Test-Path $notificationViewModelPath) {
    $content = Get-Content $notificationViewModelPath -Raw
    
    # Add stateIn import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1stateIn`nimport kotlinx.coroutines.flow."
    }
    
    # Add SharingStarted import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1SharingStarted`nimport kotlinx.coroutines.flow."
    }
    
    # Fix Flow to StateFlow by adding stateIn
    $content = $content -replace '(val\s+\w+:\s*StateFlow<[^>]+>\s*=\s*[^.]+\.[^.]+\([^)]*\))\s*$', '$1.stateIn(scope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))'
    
    Set-Content $notificationViewModelPath $content -NoNewline
    Write-Host "  Fixed: SettingsNotificationViewModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 5: SettingsSecurityViewModel.kt - Fix map on Preference
Write-Host "`n5. Fixing SettingsSecurityViewModel..." -ForegroundColor Yellow
$securityViewModelPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/security/SettingsSecurityViewModel.kt"
if (Test-Path $securityViewModelPath) {
    $content = Get-Content $securityViewModelPath -Raw
    
    # Fix map on Preference - convert to Flow first
    $content = $content -replace '(securityPreferences\.[^.]+\(\)\.get\(\))\.map', '$1.asFlow().map'
    
    # Add asFlow import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.asFlow') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1map`nimport kotlinx.coroutines.flow."
    }
    
    Set-Content $securityViewModelPath $content -NoNewline
    Write-Host "  Fixed: SettingsSecurityViewModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 6: AdvancedStatisticsScreen.kt - Fix koin reference and unclosed comment
Write-Host "`n6. Fixing AdvancedStatisticsScreen..." -ForegroundColor Yellow
$advancedStatsPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/AdvancedStatisticsScreen.kt"
if (Test-Path $advancedStatsPath) {
    $content = Get-Content $advancedStatsPath -Raw
    
    # Fix koin() to getKoin()
    $content = $content -replace '\bkoin\(\)', 'getKoin()'
    
    # Add getKoin import if missing
    if ($content -notmatch 'import org\.koin\.compose\.getKoin') {
        $content = $content -replace '(import org\.koin)', "`$1.compose.getKoin`nimport org.koin"
    }
    
    # Fix unclosed comment - find and close it
    $content = $content -replace '(/\*[^*]*\*(?!/\*)[^*]*$)', '$1*/'
    
    Set-Content $advancedStatsPath $content -NoNewline
    Write-Host "  Fixed: AdvancedStatisticsScreen.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 7: EnhancedStatisticsScreen.kt and StatisticsScreen.kt - Fix count property to function
Write-Host "`n7. Fixing Statistics screens..." -ForegroundColor Yellow
$enhancedStatsPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/EnhancedStatisticsScreen.kt"
$statsPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/StatisticsScreen.kt"

foreach ($path in @($enhancedStatsPath, $statsPath)) {
    if (Test-Path $path) {
        $content = Get-Content $path -Raw
        
        # Fix .count to .count()
        $content = $content -replace '\.count\s*\)', '.count())'
        $content = $content -replace '(\w+)\.count([^(])', '$1.count()$2'
        
        Set-Content $path $content -NoNewline
        Write-Host "  Fixed: $(Split-Path $path -Leaf)" -ForegroundColor Green
        $filesFixed++
    }
}

# Fix 8: VoiceSelectionViewModel.kt - Fix stateIn
Write-Host "`n8. Fixing VoiceSelectionViewModel..." -ForegroundColor Yellow
$voiceSelectionPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/viewmodels/VoiceSelectionViewModel.kt"
if (Test-Path $voiceSelectionPath) {
    $content = Get-Content $voiceSelectionPath -Raw
    
    # Add stateIn import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.stateIn') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1stateIn`nimport kotlinx.coroutines.flow."
    }
    
    # Add SharingStarted import if missing
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.SharingStarted') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1SharingStarted`nimport kotlinx.coroutines.flow."
    }
    
    # Fix stateIn call
    $content = $content -replace '\.stateIn\(scope\)', '.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())'
    
    Set-Content $voiceSelectionPath $content -NoNewline
    Write-Host "  Fixed: VoiceSelectionViewModel.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 9: DynamicColors.kt - Fix getDynamicColorScheme reference
Write-Host "`n9. Fixing DynamicColors..." -ForegroundColor Yellow
$dynamicColorsPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/theme/DynamicColors.kt"
if (Test-Path $dynamicColorsPath) {
    $content = Get-Content $dynamicColorsPath -Raw
    
    # Check if getDynamicColorScheme is defined, if not, create it
    if ($content -notmatch 'fun getDynamicColorScheme') {
        $getDynamicColorSchemeFunc = @"

@Composable
private fun getDynamicColorScheme(isDark: Boolean): ColorScheme? {
    if (!isSupported()) return null
    
    return try {
        val context = LocalContext.current
        if (isDark) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } catch (e: Exception) {
        null
    }
}
"@
        # Add before IReaderDynamicTheme function
        $content = $content -replace '(@Composable\s+fun IReaderDynamicTheme)', "$getDynamicColorSchemeFunc`n`n`$1"
    }
    
    Set-Content $dynamicColorsPath $content -NoNewline
    Write-Host "  Fixed: DynamicColors.kt" -ForegroundColor Green
    $filesFixed++
}

# Fix 10: GlobalSearchViewModel (sources) - Fix Flow operations
Write-Host "`n10. Fixing GlobalSearchViewModel (sources)..." -ForegroundColor Yellow
$globalSearchSourcesPath = "presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/global_search/viewmodel/GlobalSearchViewModel.kt"
if (Test-Path $globalSearchSourcesPath) {
    $content = Get-Content $globalSearchSourcesPath -Raw
    
    # Add missing imports
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.onStart') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1onStart`nimport kotlinx.coroutines.flow."
    }
    if ($content -notmatch 'import kotlinx\.coroutines\.flow\.map') {
        $content = $content -replace '(import kotlinx\.coroutines\.flow\.)', "`$1map`nimport kotlinx.coroutines.flow."
    }
    
    Set-Content $globalSearchSourcesPath $content -NoNewline
    Write-Host "  Fixed: GlobalSearchViewModel (sources).kt" -ForegroundColor Green
    $filesFixed++
}

Write-Host "`n=== Fix Summary ===" -ForegroundColor Cyan
Write-Host "Total files fixed: $filesFixed" -ForegroundColor Green
Write-Host "`nPlease review the changes and test compilation." -ForegroundColor Yellow
